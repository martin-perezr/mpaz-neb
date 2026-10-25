package cl.duoc.mpazneb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazneb.contenedor
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AuthRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.model.ActividadConConteo
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogoUiState(
    val cargando: Boolean = true,
    val alias: String = "",
    val todas: List<ActividadConConteo> = emptyList(),
    val filtroModulo: Modulo? = null,
    val filtroCurso: Int? = null,
    val filtroDificultad: Int? = null,
    val mensaje: String? = null,
    val mensajeEsError: Boolean = false,
    val sinPermiso: Boolean = false,
    val sesionCerrada: Boolean = false
) {
    /** El filtro ocurre aquí, sobre la lista que ya llegó de Room. */
    val visibles: List<ActividadConConteo>
        get() = todas.filter { fila ->
            val actividad = fila.actividad
            val moduloOk = filtroModulo == null || actividad.modulo == filtroModulo
            val cursoOk = filtroCurso == null || actividad.curso == filtroCurso
            val dificultadOk = filtroDificultad == null || actividad.dificultad == filtroDificultad
            moduloOk && cursoOk && dificultadOk
        }
}

class CatalogoViewModel(
    private val auth: AuthRepository,
    private val actividades: ActividadRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(CatalogoUiState())
    val estado: StateFlow<CatalogoUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val sesion = auth.sesionActual.first()
            if (sesion == null || sesion.rol != Rol.ADMIN) {
                _estado.update { it.copy(cargando = false, sinPermiso = true) }
                return@launch
            }
            _estado.update { it.copy(alias = sesion.alias) }
            actividades.catalogo().collect { lista ->
                _estado.update { it.copy(cargando = false, todas = lista) }
            }
        }
    }

    fun filtrarModulo(modulo: Modulo?) = _estado.update { it.copy(filtroModulo = modulo) }

    fun filtrarCurso(curso: Int?) = _estado.update { it.copy(filtroCurso = curso) }

    fun filtrarDificultad(dificultad: Int?) = _estado.update { it.copy(filtroDificultad = dificultad) }

    fun cambiarEstado(id: Long, estado: EstadoActividad) {
        viewModelScope.launch {
            when (val resultado = actividades.cambiarEstado(id, estado)) {
                is ResultadoAccion.Exito -> _estado.update {
                    it.copy(mensaje = "Estado actualizado", mensajeEsError = false)
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
                CatalogoViewModel(contenedor.authRepository, contenedor.actividadRepository)
            }
        }
    }
}
