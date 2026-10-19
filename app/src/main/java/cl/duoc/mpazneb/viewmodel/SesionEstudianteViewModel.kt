package cl.duoc.mpazneb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazneb.contenedor
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.Alternativa
import cl.duoc.mpazneb.model.PreguntaConAlternativas
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Máquina de estados de la sesión:
 * INICIO → PREGUNTA → CONFIRMAR → (ACIERTO | CASI → PREGUNTA | REVELAR) → siguiente pregunta … → FIN
 */
enum class Fase { CARGANDO, INICIO, PREGUNTA, CONFIRMAR, ACIERTO, CASI, REVELAR, FIN, ERROR }

/** Efectos de una sola vez que la pantalla convierte en vibración. */
enum class Efecto { ENVIO, ACIERTO, CASI }

data class SesionUiState(
    val fase: Fase = Fase.CARGANDO,
    val actividad: Actividad? = null,
    val alias: String = "",
    val preguntas: List<PreguntaConAlternativas> = emptyList(),
    val indice: Int = 0,
    val seleccion: Long? = null,
    val intento: Int = 1,
    val descartadas: Set<Long> = emptySet(),
    val estrellas: Int = 0
) {
    val preguntaActual: PreguntaConAlternativas? get() = preguntas.getOrNull(indice)
    val alternativas: List<Alternativa> get() = preguntaActual?.alternativas.orEmpty().sortedBy { it.id }
    val correcta: Alternativa? get() = alternativas.firstOrNull { it.esCorrecta }
    val total: Int get() = preguntas.size
    val esUltima: Boolean get() = indice == total - 1
    val intentosPermitidos: Int get() = actividad?.intentosPermitidos ?: 2
    val progreso: Float
        get() {
            if (total == 0) return 0f
            val respondidas = indice + if (fase == Fase.ACIERTO || fase == Fase.REVELAR || fase == Fase.FIN) 1 else 0
            return respondidas.toFloat() / total
        }
}

class SesionEstudianteViewModel(
    private val asignacionId: Long,
    private val actividades: ActividadRepository,
    private val asignaciones: AsignacionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(SesionUiState())
    val estado: StateFlow<SesionUiState> = _estado.asStateFlow()

    private val _efectos = Channel<Efecto>(Channel.BUFFERED)
    val efectos = _efectos.receiveAsFlow()

    init {
        viewModelScope.launch {
            val asignacion = asignaciones.obtener(asignacionId)
            val completa = asignacion?.let { actividades.obtenerCompleta(it.actividadId) }
            if (completa == null || completa.preguntas.isEmpty()) {
                _estado.update { it.copy(fase = Fase.ERROR) }
                return@launch
            }
            asignaciones.iniciar(asignacionId) // Pendiente → En desarrollo
            _estado.update {
                it.copy(
                    fase = Fase.INICIO,
                    actividad = completa.actividad,
                    preguntas = completa.preguntas.sortedBy { p -> p.pregunta.orden }
                )
            }
        }
    }

    fun comenzar() = _estado.update { it.copy(fase = Fase.PREGUNTA) }

    fun seleccionar(alternativaId: Long) = _estado.update {
        if (it.fase == Fase.PREGUNTA && alternativaId !in it.descartadas) it.copy(seleccion = alternativaId) else it
    }

    /** Antes de enviar se pide confirmación (requisito del caso). */
    fun pedirConfirmacion() = _estado.update {
        if (it.fase == Fase.PREGUNTA && it.seleccion != null) it.copy(fase = Fase.CONFIRMAR) else it
    }

    fun revisar() = _estado.update { if (it.fase == Fase.CONFIRMAR) it.copy(fase = Fase.PREGUNTA) else it }

    fun confirmar() {
        val s = _estado.value
        val pregunta = s.preguntaActual ?: return
        val elegida = s.seleccion ?: return
        if (s.fase != Fase.CONFIRMAR) return
        val esCorrecta = s.alternativas.firstOrNull { it.id == elegida }?.esCorrecta == true
        viewModelScope.launch {
            _efectos.send(Efecto.ENVIO)
            asignaciones.registrarRespuesta(asignacionId, pregunta.pregunta.id, elegida, esCorrecta, s.intento)
            when {
                esCorrecta -> {
                    _estado.update { it.copy(fase = Fase.ACIERTO, estrellas = it.estrellas + 1) }
                    _efectos.send(Efecto.ACIERTO)
                }
                s.intento < s.intentosPermitidos -> {
                    _estado.update { it.copy(fase = Fase.CASI, descartadas = it.descartadas + elegida) }
                    _efectos.send(Efecto.CASI)
                }
                else -> _estado.update { it.copy(fase = Fase.REVELAR) }
            }
        }
    }

    fun reintentar() = _estado.update {
        if (it.fase == Fase.CASI) it.copy(fase = Fase.PREGUNTA, seleccion = null, intento = it.intento + 1) else it
    }

    fun siguiente() {
        val s = _estado.value
        if (s.fase != Fase.ACIERTO && s.fase != Fase.REVELAR) return
        if (s.esUltima) {
            viewModelScope.launch {
                asignaciones.finalizar(asignacionId)
                _estado.update { it.copy(fase = Fase.FIN) }
            }
        } else {
            _estado.update {
                it.copy(fase = Fase.PREGUNTA, indice = it.indice + 1, seleccion = null, intento = 1, descartadas = emptySet())
            }
        }
    }

    companion object {
        fun factory(asignacionId: Long) = viewModelFactory {
            initializer {
                val c = contenedor()
                SesionEstudianteViewModel(asignacionId, c.actividadRepository, c.asignacionRepository)
            }
        }
    }
}
