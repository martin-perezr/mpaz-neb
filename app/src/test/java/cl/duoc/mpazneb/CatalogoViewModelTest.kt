package cl.duoc.mpazneb

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AuthRepository
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.ActividadConConteo
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.support.PruebaFabrica
import cl.duoc.mpazneb.viewmodel.CatalogoUiState
import cl.duoc.mpazneb.viewmodel.CatalogoViewModel
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
class CatalogoViewModelTest {

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
    fun filtros_visibles_soloCoinciden() {
        val lectura = ActividadConConteo(PruebaFabrica.actividadLocal(), 1)
        val mate = ActividadConConteo(
            PruebaFabrica.actividadLocal().copy(
                nombre = "Sumas",
                modulo = Modulo.MATEMATICA,
                curso = 1,
            ),
            2,
        )
        val estado = CatalogoUiState(
            todas = listOf(lectura, mate),
            filtroModulo = Modulo.LECTURA,
            filtroCurso = 2,
        )
        assertEquals(1, estado.visibles.size)
        assertEquals(Modulo.LECTURA, estado.visibles[0].actividad.modulo)
    }

    @Test
    fun init_sinPermisoSiNoEsAdmin() = runTest {
        val auth = PruebaFabrica.authAsistente()
        val actividades = mockk<ActividadRepository> {
            coEvery { sincronizarCatalogoRemoto() } returns cl.duoc.mpazneb.data.repository.ResultadoAccion.Exito()
            every { catalogo() } returns flowOf(emptyList())
        }
        val vm = CatalogoViewModel(auth, actividades)
        advanceUntilIdle()
        assertTrue(vm.estado.value.sinPermiso)
    }
}