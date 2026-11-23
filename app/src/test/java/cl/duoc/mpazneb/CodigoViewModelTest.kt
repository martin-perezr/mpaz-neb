package cl.duoc.mpazneb

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.data.repository.ResultadoCodigo
import cl.duoc.mpazneb.support.PruebaFabrica
import cl.duoc.mpazneb.viewmodel.CodigoViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CodigoViewModelTest {

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
    fun entrar_navegaConAsignacionValida() = runTest {
        val asignaciones = mockk<AsignacionRepository> {
            coEvery { validarCodigo("123456") } returns ResultadoCodigo.Valido(
                cl.duoc.mpazneb.model.Asignacion(
                    id = 88,
                    estudianteId = 4,
                    actividadId = 3,
                    asistenteId = 2,
                    codigo = "123456",
                    creadaEn = 1,
                    venceEn = 9_000,
                ),
            )
        }
        val vm = CodigoViewModel(asignaciones)
        "123456".forEach(vm::tecla)
        vm.entrar()
        advanceUntilIdle()

        assertEquals(88L, vm.estado.value.asignacionId)
        assertFalse(vm.estado.value.verificando)
    }

    @Test
    fun pegarDesdePortapapeles_rellenaCodigo() = runTest {
        val vm = CodigoViewModel(mockk(relaxed = true))
        vm.pegarDesdePortapapeles("  482196  ")
        assertEquals("482196", vm.estado.value.codigo)
    }

    @Test
    fun entrar_muestraNoExiste() = runTest {
        val asignaciones = mockk<AsignacionRepository> {
            coEvery { validarCodigo(any()) } returns ResultadoCodigo.NoExiste
        }
        val vm = CodigoViewModel(asignaciones)
        "111111".forEach(vm::tecla)
        vm.entrar()
        advanceUntilIdle()
        assertTrue(vm.estado.value.mensaje!!.contains("no está"))
    }

    @Test
    fun entrar_incrementaErroresSiElCodigoVencio() = runTest {
        val asignaciones = mockk<AsignacionRepository> {
            coEvery { validarCodigo(any()) } returns ResultadoCodigo.Vencido
        }
        val vm = CodigoViewModel(asignaciones)
        "654321".forEach(vm::tecla)
        vm.entrar()
        advanceUntilIdle()

        assertEquals(1, vm.estado.value.errores)
        assertTrue(vm.estado.value.mensaje!!.contains("venció"))
    }
}