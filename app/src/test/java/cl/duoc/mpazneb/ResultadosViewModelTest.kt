package cl.duoc.mpazneb

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.data.sesion.SesionUsuario
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.AsignacionDetalle
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.Rol
import cl.duoc.mpazneb.support.PruebaFabrica
import cl.duoc.mpazneb.viewmodel.ResultadosViewModel
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
class ResultadosViewModelTest {

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

    private fun detalle(): AsignacionDetalle {
        val actividad = PruebaFabrica.actividadLocal()
        return AsignacionDetalle(
            Asignacion(5, 4, 3, 2, "482196", 0, 9_000, EstadoAsignacion.FINALIZADA),
            PruebaFabrica.estudiante(),
            actividad,
        )
    }

    @Test
    fun init_sinPermiso() = runTest {
        val auth = mockk<cl.duoc.mpazneb.data.repository.AuthRepository> {
            every { sesionActual } returns flowOf(null)
        }
        val vm = ResultadosViewModel(5, auth, mockk(), mockk())
        advanceUntilIdle()
        assertTrue(vm.estado.value.sinPermiso)
    }

    @Test
    fun init_muestraDetalleYGeneraCsv() = runTest {
        val auth = mockk<cl.duoc.mpazneb.data.repository.AuthRepository> {
            every { sesionActual } returns flowOf(SesionUsuario(2, "asistente", Rol.ASISTENTE))
        }
        val asignaciones = mockk<AsignacionRepository>()
        val actividades = mockk<ActividadRepository>()
        coEvery { asignaciones.detalle(5) } returns flowOf(detalle())
        coEvery { asignaciones.respuestas(5) } returns flowOf(emptyList())
        coEvery { asignaciones.observaciones(5) } returns flowOf(emptyList())
        coEvery { actividades.obtenerCompleta(3) } returns null

        val vm = ResultadosViewModel(5, auth, actividades, asignaciones)
        advanceUntilIdle()
        assertFalse(vm.estado.value.cargando)
        assertEquals("482196", vm.estado.value.codigo)
        assertTrue(vm.estado.value.contenidoCsv.contains("482196"))
    }

    @Test
    fun guardarObservacion_validaCampos() = runTest {
        val auth = mockk<cl.duoc.mpazneb.data.repository.AuthRepository> {
            every { sesionActual } returns flowOf(SesionUsuario(2, "asistente", Rol.ASISTENTE))
        }
        val asignaciones = mockk<AsignacionRepository>()
        coEvery { asignaciones.detalle(5) } returns flowOf(detalle())
        coEvery { asignaciones.respuestas(5) } returns flowOf(emptyList())
        coEvery { asignaciones.observaciones(5) } returns flowOf(emptyList())
        val vm = ResultadosViewModel(5, auth, mockk(relaxed = true), asignaciones)
        advanceUntilIdle()
        vm.guardarObservacion()
        assertTrue(vm.estado.value.mensajeEsError)
    }

    @Test
    fun guardarObservacion_exito() = runTest {
        val auth = mockk<cl.duoc.mpazneb.data.repository.AuthRepository> {
            every { sesionActual } returns flowOf(SesionUsuario(2, "asistente", Rol.ASISTENTE))
        }
        val asignaciones = mockk<AsignacionRepository>()
        coEvery { asignaciones.detalle(5) } returns flowOf(detalle())
        coEvery { asignaciones.respuestas(5) } returns flowOf(emptyList())
        coEvery { asignaciones.observaciones(5) } returns flowOf(emptyList())
        coEvery { asignaciones.guardarObservacion(5, "Participación", any()) } returns ResultadoAccion.Exito()
        val vm = ResultadosViewModel(5, auth, mockk(relaxed = true), asignaciones)
        advanceUntilIdle()
        vm.cambiarCategoria("Participación")
        vm.cambiarTexto("Observación ficticia suficientemente larga.")
        vm.guardarObservacion()
        advanceUntilIdle()
        assertEquals("Observación ficticia guardada.", vm.estado.value.mensaje)
    }
}