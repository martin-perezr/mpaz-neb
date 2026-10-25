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
import cl.duoc.mpazneb.domain.validation.AsignacionFormValidator
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.Estudiante
import cl.duoc.mpazneb.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AsignarUiState(
    val cargando: Boolean = true,
    val actividad: Actividad? = null,
    val estudiantes: List<Estudiante> = emptyList(),
    val estudianteId: Long? = null,
    val intentoAsignar: Boolean = false,
    val codigo: String? = null,
    val mensaje: String? = null,
    val mensajeEsError: Boolean = false,
    val sinPermiso: Boolean = false
) {
    val errorEstudiante: String?
        get() = if (intentoAsignar) AsignacionFormValidator.validarEstudiante(estudianteId) else null
}

class AsignarViewModel(
    private val actividadId: Long,
    private val auth: AuthRepository,
    private val actividades: ActividadRepository,
    private val asignaciones: AsignacionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(AsignarUiState())
    val estado: StateFlow<AsignarUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val sesion = auth.sesionActual.first()
            if (sesion == null || sesion.rol != Rol.ASISTENTE) {
                _estado.update { it.copy(cargando = false, sinPermiso = true) }
                return@launch
            }
            val actividad = actividades.obtener(actividadId)
            if (actividad == null) {
                _estado.update { it.copy(cargando = false, mensaje = "No se encontró la actividad.", mensajeEsError = true) }
                return@launch
            }
            _estado.update { it.copy(cargando = false, actividad = actividad) }
            asignaciones.estudiantesPorCurso(actividad.curso).collect { lista ->
                _estado.update { it.copy(estudiantes = lista) }
            }
        }
    }

    fun elegirEstudiante(id: Long) = _estado.update { it.copy(estudianteId = id, mensaje = null) }

    fun asignar() {
        _estado.update { it.copy(intentoAsignar = true, mensaje = null) }
        val estudianteId = _estado.value.estudianteId
        if (AsignacionFormValidator.validarEstudiante(estudianteId) != null) {
            _estado.update { it.copy(mensaje = "Elige un estudiante ficticio.", mensajeEsError = true) }
            return
        }
        viewModelScope.launch {
            when (val resultado = asignaciones.crear(estudianteId!!, actividadId)) {
                is ResultadoAccion.Exito -> _estado.update {
                    it.copy(
                        codigo = resultado.asignacion?.codigo,
                        mensaje = "Asignación guardada. El código vence en 30 minutos.",
                        mensajeEsError = false
                    )
                }
                is ResultadoAccion.Error -> _estado.update {
                    it.copy(mensaje = resultado.mensaje, mensajeEsError = true)
                }
            }
        }
    }

    companion object {
        fun factory(actividadId: Long) = viewModelFactory {
            initializer {
                val contenedor = contenedor()
                AsignarViewModel(
                    actividadId,
                    contenedor.authRepository,
                    contenedor.actividadRepository,
                    contenedor.asignacionRepository
                )
            }
        }
    }
}
