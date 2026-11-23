package cl.duoc.mpazneb

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.model.ActividadConPreguntas
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.Alternativa
import cl.duoc.mpazneb.model.Pregunta
import cl.duoc.mpazneb.model.PreguntaConAlternativas
import cl.duoc.mpazneb.model.TipoPregunta
import cl.duoc.mpazneb.support.PruebaFabrica
import cl.duoc.mpazneb.viewmodel.Efecto
import cl.duoc.mpazneb.viewmodel.Fase
import cl.duoc.mpazneb.viewmodel.SesionEstudianteViewModel
import cl.duoc.mpazneb.viewmodel.SesionUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
class SesionEstudianteViewModelTest {

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

  private fun sesionCompleta(): ActividadConPreguntas {
        val actividad = PruebaFabrica.actividadLocal(id = 3).copy(intentosPermitidos = 2)
        val pregunta = PreguntaConAlternativas(
            Pregunta(10, 3, 1, TipoPregunta.EXPLICITA, "¿Quién aparece en el texto?", "", ""),
            listOf(
                Alternativa(100, 10, "Tomás", true),
                Alternativa(101, 10, "Pedro", false),
            ),
        )
        return ActividadConPreguntas(actividad, listOf(pregunta))
    }

    @Test
    fun init_errorSiNoHayPreguntas() = runTest {
        val asignaciones = mockk<AsignacionRepository>()
        val actividades = mockk<ActividadRepository>()
        coEvery { asignaciones.obtener(1) } returns Asignacion(
            1, 4, 3, 2, "111111", 0, 9_000, EstadoAsignacion.PENDIENTE,
        )
        coEvery { actividades.obtenerCompleta(3) } returns ActividadConPreguntas(
            PruebaFabrica.actividadLocal(),
            emptyList(),
        )
        val vm = SesionEstudianteViewModel(1, actividades, asignaciones)
        advanceUntilIdle()
        assertEquals(Fase.ERROR, vm.estado.value.fase)
    }

    @Test
    fun flujo_aciertoYSiguieteFinaliza() = runTest {
        val asignaciones = mockk<AsignacionRepository>(relaxed = true)
        val actividades = mockk<ActividadRepository>()
        coEvery { asignaciones.obtener(1) } returns Asignacion(
            1, 4, 3, 2, "111111", 0, 9_000, EstadoAsignacion.PENDIENTE,
        )
        coEvery { actividades.obtenerCompleta(3) } returns sesionCompleta()
        coEvery { asignaciones.iniciar(1) } returns Unit
        coEvery { asignaciones.registrarRespuesta(1, 10, 100, true, 1) } returns Unit
        coEvery { asignaciones.finalizar(1) } returns Unit

        val vm = SesionEstudianteViewModel(1, actividades, asignaciones)
        advanceUntilIdle()
        assertEquals(Fase.INICIO, vm.estado.value.fase)

        vm.comenzar()
        vm.seleccionar(100)
        vm.pedirConfirmar()
        val efectos = mutableListOf<Efecto>()
        val job = launch { vm.efectos.collect { efectos.add(it) } }
        vm.confirmar()
        advanceUntilIdle()
        assertEquals(Fase.ACIERTO, vm.estado.value.fase)
        assertTrue(efectos.contains(Efecto.ACIERTO))
        vm.siguiente()
        advanceUntilIdle()
        assertEquals(Fase.FIN, vm.estado.value.fase)
        coVerify { asignaciones.finalizar(1) }
        job.cancel()
    }

    @Test
    fun confirmar_casiYReintento() = runTest {
        val asignaciones = mockk<AsignacionRepository>(relaxed = true)
        val actividades = mockk<ActividadRepository>()
        coEvery { asignaciones.obtener(1) } returns Asignacion(
            1, 4, 3, 2, "111111", 0, 9_000, EstadoAsignacion.PENDIENTE,
        )
        coEvery { actividades.obtenerCompleta(3) } returns sesionCompleta()
        coEvery { asignaciones.iniciar(1) } returns Unit
        val vm = SesionEstudianteViewModel(1, actividades, asignaciones)
        advanceUntilIdle()
        vm.comenzar()
        vm.seleccionar(101)
        vm.pedirConfirmar()
        vm.confirmar()
        advanceUntilIdle()
        assertEquals(Fase.CASI, vm.estado.value.fase)
        vm.reintentar()
        assertEquals(Fase.PREGUNTA, vm.estado.value.fase)
        assertEquals(2, vm.estado.value.intento)
    }

    @Test
    fun confirmar_incorrectoReintentaYRevela() = runTest {
        val asignaciones = mockk<AsignacionRepository>(relaxed = true)
        val actividades = mockk<ActividadRepository>()
        val completa = sesionCompleta().copy(
            actividad = sesionCompleta().actividad.copy(intentosPermitidos = 1),
        )
        coEvery { asignaciones.obtener(1) } returns Asignacion(
            1, 4, 3, 2, "111111", 0, 9_000, EstadoAsignacion.PENDIENTE,
        )
        coEvery { actividades.obtenerCompleta(3) } returns completa
        coEvery { asignaciones.iniciar(1) } returns Unit
        coEvery { asignaciones.registrarRespuesta(any(), any(), any(), any(), any()) } returns Unit

        val vm = SesionEstudianteViewModel(1, actividades, asignaciones)
        advanceUntilIdle()
        vm.comenzar()
        vm.seleccionar(101)
        vm.pedirConfirmar()
        vm.confirmar()
        advanceUntilIdle()
        assertEquals(Fase.REVELAR, vm.estado.value.fase)
    }

    @Test
    fun sesionUiState_calculaProgreso() {
        val estado = SesionUiState(
            fase = Fase.PREGUNTA,
            preguntas = listOf(mockk(), mockk()),
            indice = 0,
        )
        assertEquals(0f, estado.progreso)
        assertEquals(2, estado.total)
    }

    private fun SesionEstudianteViewModel.pedirConfirmar() = pedirConfirmacion()
}