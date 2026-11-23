package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.local.ActividadDao
import cl.duoc.mpazneb.data.remote.ApiService
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.support.PruebaFabrica
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActividadRepositoryAdminTest {

    private val dao = mockk<ActividadDao>(relaxed = true)
    private val api = mockk<ApiService>(relaxed = true)
    private val repo = ActividadRepository(dao, PruebaFabrica.authAdmin(), api)

    @Test
    fun guardar_rechazaSinPreguntasSiQuedaDisponible() = runTest {
        coEvery { dao.contarPreguntas(5) } returns 0
        val actividad = PruebaFabrica.actividadLocal(id = 5, estado = EstadoActividad.DISPONIBLE)
        val resultado = repo.guardar(actividad)
        assertTrue(resultado is ResultadoAccion.Error)
    }

    @Test
    fun guardar_insertaActividadNuevaConPreguntas() = runTest {
        coEvery { api.crearActividad(any()) } returns retrofit2.Response.success(PruebaFabrica.actividadRemota())
        coEvery { dao.insertar(any()) } returns 99L
        val actividad = PruebaFabrica.actividadLocal(id = 0, idRemoto = null, estado = EstadoActividad.BORRADOR)
        val resultado = repo.guardar(actividad)
        assertTrue(resultado is ResultadoAccion.Exito)
        assertEquals(99L, (resultado as ResultadoAccion.Exito).id)
        coVerify { dao.insertar(any()) }
    }

    @Test
    fun cambiarEstado_requierePreguntasParaActivar() = runTest {
        coEvery { dao.contarPreguntas(5) } returns 0
        val resultado = repo.cambiarEstado(5, EstadoActividad.DISPONIBLE)
        assertTrue(resultado is ResultadoAccion.Error)
    }
}