package cl.duoc.mpazneb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazneb.contenedor
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.data.repository.AuthRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.domain.FilaObservacion
import cl.duoc.mpazneb.domain.FilaResultado
import cl.duoc.mpazneb.domain.ResultadoDescriptivo
import cl.duoc.mpazneb.domain.TextoCsv
import cl.duoc.mpazneb.domain.validation.ErroresObservacion
import cl.duoc.mpazneb.domain.validation.ObservacionValidator
import cl.duoc.mpazneb.model.Observacion
import cl.duoc.mpazneb.model.Respuesta
import cl.duoc.mpazneb.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResultadosUiState(
    val cargando: Boolean = true,
    val estudiante: String = "",
    val actividad: String = "",
    val codigo: String = "",
    val estadoAsignacion: String = "",
    val resultado: String = "",
    val respuestas: List<FilaResultado> = emptyList(),
    val observaciones: List<Observacion> = emptyList(),
    val categoria: String = "",
    val textoObservacion: String = "",
    val intentoGuardar: Boolean = false,
    val contenidoCsv: String = "",
    val pedirArchivo: Boolean = false,
    val mensaje: String? = null,
    val mensajeEsError: Boolean = false,
    val sinPermiso: Boolean = false
) {
    val errores: ErroresObservacion
        get() = if (!intentoGuardar) ErroresObservacion() else ObservacionValidator.validar(categoria, textoObservacion)
}

class ResultadosViewModel(
    private val asignacionId: Long,
    private val auth: AuthRepository,
    private val actividades: ActividadRepository,
    private val asignaciones: AsignacionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(ResultadosUiState())
    val estado: StateFlow<ResultadosUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val sesion = auth.sesionActual.first()
            if (sesion == null || sesion.rol != Rol.ASISTENTE) {
                _estado.update { it.copy(cargando = false, sinPermiso = true) }
                return@launch
            }
            combine(
                asignaciones.detalle(asignacionId),
                asignaciones.respuestas(asignacionId),
                asignaciones.observaciones(asignacionId)
            ) { detalle, respuestas, observaciones ->
                Triple(detalle, respuestas, observaciones)
            }.collect { (detalle, respuestas, observaciones) ->
                if (detalle == null) {
                    _estado.update {
                        it.copy(cargando = false, mensaje = "No se encontró la asignación.", mensajeEsError = true)
                    }
                    return@collect
                }
                val completa = actividades.obtenerCompleta(detalle.actividad.id)
                val filas = armarFilas(completa?.preguntas.orEmpty(), respuestas)
                val total = completa?.preguntas?.size ?: 0
                val correctas = respuestas.filter { it.correcta }.map { it.preguntaId }.distinct().size
                val frase = ResultadoDescriptivo.frase(correctas, total)
                val csv = TextoCsv.documento(
                    estudiante = detalle.estudiante.alias,
                    actividad = detalle.actividad.nombre,
                    codigo = detalle.asignacion.codigo,
                    estado = detalle.asignacion.estado.etiqueta,
                    resultado = frase,
                    respuestas = filas,
                    observaciones = observaciones.map { FilaObservacion(it.categoria, it.texto) }
                )
                _estado.update { actual ->
                    actual.copy(
                        cargando = false,
                        estudiante = detalle.estudiante.alias,
                        actividad = detalle.actividad.nombre,
                        codigo = detalle.asignacion.codigo,
                        estadoAsignacion = detalle.asignacion.estado.etiqueta,
                        resultado = frase,
                        respuestas = filas,
                        observaciones = observaciones,
                        contenidoCsv = csv
                    )
                }
            }
        }
    }

    fun cambiarCategoria(valor: String) = _estado.update { it.copy(categoria = valor, mensaje = null) }

    fun cambiarTexto(valor: String) = _estado.update { it.copy(textoObservacion = valor.take(ObservacionValidator.TEXTO_MAX), mensaje = null) }

    fun guardarObservacion() {
        _estado.update { it.copy(intentoGuardar = true, mensaje = null) }
        val actual = _estado.value
        val errores = ObservacionValidator.validar(actual.categoria, actual.textoObservacion)
        if (!errores.valido) {
            _estado.update { it.copy(mensaje = "Revisa la observación.", mensajeEsError = true) }
            return
        }
        viewModelScope.launch {
            when (val resultado = asignaciones.guardarObservacion(asignacionId, actual.categoria, actual.textoObservacion)) {
                is ResultadoAccion.Exito -> _estado.update {
                    it.copy(
                        textoObservacion = "",
                        intentoGuardar = false,
                        mensaje = "Observación ficticia guardada.",
                        mensajeEsError = false
                    )
                }
                is ResultadoAccion.Error -> _estado.update {
                    it.copy(mensaje = resultado.mensaje, mensajeEsError = true)
                }
            }
        }
    }

    fun pedirExportacion() = _estado.update { it.copy(pedirArchivo = true) }

    fun archivoPedido() = _estado.update { it.copy(pedirArchivo = false) }

    fun avisarExportacion(mensaje: String, esError: Boolean) =
        _estado.update { it.copy(mensaje = mensaje, mensajeEsError = esError) }

    private fun armarFilas(
        preguntas: List<cl.duoc.mpazneb.model.PreguntaConAlternativas>,
        respuestas: List<Respuesta>
    ): List<FilaResultado> {
        return respuestas.map { respuesta ->
            val pregunta = preguntas.firstOrNull { it.pregunta.id == respuesta.preguntaId }
            val alternativa = pregunta?.alternativas?.firstOrNull { it.id == respuesta.alternativaId }
            FilaResultado(
                enunciado = pregunta?.pregunta?.enunciado ?: "Pregunta",
                elegida = alternativa?.texto ?: "Respuesta",
                correcta = respuesta.correcta,
                intento = respuesta.intento
            )
        }
    }

    companion object {
        fun factory(asignacionId: Long) = viewModelFactory {
            initializer {
                val contenedor = contenedor()
                ResultadosViewModel(
                    asignacionId,
                    contenedor.authRepository,
                    contenedor.actividadRepository,
                    contenedor.asignacionRepository
                )
            }
        }
    }
}
