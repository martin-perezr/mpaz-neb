package cl.duoc.mpazneb

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.LibroEncontrado
import cl.duoc.mpazneb.data.repository.OpenLibraryRepository
import cl.duoc.mpazneb.device.AlmacenImagenes
import cl.duoc.mpazneb.model.Alternativa
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Pregunta
import cl.duoc.mpazneb.model.PreguntaConAlternativas
import cl.duoc.mpazneb.model.TipoPregunta
import cl.duoc.mpazneb.support.PruebaFabrica
import cl.duoc.mpazneb.viewmodel.EditorActividadUiState
import cl.duoc.mpazneb.viewmodel.EditorActividadViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorActividadViewModelBuscarLibrosTest {

    @get:Rule
    val instant = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun prepararMain() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun limpiarMain() {
        Dispatchers.resetMain()
    }

    private fun editor(
        openLibrary: OpenLibraryRepository,
        actividades: ActividadRepository = mockk(relaxed = true),
        actividadId: Long = 0L,
    ): EditorActividadViewModel {
        val almacen = mockk<AlmacenImagenes>(relaxed = true)
        return EditorActividadViewModel(
            actividadId,
            PruebaFabrica.authAdmin(),
            actividades,
            almacen,
            openLibrary,
        )
    }

    private val preguntaEjemplo = PreguntaConAlternativas(
        pregunta = Pregunta(
            id = 11,
            actividadId = 8,
            orden = 1,
            tipo = TipoPregunta.EXPLICITA,
            enunciado = "¿Quién es el protagonista?",
            retroalimentacion = "Revisa el texto.",
            pista = "Mira el título.",
        ),
        alternativas = listOf(
            Alternativa(id = 1, preguntaId = 11, texto = "Tomás", esCorrecta = true),
        ),
    )

    @Test
    fun buscarLibros_publicaTitulosYAutores() = runTest {
        val openLibrary = mockk<OpenLibraryRepository> {
            coEvery { buscar("volantín") } returns listOf(
                LibroEncontrado("El volantín de Tomás", listOf("Francisco Coloane")),
            )
        }
        val vm = editor(openLibrary)
        advanceUntilIdle()

        vm.cambiarConsultaLibros("volantín")
        vm.buscarLibros()
        advanceUntilIdle()

        assertEquals(1, vm.estado.value.librosEncontrados.size)
        assertEquals("El volantín de Tomás", vm.estado.value.librosEncontrados[0].titulo)
        assertEquals(listOf("Francisco Coloane"), vm.estado.value.librosEncontrados[0].autores)
        assertNull(vm.estado.value.mensajeLibros)
        assertFalse(vm.estado.value.buscandoLibros)
    }

    @Test
    fun buscarLibros_sinResultadosMuestraAviso() = runTest {
        val openLibrary = mockk<OpenLibraryRepository> {
            coEvery { buscar(any()) } returns emptyList()
        }
        val vm = editor(openLibrary)
        advanceUntilIdle()
        vm.cambiarConsultaLibros("xyz inexistente")
        vm.buscarLibros()
        advanceUntilIdle()

        assertTrue(vm.estado.value.librosEncontrados.isEmpty())
        assertEquals("No encontramos libros con esa búsqueda.", vm.estado.value.mensajeLibros)
    }

    @Test
    fun buscarLibros_fallaDeRedMantieneEditorAbierto() = runTest {
        val openLibrary = mockk<OpenLibraryRepository> {
            coEvery { buscar(any()) } throws IOException("offline")
        }
        val vm = editor(openLibrary)
        advanceUntilIdle()
        vm.cambiarNombre("Actividad de lectura")
        vm.cambiarConsultaLibros("poesía")
        vm.buscarLibros()
        advanceUntilIdle()

        assertEquals("Sin conexión. Revisa tu red e inténtalo de nuevo.", vm.estado.value.mensajeLibros)
        assertFalse(vm.estado.value.sinPermiso)
        assertFalse(vm.estado.value.cargando)
        assertEquals("Actividad de lectura", vm.estado.value.nombre)
        assertNull(vm.estado.value.mensaje)
    }

    @Test
    fun buscarLibros_consultaVaciaNoLlamaOpenLibrary() = runTest {
        val openLibrary = mockk<OpenLibraryRepository>(relaxed = true)
        val vm = editor(openLibrary)
        advanceUntilIdle()

        vm.cambiarConsultaLibros("   ")
        vm.buscarLibros()
        advanceUntilIdle()

        assertEquals("Escribe el título o autor que quieres buscar.", vm.estado.value.mensajeLibros)
        coVerify(exactly = 0) { openLibrary.buscar(any()) }
    }

    @Test
    fun buscarLibros_noAlteraPreguntasNiCamposDelFormulario() = runTest {
        val actividad = PruebaFabrica.actividadLocal(id = 8)
        val actividades = mockk<ActividadRepository> {
            coEvery { obtener(8) } returns actividad
            every { preguntasDe(8) } returns flowOf(listOf(preguntaEjemplo))
        }
        val openLibrary = mockk<OpenLibraryRepository> {
            coEvery { buscar(any()) } returns listOf(
                LibroEncontrado("Otro libro", listOf("Autor X")),
            )
        }
        val vm = editor(openLibrary, actividades, actividadId = 8)
        advanceUntilIdle()

        val antes = vm.estado.value
        vm.cambiarConsultaLibros("tomás")
        vm.buscarLibros()
        advanceUntilIdle()
        val despues = vm.estado.value

        assertEquals(antes.nombre, despues.nombre)
        assertEquals(antes.objetivo, despues.objetivo)
        assertEquals(antes.textoCuerpo, despues.textoCuerpo)
        assertEquals(antes.preguntas, despues.preguntas)
        assertEquals(1, despues.preguntas.size)
        assertEquals(11L, despues.preguntas[0].pregunta.id)
    }

    @Test
    fun buscarLibros_consecutivas_actualizaResultadosYMensajes() = runTest {
        val openLibrary = mockk<OpenLibraryRepository>()
        coEvery { openLibrary.buscar("ok") } returns listOf(
            LibroEncontrado("Libro A", listOf("Autor A")),
        )
        coEvery { openLibrary.buscar("vacio") } returns emptyList()
        coEvery { openLibrary.buscar("falla") } throws IOException("red")

        val vm = editor(openLibrary)
        advanceUntilIdle()

        vm.cambiarConsultaLibros("ok")
        vm.buscarLibros()
        advanceUntilIdle()
        assertEquals(1, vm.estado.value.librosEncontrados.size)

        vm.cambiarConsultaLibros("vacio")
        vm.buscarLibros()
        advanceUntilIdle()
        assertTrue(vm.estado.value.librosEncontrados.isEmpty())
        assertEquals("No encontramos libros con esa búsqueda.", vm.estado.value.mensajeLibros)

        vm.cambiarConsultaLibros("falla")
        vm.buscarLibros()
        advanceUntilIdle()
        assertTrue(vm.estado.value.mensajeLibros!!.contains("conexión"))
    }

    @Test
    fun buscarLibros_errorTrasExito_noMuestraResultadosAnteriores() = runTest {
        val actividad = PruebaFabrica.actividadLocal(id = 8)
        val actividades = mockk<ActividadRepository> {
            coEvery { obtener(8) } returns actividad
            every { preguntasDe(8) } returns flowOf(listOf(preguntaEjemplo))
        }
        val openLibrary = mockk<OpenLibraryRepository>()
        coEvery { openLibrary.buscar("ok") } returns listOf(
            LibroEncontrado("Libro A", listOf("Autor A")),
        )
        coEvery { openLibrary.buscar("falla") } throws IOException("red")

        val vm = editor(openLibrary, actividades, actividadId = 8)
        advanceUntilIdle()
        val formulario = vm.estado.value

        vm.cambiarConsultaLibros("ok")
        vm.buscarLibros()
        advanceUntilIdle()
        assertEquals(1, vm.estado.value.librosEncontrados.size)

        vm.cambiarConsultaLibros("falla")
        vm.buscarLibros()
        advanceUntilIdle()

        assertTrue(vm.estado.value.librosEncontrados.isEmpty())
        assertEquals(
            "Sin conexión. Revisa tu red e inténtalo de nuevo.",
            vm.estado.value.mensajeLibros,
        )
        assertFalse(vm.estado.value.buscandoLibros)
        assertFalse(vm.estado.value.sinPermiso)
        assertFalse(vm.estado.value.cargando)
        assertEquals(formulario.nombre, vm.estado.value.nombre)
        assertEquals(formulario.objetivo, vm.estado.value.objetivo)
        assertEquals(formulario.textoCuerpo, vm.estado.value.textoCuerpo)
        assertEquals(formulario.preguntas, vm.estado.value.preguntas)
    }

    @Test
    fun buscarLibros_activaYFinalizaEstadoDeCarga() = runTest {
        val openLibrary = mockk<OpenLibraryRepository> {
            coEvery { buscar(any()) } coAnswers {
                delay(1_000)
                emptyList()
            }
        }
        val vm = editor(openLibrary)
        advanceUntilIdle()
        vm.cambiarConsultaLibros("demora")

        vm.buscarLibros()
        runCurrent()
        assertTrue(vm.estado.value.buscandoLibros)

        advanceUntilIdle()
        assertFalse(vm.estado.value.buscandoLibros)
    }

    @Test
    fun visibilidadBuscarLibros_soloEnModuloLectura_comoEditorScreen() = runTest {
        // Misma condición que EditorActividadScreen (línea ~111): `estado.modulo == Modulo.LECTURA`
        fun mostrarBuscarLibros(estado: EditorActividadUiState) = estado.modulo == Modulo.LECTURA

        assertTrue(mostrarBuscarLibros(EditorActividadUiState(modulo = Modulo.LECTURA)))
        assertFalse(mostrarBuscarLibros(EditorActividadUiState(modulo = Modulo.MATEMATICA)))
        assertFalse(mostrarBuscarLibros(EditorActividadUiState(modulo = null)))
    }
}