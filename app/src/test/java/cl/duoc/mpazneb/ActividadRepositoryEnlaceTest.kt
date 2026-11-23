package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.local.ActividadDao
import cl.duoc.mpazneb.data.remote.ApiService
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.ResolucionActividadLocal
import cl.duoc.mpazneb.support.PruebaFabrica
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActividadRepositoryEnlaceTest {

    private val actividadDao = mockk<ActividadDao>()
    private val api = mockk<ApiService>()
    private val repo = ActividadRepository(actividadDao, PruebaFabrica.authAsistente(), api)

    @Test
    fun actividadLocalParaIdRemoto_usaEnlaceExistenteSinRed() = runTest {
        val local = PruebaFabrica.actividadLocal()
        coEvery { actividadDao.obtenerPorIdRemoto(1009) } returns local

        val resolucion = repo.actividadLocalParaIdRemoto(1009)

        assertTrue(resolucion is ResolucionActividadLocal.Lista)
        assertEquals(3L, (resolucion as ResolucionActividadLocal.Lista).actividad.id)
        coVerify(exactly = 0) { api.listarActividades() }
    }

    @Test
    fun actividadLocalParaIdRemoto_enlazaTabletNuevaPorCoincidencia() = runTest {
        val sinEnlace = PruebaFabrica.actividadLocal(idRemoto = null)
        coEvery { actividadDao.obtenerPorIdRemoto(1009) } returns null
        coEvery { api.listarActividades() } returns listOf(PruebaFabrica.actividadRemota())
        coEvery {
            actividadDao.coincidenciasConRemota(
                sinEnlace.nombre.trim(),
                sinEnlace.modulo,
                sinEnlace.curso,
                sinEnlace.dificultad,
            )
        } returns listOf(sinEnlace)
        coEvery { actividadDao.actualizarIdRemoto(3, 1009) } returns Unit

        val resolucion = repo.actividadLocalParaIdRemoto(1009)

        assertTrue(resolucion is ResolucionActividadLocal.Lista)
        assertEquals(1009L, (resolucion as ResolucionActividadLocal.Lista).actividad.idRemoto)
        coVerify { actividadDao.actualizarIdRemoto(3, 1009) }
    }

    @Test
    fun actividadLocalParaIdRemoto_informaErrorDeRed() = runTest {
        coEvery { actividadDao.obtenerPorIdRemoto(any()) } returns null
        coEvery { api.listarActividades() } throws IOException("offline")

        val resolucion = repo.actividadLocalParaIdRemoto(1009)
        assertEquals(ResolucionActividadLocal.ErrorRed, resolucion)
    }
}