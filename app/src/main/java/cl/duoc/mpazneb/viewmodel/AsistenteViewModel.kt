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
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.AsignacionDetalle
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Rol
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FiltrosAsistente(
    val modulo: Modulo? = null,
    val curso: Int? = null,
    val dificultad: Int? = null
)

data class AsistenteUiState(
    val cargando: Boolean = true,
    val alias: String = "",
    val modulo: Modulo? = null,
    val curso: Int? = null,
    val dificultad: Int? = null,
    val actividades: List<Actividad> = emptyList(),
    val asignaciones: List<AsignacionDetalle> = emptyList(),
    val mensaje: String? = null,
    val mensajeEsError: Boolean = false,
    val sinPermiso: Boolean = false,
    val sesionCerrada: Boolean = false
) {
    val filtrosCompletos: Boolean get() = modulo != null && curso != null && dificultad != null
}

class AsistenteViewModel(
    private val auth: AuthRepository,
    private val actividades: ActividadRepository,
    private val asignaciones: AsignacionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(AsistenteUiState())
    val estado: StateFlow<AsistenteUiState> = _estado.asStateFlow()
    private val filtros = MutableStateFlow(FiltrosAsistente())
    private var cargaActividades: Job? = null

    init {
        viewModelScope.launch {
            val sesion = auth.sesionActual.first()
            if (sesion == null || sesion.rol != Rol.ASISTENTE) {
                _estado.update { it.copy(cargando = false, sinPermiso = true) }
                return@launch
            }
            _estado.update { it.copy(alias = sesion.alias, cargando = false) }
            asignaciones.delAsistente(sesion.id).collect { lista ->
                _estado.update { it.copy(asignaciones = lista) }
            }
        }
    }

    fun elegirModulo(modulo: Modulo) = aplicar(filtros.value.copy(modulo = modulo))

    fun elegirCurso(curso: Int) = aplicar(filtros.value.copy(curso = curso))

    fun elegirDificultad(dificultad: Int) = aplicar(filtros.value.copy(dificultad = dificultad))

    private fun aplicar(nuevo: FiltrosAsistente) {
        filtros.value = nuevo
        _estado.update {
            it.copy(modulo = nuevo.modulo, curso = nuevo.curso, dificultad = nuevo.dificultad, mensaje = null)
        }
        recargarActividades()
    }

    /** Cada cambio de filtro cancela la consulta anterior y escucha la nueva. */
    private fun recargarActividades() {
        cargaActividades?.cancel()
        val modulo = filtros.value.modulo
        val curso = filtros.value.curso
        val dificultad = filtros.value.dificultad
        if (modulo == null || curso == null || dificultad == null) {
            _estado.update { it.copy(actividades = emptyList()) }
            return
        }
        cargaActividades = viewModelScope.launch {
            actividades.disponibles(modulo, curso, dificultad).collect { lista ->
                _estado.update { it.copy(actividades = lista) }
            }
        }
    }

    fun renovarCodigo(asignacionId: Long) {
        viewModelScope.launch {
            when (val resultado = asignaciones.regenerarCodigo(asignacionId)) {
                is ResultadoAccion.Exito -> _estado.update {
                    it.copy(
                        mensaje = "Nuevo código: ${resultado.asignacion?.codigo.orEmpty()}",
                        mensajeEsError = false
                    )
                }
                is ResultadoAccion.Error -> _estado.update {
                    it.copy(mensaje = resultado.mensaje, mensajeEsError = true)
                }
            }
        }
    }

    fun cerrarMensaje() = _estado.update { it.copy(mensaje = null) }

    fun cerrarSesion() {
        viewModelScope.launch {
            auth.cerrarSesion()
            _estado.update { it.copy(sesionCerrada = true) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val contenedor = contenedor()
                AsistenteViewModel(
                    contenedor.authRepository,
                    contenedor.actividadRepository,
                    contenedor.asignacionRepository
                )
            }
        }
    }
}
