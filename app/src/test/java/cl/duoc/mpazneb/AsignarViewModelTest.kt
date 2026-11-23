package cl.duoc.mpazneb

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.support.PruebaFabrica
import cl.duoc.mpazneb.viewmodel.AsignarViewModel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AsignarViewModelTest {

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
    fun init_marcaSinPermisoSiNoEsAsistente() = runTest {
        val actividades = mockk<ActividadRepository>()
        val asignaciones = mockk<AsignacionRepository>()

        val vm = AsignarViewModel(3, PruebaFabrica.authAdmin(), actividades, asignaciones)
        advanceUntilIdle()

        assertTrue(vm.estado.value.sinPermiso)
        assertFalse(vm.estado.value.cargando)
    }

    @Test
    fun asignar_exponeCodigoTrasExito() = runTest {
        val actividad = PruebaFabrica.actividadLocal()
        val actividades = mockk<ActividadRepository> {
            coEvery { obtener(3) } returns actividad
        }
        val asignaciones = mockk<AsignacionRepository> {
            every { estudiantesPorCurso(2) } returns flowOf(listOf(PruebaFabrica.estudiante()))
            coEvery { crear(4, 3) } returns ResultadoAccion.Exito(
                id = 1,
                asignacion = Asignacion(
                    id = 1,
                    estudianteId = 4,
                    actividadId = 3,
                    asistenteId = 2,
                    codigo = "048213",
                    creadaEn = 1,
                    venceEn = 9_000,
                ),
            )
        }

        val vm = AsignarViewModel(3, PruebaFabrica.authAsistente(), actividades, asignaciones)
        advanceUntilIdle()

        vm.elegirEstudiante(4)
        vm.asignar()
        advanceUntilIdle()

        assertEquals("048213", vm.estado.value.codigo)
        assertFalse(vm.estado.value.mensajeEsError)
        assertFalse(vm.estado.value.asignando)
    }

    @Test
    fun asignar_muestraErrorDelRepositorio() = runTest {
        val actividades = mockk<ActividadRepository> {
            coEvery { obtener(3) } returns PruebaFabrica.actividadLocal()
        }
        val asignaciones = mockk<AsignacionRepository> {
            every { estudiantesPorCurso(2) } returns flowOf(listOf(PruebaFabrica.estudiante()))
            coEvery { crear(4, 3) } returns ResultadoAccion.Error("Ese estudiante ya tiene una actividad abierta.")
        }

        val vm = AsignarViewModel(3, PruebaFabrica.authAsistente(), actividades, asignaciones)
        advanceUntilIdle()
        vm.elegirEstudiante(4)
        vm.asignar()
        advanceUntilIdle()

        assertTrue(vm.estado.value.mensajeEsError)
        assertEquals("Ese estudiante ya tiene una actividad abierta.", vm.estado.value.mensaje)
    }
}