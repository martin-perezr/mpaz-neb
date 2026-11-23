package cl.duoc.mpazneb

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.support.PruebaFabrica
import cl.duoc.mpazneb.viewmodel.AsistenteViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AsistenteViewModelTest {

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
    fun init_sinPermiso() = runTest {
        val auth = mockk<cl.duoc.mpazneb.data.repository.AuthRepository> {
            every { sesionActual } returns flowOf(null)
        }
        val vm = AsistenteViewModel(auth, mockk(), mockk())
        advanceUntilIdle()
        assertTrue(vm.estado.value.sinPermiso)
    }

    @Test
    fun filtros_cargaActividadesDisponibles() = runTest {
        val actividades = mockk<ActividadRepository>()
        val lista = listOf(PruebaFabrica.actividadLocal())
        every { actividades.disponibles(Modulo.LECTURA, 2, 1) } returns flowOf(lista)
        val asignaciones = mockk<AsignacionRepository> {
            every { delAsistente(2) } returns flowOf(emptyList())
        }
        val vm = AsistenteViewModel(PruebaFabrica.authAsistente(), actividades, asignaciones)
        advanceUntilIdle()
        vm.elegirModulo(Modulo.LECTURA)
        vm.elegirCurso(2)
        vm.elegirDificultad(1)
        advanceUntilIdle()
        assertEquals(1, vm.estado.value.actividades.size)
    }

    @Test
    fun cerrarSesion_marcaEstado() = runTest {
        val auth = mockk<cl.duoc.mpazneb.data.repository.AuthRepository> {
            every { sesionActual } returns kotlinx.coroutines.flow.flowOf(
                cl.duoc.mpazneb.data.sesion.SesionUsuario(2, "a", cl.duoc.mpazneb.model.Rol.ASISTENTE),
            )
            coEvery { cerrarSesion() } returns Unit
        }
        val vm = AsistenteViewModel(auth, mockk(relaxed = true), mockk {
            every { delAsistente(2) } returns flowOf(emptyList())
        })
        advanceUntilIdle()
        vm.cerrarSesion()
        advanceUntilIdle()
        assertTrue(vm.estado.value.sesionCerrada)
    }

    @Test
    fun renovarCodigo_muestraErrorDelServidor() = runTest {
        val asignaciones = mockk<AsignacionRepository> {
            every { delAsistente(2) } returns flowOf(emptyList())
            coEvery { regenerarCodigo(9) } returns ResultadoAccion.Error("No disponible")
        }
        val vm = AsistenteViewModel(PruebaFabrica.authAsistente(), mockk(relaxed = true), asignaciones)
        advanceUntilIdle()
        vm.renovarCodigo(9)
        advanceUntilIdle()
        assertTrue(vm.estado.value.mensajeEsError)
    }
}