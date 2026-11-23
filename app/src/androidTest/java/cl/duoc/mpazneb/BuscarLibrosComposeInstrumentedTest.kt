package cl.duoc.mpazneb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.LibroEncontrado
import cl.duoc.mpazneb.data.repository.OpenLibraryRepository
import cl.duoc.mpazneb.device.AlmacenImagenes
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.support.AuthInstrumentada
import cl.duoc.mpazneb.ui.screens.admin.BuscarLibrosSection
import cl.duoc.mpazneb.ui.screens.admin.EditorActividadScreen
import cl.duoc.mpazneb.ui.theme.MpazNebTheme
import cl.duoc.mpazneb.viewmodel.EditorActividadUiState
import cl.duoc.mpazneb.viewmodel.EditorActividadViewModel
import io.mockk.mockk
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BuscarLibrosComposeInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun esperarCargaViewModel(vm: EditorActividadViewModel) {
        val listo = CountDownLatch(1)
        val hilo = Thread {
            while (vm.estado.value.cargando) {
                Thread.sleep(20)
            }
            listo.countDown()
        }
        hilo.start()
        listo.await(5, TimeUnit.SECONDS)
    }

    private fun viewModelEditor(): EditorActividadViewModel {
        val openLibrary = mockk<OpenLibraryRepository>(relaxed = true)
        val actividades = mockk<ActividadRepository>(relaxed = true)
        val almacen = mockk<AlmacenImagenes>(relaxed = true)
        return EditorActividadViewModel(
            0L,
            AuthInstrumentada.admin(),
            actividades,
            almacen,
            openLibrary,
        )
    }

    @Test
    fun editor_muestraBuscarLibrosSoloEnLectura() {
        val vm = viewModelEditor()
        composeRule.setContent {
            MpazNebTheme {
                EditorActividadScreen(
                    actividadId = 0,
                    onCreada = {},
                    onEditarPregunta = {},
                    onNuevaPregunta = {},
                    onVolver = {},
                    vm = vm,
                )
            }
        }
        esperarCargaViewModel(vm)
        vm.cambiarModulo(Modulo.LECTURA)
        composeRule.waitForIdle()
        composeRule
            .onNodeWithText("Buscar libros", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()

        vm.cambiarModulo(Modulo.MATEMATICA)
        composeRule.waitForIdle()
        assertEquals(
            0,
            composeRule.onAllNodesWithText("Buscar libros").fetchSemanticsNodes().size,
        )
    }

    @Test
    fun seccion_muestraResultadosDeBusqueda() {
        composeRule.setContent {
            MpazNebTheme {
                BuscarLibrosSection(
                    estado = EditorActividadUiState(
                        consultaLibros = "tomás",
                        librosEncontrados = listOf(
                            LibroEncontrado("El volantín de Tomás", listOf("Francisco Coloane")),
                        ),
                    ),
                    onConsulta = {},
                    onBuscar = {},
                )
            }
        }
        composeRule.onNodeWithText("El volantín de Tomás").assertIsDisplayed()
        composeRule.onNodeWithText("Francisco Coloane").assertIsDisplayed()
    }

    @Test
    fun seccion_muestraMensajeDeError() {
        composeRule.setContent {
            MpazNebTheme {
                BuscarLibrosSection(
                    estado = EditorActividadUiState(
                        mensajeLibros = "Sin conexión. Revisa tu red e inténtalo de nuevo.",
                    ),
                    onConsulta = {},
                    onBuscar = {},
                )
            }
        }
        composeRule
            .onNodeWithText("Sin conexión. Revisa tu red e inténtalo de nuevo.")
            .assertIsDisplayed()
    }

    @Test
    fun seccion_muestraConsultaEnCarga() {
        composeRule.setContent {
            MpazNebTheme {
                BuscarLibrosSection(
                    estado = EditorActividadUiState(
                        consultaLibros = "poesía",
                        buscandoLibros = true,
                    ),
                    onConsulta = {},
                    onBuscar = {},
                )
            }
        }
        composeRule.onNodeWithText("Buscar libros").assertIsDisplayed()
        composeRule.onNodeWithText("poesía").assertIsDisplayed()
    }
}