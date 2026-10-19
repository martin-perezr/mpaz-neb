package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.UsuarioDao
import cl.duoc.mpazneb.data.seguridad.Hash
import cl.duoc.mpazneb.data.sesion.SesionUsuario
import cl.duoc.mpazneb.data.sesion.SessionManager
import cl.duoc.mpazneb.model.Usuario
import kotlinx.coroutines.flow.Flow

sealed interface ResultadoLogin {
    data class Exito(val usuario: Usuario) : ResultadoLogin
    data object CredencialesInvalidas : ResultadoLogin
    data object CuentaInactiva : ResultadoLogin
    data class Bloqueado(val segundos: Int) : ResultadoLogin
}

class AuthRepository(
    private val dao: UsuarioDao,
    private val sesion: SessionManager,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    private var fallidos = 0
    private var bloqueadoHasta = 0L

    val sesionActual: Flow<SesionUsuario?> = sesion.sesion

    /** Regla de negocio: 3 intentos fallidos bloquean el ingreso por 1 minuto. */
    suspend fun ingresar(alias: String, clave: String): ResultadoLogin {
        val ahora = reloj()
        if (ahora < bloqueadoHasta) {
            return ResultadoLogin.Bloqueado(((bloqueadoHasta - ahora) / 1000).toInt() + 1)
        }
        val usuario = dao.buscarPorAlias(alias.trim())
        if (usuario == null || Hash.sha256(clave, usuario.sal) != usuario.hashClave) {
            fallidos++
            if (fallidos >= 3) {
                fallidos = 0
                bloqueadoHasta = ahora + 60_000
                return ResultadoLogin.Bloqueado(60)
            }
            return ResultadoLogin.CredencialesInvalidas
        }
        if (!usuario.activo) return ResultadoLogin.CuentaInactiva
        fallidos = 0
        dao.registrarAcceso(usuario.id, ahora)
        sesion.guardar(usuario)
        return ResultadoLogin.Exito(usuario)
    }

    suspend fun cerrarSesion() = sesion.cerrar()
}
