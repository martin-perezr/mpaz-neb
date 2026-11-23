package cl.duoc.mpazneb

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.OpenLibraryRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.device.AlmacenImagenes
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.support.PruebaFabrica
import cl.duoc.mpazneb.viewmodel.EditorActividadViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorActividadViewModelGuardarTest {

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

    private fun editor(actividades: ActividadRepository, id: Long = 0): EditorActividadViewModel {
        val almacen = mockk<AlmacenImagenes>(relaxed = true)
        val openLibrary = mockk<OpenLibraryRepository>(relaxed = true)
        return EditorActividadViewModel(id, PruebaFabrica.authAdmin(), actividades, almacen, openLibrary)
    }

    @Test
    fun guardar_nuevaActividadRemota() = runTest {
        val actividades = mockk<ActividadRepository>()
        coEvery { actividades.obtener(0) } returns null
        coEvery { actividades.preguntasDe(0) } returns flowOf(emptyList())
        coEvery { actividades.guardar(any()) } returns ResultadoAccion.Exito(88)
        val vm = editor(actividades)
        advanceUntilIdle()
        vm.cambiarNombre("Actividad de prueba remota")
        vm.cambiarModulo(Modulo.LECTURA)
        vm.cambiarCurso(2)
        vm.cambiarDificultad(1)
        vm.cambiarContenido("Contenido de lectura")
        vm.cambiarObjetivo("Practicar comprensión lectora")
        vm.cambiarInstrucciones("Lee con calma y responde")
        vm.cambiarIntentos("2")
        vm.cambiarTextoCuerpo("Texto de más de veinte caracteres para validar.")
        vm.cambiarEstadoActividad(EstadoActividad.BORRADOR)
        vm.guardar()
        advanceUntilIdle()
        assertEquals(88L, vm.estado.value.id)
        assertEquals("Actividad guardada.", vm.estado.value.mensaje)
    }

    @Test
    fun guardar_rechazaNombreCorto() = runTest {
        val actividades = mockk<ActividadRepository>(relaxed = true)
        coEvery { actividades.obtener(0) } returns null
        coEvery { actividades.preguntasDe(0) } returns flowOf(emptyList())
        val vm = editor(actividades)
        advanceUntilIdle()
        vm.cambiarNombre("abc")
        vm.guardar()
        assertTrue(vm.estado.value.mensajeEsError)
    }

    @Test
    fun cambiarModulo_bloqueadoSiHayPreguntas() = runTest {
        val actividades = mockk<ActividadRepository>()
        val existente = PruebaFabrica.actividadLocal(id = 5)
        coEvery { actividades.obtener(5) } returns existente
        coEvery { actividades.preguntasDe(5) } returns flowOf(
            listOf(
                cl.duoc.mpazneb.model.PreguntaConAlternativas(
                    cl.duoc.mpazneb.model.Pregunta(1, 5, 1, cl.duoc.mpazneb.model.TipoPregunta.EXPLICITA, "x?", "", ""),
                    emptyList(),
                ),
            ),
        )
        val vm = editor(actividades, 5)
        advanceUntilIdle()
        vm.cambiarModulo(Modulo.MATEMATICA)
        assertTrue(vm.estado.value.mensajeEsError)
    }

    @Test
    fun borrarPregunta_muestraExito() = runTest {
        val actividades = mockk<ActividadRepository>()
        coEvery { actividades.obtener(5) } returns PruebaFabrica.actividadLocal(id = 5)
        coEvery { actividades.preguntasDe(5) } returns flowOf(emptyList())
        coEvery { actividades.borrarPregunta(9) } returns ResultadoAccion.Exito(9)
        val vm = editor(actividades, 5)
        advanceUntilIdle()
        vm.borrarPregunta(9)
        advanceUntilIdle()
        assertEquals("Pregunta eliminada.", vm.estado.value.mensaje)
        assertNull(vm.estado.value.navegarAId)
    }
}