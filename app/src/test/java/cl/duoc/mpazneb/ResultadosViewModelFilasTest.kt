package cl.duoc.mpazneb

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.data.sesion.SesionUsuario
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.AsignacionDetalle
import cl.duoc.mpazneb.model.Alternativa
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.ActividadConPreguntas
import cl.duoc.mpazneb.model.Pregunta
import cl.duoc.mpazneb.model.PreguntaConAlternativas
import cl.duoc.mpazneb.model.Respuesta
import cl.duoc.mpazneb.model.Rol
import cl.duoc.mpazneb.model.TipoPregunta
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResultadosViewModelFilasTest {

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
    fun init_conRespuestasArmaFilas() = runTest {
        val auth = mockk<cl.duoc.mpazneb.data.repository.AuthRepository> {
            every { sesionActual } returns flowOf(SesionUsuario(2, "a", Rol.ASISTENTE))
        }
        val actividad = PruebaFabrica.actividadLocal()
        val pregunta = PreguntaConAlternativas(
            Pregunta(10, 3, 1, TipoPregunta.EXPLICITA, "¿Pregunta de prueba larga?", "", ""),
            listOf(Alternativa(100, 10, "Sí", true)),
        )
        val asignaciones = mockk<AsignacionRepository>()
        val actividades = mockk<cl.duoc.mpazneb.data.repository.ActividadRepository>()
        coEvery { asignaciones.detalle(5) } returns flowOf(
            AsignacionDetalle(
                Asignacion(5, 4, 3, 2, "482196", 0, 9_000, EstadoAsignacion.FINALIZADA),
                PruebaFabrica.estudiante(),
                actividad,
            ),
        )
        coEvery { asignaciones.respuestas(5) } returns flowOf(
            listOf(Respuesta(0, 5, 10, 100, true, 1, 0)),
        )
        coEvery { asignaciones.observaciones(5) } returns flowOf(emptyList())
        coEvery { actividades.obtenerCompleta(3) } returns ActividadConPreguntas(actividad, listOf(pregunta))

        val vm = ResultadosViewModel(5, auth, actividades, asignaciones)
        advanceUntilIdle()
        assertTrue(vm.estado.value.respuestas.isNotEmpty())
        assertTrue(vm.estado.value.contenidoCsv.contains("¿Pregunta"))
    }
}