package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.repository.AuthRepository
import cl.duoc.mpazneb.data.repository.ResultadoLogin
import cl.duoc.mpazneb.data.seguridad.Hash
import cl.duoc.mpazneb.data.sesion.SesionUsuario
import cl.duoc.mpazneb.model.Rol
import cl.duoc.mpazneb.model.Usuario
import cl.duoc.mpazneb.support.InMemoryUsuarioDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private val dao = InMemoryUsuarioDao()
    private val sesion = mockk<cl.duoc.mpazneb.data.sesion.SessionManager>(relaxed = true)
    private var ahora = 1_000L

    private lateinit var auth: AuthRepository

    @Before
    fun preparar() {
        every { sesion.sesion } returns flowOf(null)
        auth = AuthRepository(dao, sesion) { ahora }
        val sal = Hash.nuevaSal()
        dao.usuarios += Usuario(
            id = 1,
            alias = "utp.demo",
            rol = Rol.ADMIN,
            hashClave = Hash.sha256("Utp2026", sal),
            sal = sal,
        )
    }

    @Test
    fun ingresar_exitoGuardaSesion() = runTest {
        val resultado = auth.ingresar("utp.demo", "Utp2026")
        assertTrue(resultado is ResultadoLogin.Exito)
        coVerify { sesion.guardar(any()) }
    }

    @Test
    fun ingresar_credencialesInvalidas() = runTest {
        assertEquals(ResultadoLogin.CredencialesInvalidas, auth.ingresar("utp.demo", "mala"))
    }

    @Test
    fun ingresar_bloqueaTrasTresFallos() = runTest {
        repeat(3) { auth.ingresar("utp.demo", "mala") }
        val cuarto = auth.ingresar("utp.demo", "Utp2026")
        assertTrue(cuarto is ResultadoLogin.Bloqueado)
    }
}