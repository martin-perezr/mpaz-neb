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
    fun cambiarEstado_muestraErrorRemoto() = runTest {
        val actividades = mockk<ActividadRepository> {
            coEvery { sincronizarCatalogoRemoto() } returns cl.duoc.mpazneb.data.repository.ResultadoAccion.Exito()
            every { catalogo() } returns flowOf(emptyList())
            coEvery { cambiarEstado(3, cl.duoc.mpazneb.model.EstadoActividad.DISPONIBLE) } returns
                cl.duoc.mpazneb.data.repository.ResultadoAccion.Error("Sin red")
        }
        val vm = CatalogoViewModel(PruebaFabrica.authAdmin(), actividades)
        advanceUntilIdle()
        vm.cambiarEstado(3, cl.duoc.mpazneb.model.EstadoActividad.DISPONIBLE)
        advanceUntilIdle()
        assertTrue(vm.estado.value.mensajeEsError)
    }

    @Test
    fun confirmarEliminar_muestraExito() = runTest {
        val auth = PruebaFabrica.authAdmin()
        val actividades = mockk<ActividadRepository> {
            coEvery { sincronizarCatalogoRemoto() } returns cl.duoc.mpazneb.data.repository.ResultadoAccion.Exito()
            every { catalogo() } returns flowOf(emptyList())
            coEvery { eliminarBorrador(8) } returns cl.duoc.mpazneb.data.repository.ResultadoAccion.Exito(8)
        }
        val vm = CatalogoViewModel(auth, actividades)
        advanceUntilIdle()
        vm.solicitarEliminar(8, "Borrador prueba")
        vm.confirmarEliminar()
        advanceUntilIdle()
        assertEquals(null, vm.estado.value.eliminarPendienteId)
    }

    @Test
    fun cerrarMensaje_y_sesion() = runTest {
        val auth = mockk<AuthRepository> {
            every { sesionActual } returns flowOf(
                cl.duoc.mpazneb.data.sesion.SesionUsuario(1, "utp", cl.duoc.mpazneb.model.Rol.ADMIN),
            )
            coEvery { cerrarSesion() } returns Unit
        }
        val actividades = mockk<ActividadRepository> {
            coEvery { sincronizarCatalogoRemoto() } returns cl.duoc.mpazneb.data.repository.ResultadoAccion.Exito()
            every { catalogo() } returns flowOf(emptyList())
        }
        val vm = CatalogoViewModel(auth, actividades)
        advanceUntilIdle()
        vm.cerrarMensaje()
        vm.cerrarSesion()
        advanceUntilIdle()
        assertTrue(vm.estado.value.sesionCerrada)
    }

    @Test
    fun init_muestraErrorDeSync() = runTest {
        val actividades = mockk<ActividadRepository> {
            coEvery { sincronizarCatalogoRemoto() } returns
                cl.duoc.mpazneb.data.repository.ResultadoAccion.Error("Sin servidor")
            every { catalogo() } returns flowOf(emptyList())
        }
        val vm = CatalogoViewModel(PruebaFabrica.authAdmin(), actividades)
        advanceUntilIdle()
        assertTrue(vm.estado.value.mensajeEsError)
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