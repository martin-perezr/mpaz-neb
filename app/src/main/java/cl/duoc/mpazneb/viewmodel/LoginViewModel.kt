package cl.duoc.mpazneb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazneb.contenedor
import cl.duoc.mpazneb.data.repository.AuthRepository
import cl.duoc.mpazneb.data.repository.ResultadoLogin
import cl.duoc.mpazneb.domain.validation.LoginValidator
import cl.duoc.mpazneb.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val alias: String = "",
    val clave: String = "",
    val mostrarClave: Boolean = false,
    val intentoEnviar: Boolean = false,
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val intentosFallidos: Int = 0,
    val destino: Rol? = null
) {
    private val errores get() = LoginValidator.validar(alias, clave)
    // Los errores se muestran cuando el campo tiene algo escrito o cuando se intentó enviar.
    val errorAlias: String? get() = if (alias.isNotEmpty() || intentoEnviar) errores.alias else null
    val errorClave: String? get() = if (clave.isNotEmpty() || intentoEnviar) errores.clave else null
    val puedeEnviar: Boolean get() = errores.valido && !cargando
}

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    init {
        // Si ya hay una sesión guardada en DataStore, se entra directo.
        viewModelScope.launch {
            auth.sesionActual.first()?.let { sesion -> _estado.update { it.copy(destino = sesion.rol) } }
        }
    }

    fun cambiarAlias(valor: String) = _estado.update { it.copy(alias = valor, mensaje = null) }

    fun cambiarClave(valor: String) = _estado.update { it.copy(clave = valor, mensaje = null) }

    fun alternarClave() = _estado.update { it.copy(mostrarClave = !it.mostrarClave) }

    fun ingresar() {
        _estado.update { it.copy(intentoEnviar = true) }
        val actual = _estado.value
        if (!actual.puedeEnviar) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensaje = null) }
            val resultado = auth.ingresar(actual.alias, actual.clave)
            _estado.update {
                when (resultado) {
                    is ResultadoLogin.Exito -> it.copy(cargando = false, destino = resultado.usuario.rol)
                    ResultadoLogin.CredencialesInvalidas -> it.copy(
                        cargando = false, mensaje = "Alias o contraseña incorrectos",
                        intentosFallidos = it.intentosFallidos + 1
                    )
                    ResultadoLogin.CuentaInactiva -> it.copy(cargando = false, mensaje = "Tu cuenta está desactivada. Habla con UTP.")
                    is ResultadoLogin.Bloqueado -> it.copy(
                        cargando = false, mensaje = "Demasiados intentos. Espera ${resultado.segundos} segundos.",
                        intentosFallidos = it.intentosFallidos + 1
                    )
                }
            }
        }
    }

    fun navegacionHecha() = _estado.update { it.copy(destino = null) }

    companion object {
        val Factory = viewModelFactory {
            initializer { LoginViewModel(contenedor().authRepository) }
        }
    }
}
