package cl.duoc.mpazneb

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AuthRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.TipoPregunta
import cl.duoc.mpazneb.support.PruebaFabrica
import cl.duoc.mpazneb.viewmodel.EditorPreguntaViewModel
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
class EditorPreguntaViewModelTest {

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
    fun init_sinPermisoSiNoEsAdmin() = runTest {
        val auth = mockk<AuthRepository> {
            every { sesionActual } returns flowOf(null)
        }
        val vm = EditorPreguntaViewModel(1, 0, auth, mockk(relaxed = true))
        advanceUntilIdle()
        assertTrue(vm.estado.value.sinPermiso)
    }

    @Test
    fun guardar_rechazaCamposVacios() = runTest {
        val actividades = mockk<ActividadRepository>()
        coEvery { actividades.obtener(1) } returns PruebaFabrica.actividadLocal()
        val vm = EditorPreguntaViewModel(1, 0, PruebaFabrica.authAdmin(), actividades)
        advanceUntilIdle()
        vm.guardar()
        assertTrue(vm.estado.value.mensajeEsError)
    }

    @Test
    fun guardar_exitoConDatosValidos() = runTest {
        val actividades = mockk<ActividadRepository>()
        coEvery { actividades.obtener(1) } returns PruebaFabrica.actividadLocal()
        coEvery { actividades.contarPreguntas(1) } returns 0
        coEvery { actividades.guardarPregunta(any(), any()) } returns ResultadoAccion.Exito(55)
        val vm = EditorPreguntaViewModel(1, 0, PruebaFabrica.authAdmin(), actividades)
        advanceUntilIdle()
        vm.cambiarEnunciado("¿Quién es el protagonista del relato?")
        vm.cambiarTipo(TipoPregunta.EXPLICITA)
        vm.cambiarRetroalimentacion("Revisa el primer párrafo del texto.")
        vm.cambiarPista("Busca el nombre del niño.")
        vm.cambiarAlternativa(0, "Tomás")
        vm.cambiarAlternativa(1, "Pedro")
        vm.marcarCorrecta(0)
        vm.guardar()
        advanceUntilIdle()
        assertTrue(vm.estado.value.guardada)
        assertFalse(vm.estado.value.mensajeEsError)
    }

    @Test
    fun init_cargaPreguntaExistente() = runTest {
        val actividades = mockk<ActividadRepository>()
        coEvery { actividades.obtener(1) } returns PruebaFabrica.actividadLocal(id = 1)
        coEvery { actividades.obtenerPregunta(9) } returns cl.duoc.mpazneb.model.PreguntaConAlternativas(
            cl.duoc.mpazneb.model.Pregunta(9, 1, 1, TipoPregunta.EXPLICITA, "¿Enunciado de prueba?", "ok", "pista"),
            listOf(
                cl.duoc.mpazneb.model.Alternativa(1, 9, "Uno", true),
                cl.duoc.mpazneb.model.Alternativa(2, 9, "Dos", false),
            ),
        )
        val vm = EditorPreguntaViewModel(1, 9, PruebaFabrica.authAdmin(), actividades)
        advanceUntilIdle()
        assertEquals("¿Enunciado de prueba?", vm.estado.value.enunciado)
        assertEquals(0, vm.estado.value.indiceCorrecta)
    }

    @Test
    fun cambiarNumero_filtraSoloDigitos() = runTest {
        val actividades = mockk<ActividadRepository>()
        coEvery { actividades.obtener(1) } returns PruebaFabrica.actividadLocal().copy(modulo = Modulo.MATEMATICA)
        val vm = EditorPreguntaViewModel(1, 0, PruebaFabrica.authAdmin(), actividades)
        advanceUntilIdle()
        vm.cambiarNumeroA("12x3")
        assertEquals("123", vm.estado.value.numeroA)
    }
}