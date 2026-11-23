package cl.duoc.mpazneb

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.mpazneb.data.repository.AuthRepository
import cl.duoc.mpazneb.data.repository.ResultadoLogin
import cl.duoc.mpazneb.data.sesion.SesionUsuario
import cl.duoc.mpazneb.model.Rol
import cl.duoc.mpazneb.model.Usuario
import cl.duoc.mpazneb.viewmodel.LoginViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instant = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun main() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun reset() {
        Dispatchers.resetMain()
    }

    @Test
    fun ingresar_navegaConRolAdmin() = runTest {
        val auth = mockk<AuthRepository> {
            every { sesionActual } returns flowOf(null)
            coEvery { ingresar("utp.demo", "Utp2026") } returns ResultadoLogin.Exito(
                Usuario(1, "utp.demo", Rol.ADMIN, "", "")
            )
        }
        val vm = LoginViewModel(auth)
        advanceUntilIdle()
        vm.cambiarAlias("utp.demo")
        vm.cambiarClave("Utp2026")
        vm.ingresar()
        advanceUntilIdle()
        assertEquals(Rol.ADMIN, vm.estado.value.destino)
    }

    @Test
    fun ingresar_rechazaClaveInvalidaEnUi() = runTest {
        val auth = mockk<AuthRepository> { every { sesionActual } returns flowOf(null) }
        val vm = LoginViewModel(auth)
        advanceUntilIdle()
        vm.cambiarAlias("x")
        vm.cambiarClave("123")
        vm.ingresar()
        advanceUntilIdle()
        assertNull(vm.estado.value.destino)
    }
}