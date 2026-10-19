package cl.duoc.mpazneb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazneb.contenedor
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.data.repository.ResultadoCodigo
import cl.duoc.mpazneb.domain.validation.CodigoValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CodigoUiState(
    val codigo: String = "",
    val mensaje: String? = null,
    val errores: Int = 0,
    val verificando: Boolean = false,
    val asignacionId: Long? = null
) {
    val completo: Boolean get() = codigo.length == CodigoValidator.LARGO
}

class CodigoViewModel(private val asignaciones: AsignacionRepository) : ViewModel() {

    private val _estado = MutableStateFlow(CodigoUiState())
    val estado: StateFlow<CodigoUiState> = _estado.asStateFlow()

    fun tecla(digito: Char) = _estado.update {
        if (it.codigo.length >= CodigoValidator.LARGO) it else it.copy(codigo = it.codigo + digito, mensaje = null)
    }

    fun borrar() = _estado.update { it.copy(codigo = it.codigo.dropLast(1), mensaje = null) }

    fun entrar() {
        val codigo = _estado.value.codigo
        CodigoValidator.validar(codigo)?.let { error ->
            _estado.update { it.copy(mensaje = error, errores = it.errores + 1) }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(verificando = true) }
            val mensaje = when (val r = asignaciones.validarCodigo(codigo)) {
                is ResultadoCodigo.Valido -> {
                    _estado.update { it.copy(verificando = false, asignacionId = r.asignacion.id) }
                    return@launch
                }
                ResultadoCodigo.NoExiste -> "Ese código no está. Pídele ayuda a tu asistente."
                ResultadoCodigo.Vencido -> "Ese código ya venció. Pide uno nuevo."
                ResultadoCodigo.YaUsado -> "Ese código ya se usó. Pide uno nuevo."
            }
            _estado.update { it.copy(verificando = false, mensaje = mensaje, errores = it.errores + 1) }
        }
    }

    fun navegacionHecha() = _estado.update { CodigoUiState() }

    companion object {
        val Factory = viewModelFactory {
            initializer { CodigoViewModel(contenedor().asignacionRepository) }
        }
    }
}
