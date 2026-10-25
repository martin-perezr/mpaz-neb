package cl.duoc.mpazneb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazneb.contenedor
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AuthRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.domain.validation.ErroresPregunta
import cl.duoc.mpazneb.domain.validation.PreguntaValidator
import cl.duoc.mpazneb.model.Alternativa
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Pregunta
import cl.duoc.mpazneb.model.Rol
import cl.duoc.mpazneb.model.TipoPregunta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorPreguntaUiState(
    val cargando: Boolean = true,
    val modulo: Modulo? = null,
    val enunciado: String = "",
    val tipo: TipoPregunta? = null,
    val alternativas: List<String> = List(PreguntaValidator.MAX_ALTERNATIVAS) { "" },
    val indiceCorrecta: Int? = null,
    val retroalimentacion: String = "",
    val pista: String = "",
    val procedimiento: String = "",
    val numeroA: String = "",
    val numeroB: String = "",
    val operacion: String = "+",
    val intentoGuardar: Boolean = false,
    val mensaje: String? = null,
    val mensajeEsError: Boolean = false,
    val sinPermiso: Boolean = false,
    val guardada: Boolean = false
) {
    val errores: ErroresPregunta
        get() = if (!intentoGuardar) ErroresPregunta() else PreguntaValidator.validar(
            modulo, enunciado, tipo, alternativas, indiceCorrecta,
            retroalimentacion, pista, procedimiento, numeroA, numeroB
        )
}

class EditorPreguntaViewModel(
    private val actividadId: Long,
    private val preguntaId: Long,
    private val auth: AuthRepository,
    private val actividades: ActividadRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(EditorPreguntaUiState())
    val estado: StateFlow<EditorPreguntaUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val sesion = auth.sesionActual.first()
            if (sesion == null || sesion.rol != Rol.ADMIN) {
                _estado.update { it.copy(cargando = false, sinPermiso = true) }
                return@launch
            }
            val actividad = actividades.obtener(actividadId)
            if (actividad == null) {
                _estado.update { it.copy(cargando = false, mensaje = "No se encontró la actividad", mensajeEsError = true) }
                return@launch
            }
            val tipoInicial = TipoPregunta.de(actividad.modulo).first()
            if (preguntaId == 0L) {
                _estado.update { it.copy(cargando = false, modulo = actividad.modulo, tipo = tipoInicial) }
                return@launch
            }
            val completa = actividades.obtenerPregunta(preguntaId)
            if (completa == null) {
                _estado.update { it.copy(cargando = false, modulo = actividad.modulo, mensaje = "No se encontró la pregunta", mensajeEsError = true) }
                return@launch
            }
            val textos = MutableList(PreguntaValidator.MAX_ALTERNATIVAS) { "" }
            completa.alternativas.sortedBy { it.id }.forEachIndexed { indice, alternativa ->
                if (indice < textos.size) textos[indice] = alternativa.texto
            }
            val correcta = completa.alternativas.sortedBy { it.id }.indexOfFirst { it.esCorrecta }
            _estado.update {
                it.copy(
                    cargando = false,
                    modulo = actividad.modulo,
                    enunciado = completa.pregunta.enunciado,
                    tipo = completa.pregunta.tipo,
                    alternativas = textos,
                    indiceCorrecta = if (correcta >= 0) correcta else null,
                    retroalimentacion = completa.pregunta.retroalimentacion,
                    pista = completa.pregunta.pista,
                    procedimiento = completa.pregunta.procedimiento.orEmpty(),
                    numeroA = completa.pregunta.numeroA?.toString().orEmpty(),
                    numeroB = completa.pregunta.numeroB?.toString().orEmpty(),
                    operacion = completa.pregunta.operacion ?: "+"
                )
            }
        }
    }

    fun cambiarEnunciado(valor: String) = _estado.update { it.copy(enunciado = valor, mensaje = null) }
    fun cambiarTipo(tipo: TipoPregunta) = _estado.update { it.copy(tipo = tipo, mensaje = null) }
    fun cambiarRetroalimentacion(valor: String) = _estado.update { it.copy(retroalimentacion = valor, mensaje = null) }
    fun cambiarPista(valor: String) = _estado.update { it.copy(pista = valor, mensaje = null) }
    fun cambiarProcedimiento(valor: String) = _estado.update { it.copy(procedimiento = valor, mensaje = null) }
    fun cambiarNumeroA(valor: String) = _estado.update { it.copy(numeroA = valor.filter { c -> c.isDigit() }.take(4), mensaje = null) }
    fun cambiarNumeroB(valor: String) = _estado.update { it.copy(numeroB = valor.filter { c -> c.isDigit() }.take(4), mensaje = null) }
    fun cambiarOperacion(valor: String) = _estado.update { it.copy(operacion = valor, mensaje = null) }
    fun marcarCorrecta(indice: Int) = _estado.update { it.copy(indiceCorrecta = indice, mensaje = null) }

    fun cambiarAlternativa(indice: Int, valor: String) = _estado.update { estado ->
        val copia = estado.alternativas.toMutableList()
        if (indice in copia.indices) copia[indice] = valor
        estado.copy(alternativas = copia, mensaje = null)
    }

    fun guardar() {
        _estado.update { it.copy(intentoGuardar = true, mensaje = null) }
        val actual = _estado.value
        val errores = PreguntaValidator.validar(
            actual.modulo, actual.enunciado, actual.tipo, actual.alternativas, actual.indiceCorrecta,
            actual.retroalimentacion, actual.pista, actual.procedimiento, actual.numeroA, actual.numeroB
        )
        if (!errores.valido) {
            _estado.update { it.copy(mensaje = "Revisa los campos marcados.", mensajeEsError = true) }
            return
        }
        val textos = PreguntaValidator.leerAlternativas(actual.alternativas).first ?: return
        val tipo = actual.tipo ?: return
        val correcta = actual.indiceCorrecta ?: return
        viewModelScope.launch {
            val ordenReal = if (preguntaId == 0L) {
                actividades.contarPreguntas(actividadId) + 1
            } else {
                actividades.obtenerPregunta(preguntaId)?.pregunta?.orden ?: 1
            }
            val usaNumeros = actual.modulo == Modulo.MATEMATICA &&
                (actual.numeroA.isNotBlank() || actual.numeroB.isNotBlank())
            val pregunta = Pregunta(
                id = preguntaId,
                actividadId = actividadId,
                orden = ordenReal,
                tipo = tipo,
                enunciado = actual.enunciado.trim(),
                retroalimentacion = actual.retroalimentacion.trim(),
                pista = actual.pista.trim(),
                numeroA = if (usaNumeros) actual.numeroA.toIntOrNull() else null,
                numeroB = if (usaNumeros) actual.numeroB.toIntOrNull() else null,
                operacion = if (usaNumeros) actual.operacion else null,
                procedimiento = actual.procedimiento.trim().ifBlank { null }
            )
            val alternativas = textos.mapIndexed { indice, texto ->
                Alternativa(preguntaId = preguntaId, texto = texto, esCorrecta = indice == correcta)
            }
            when (val resultado = actividades.guardarPregunta(pregunta, alternativas)) {
                is ResultadoAccion.Exito -> _estado.update {
                    it.copy(mensaje = "Pregunta guardada.", mensajeEsError = false, guardada = true)
                }
                is ResultadoAccion.Error -> _estado.update {
                    it.copy(mensaje = resultado.mensaje, mensajeEsError = true)
                }
            }
        }
    }

    companion object {
        fun factory(actividadId: Long, preguntaId: Long) = viewModelFactory {
            initializer {
                val contenedor = contenedor()
                EditorPreguntaViewModel(
                    actividadId,
                    preguntaId,
                    contenedor.authRepository,
                    contenedor.actividadRepository
                )
            }
        }
    }
}
