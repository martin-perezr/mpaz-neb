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
import io.mockk.slot
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class ActividadRepositoryCrudRemotoTest {

    private val dao = mockk<ActividadDao>(relaxed = true)
    private val api = mockk<ApiService>()
    private val repo = ActividadRepository(dao, PruebaFabrica.authAdmin(), api)

    @Test
    fun guardar_creaRemotoYGuardaIdRemoto() = runTest {
        val nueva = PruebaFabrica.actividadLocal(id = 0, idRemoto = null, estado = EstadoActividad.BORRADOR)
        val remota = PruebaFabrica.actividadRemota(idRemoto = 77)
        coEvery { api.crearActividad(any()) } returns Response.success(remota)
        coEvery { dao.insertar(any()) } returns 12L

        val resultado = repo.guardar(nueva)

        assertTrue(resultado is ResultadoAccion.Exito)
        val captura = slot<cl.duoc.mpazneb.model.Actividad>()
        coVerify { dao.insertar(capture(captura)) }
        assertEquals(77L, captura.captured.idRemoto)
    }

    @Test
    fun guardar_actualizaRemotoSiYaTieneId() = runTest {
        val local = PruebaFabrica.actividadLocal(id = 5, idRemoto = 1009, estado = EstadoActividad.BORRADOR)
        coEvery { api.editarActividad(1009, any()) } returns Response.success(PruebaFabrica.actividadRemota())
        coEvery { dao.contarPreguntas(5) } returns 1

        val resultado = repo.guardar(local)

        assertTrue(resultado is ResultadoAccion.Exito)
        coVerify { api.editarActividad(1009, any()) }
        coVerify { dao.actualizar(any()) }
    }

    @Test
    fun guardar_noPersisteSiServidorRechaza() = runTest {
        val local = PruebaFabrica.actividadLocal(id = 0, idRemoto = null, estado = EstadoActividad.BORRADOR)
        coEvery { api.crearActividad(any()) } returns Response.error(
            400,
            "{\"error\":\"nombre\"}".toResponseBody(),
        )

        val resultado = repo.guardar(local)

        assertTrue(resultado is ResultadoAccion.Error)
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    @Test
    fun guardar_sinConexionNoGuardaLocal() = runTest {
        val local = PruebaFabrica.actividadLocal(id = 0, idRemoto = null, estado = EstadoActividad.BORRADOR)
        coEvery { api.crearActividad(any()) } throws IOException("offline")

        assertTrue(repo.guardar(local) is ResultadoAccion.Error)
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    @Test
    fun sincronizar_actualizaExistentePorIdRemotoSinDuplicar() = runTest {
        val local = PruebaFabrica.actividadLocal()
        coEvery { api.listarActividades() } returns listOf(PruebaFabrica.actividadRemota())
        coEvery { dao.obtenerPorIdRemoto(1009) } returns local

        assertTrue(repo.sincronizarCatalogoRemoto() is ResultadoAccion.Exito)
        coVerify { dao.actualizar(any()) }
        coVerify(exactly = 0) { dao.insertar(any()) }
    }

    @Test
    fun sincronizar_insertaRemotaNuevaSinCoincidenciaLocal() = runTest {
        coEvery { api.listarActividades() } returns listOf(
            PruebaFabrica.actividadRemota().copy(nombre = "Nueva remota"),
        )
        coEvery { dao.obtenerPorIdRemoto(any()) } returns null
        coEvery { dao.coincidenciasConRemota(any(), any(), any(), any()) } returns emptyList()
        coEvery { dao.insertar(any()) } returns 1L

        assertTrue(repo.sincronizarCatalogoRemoto() is ResultadoAccion.Exito)
        coVerify { dao.insertar(any()) }
    }

    @Test
    fun sincronizar_enlazaLocalSinIdRemoto() = runTest {
        val sinRemoto = PruebaFabrica.actividadLocal(idRemoto = null)
        coEvery { api.listarActividades() } returns listOf(PruebaFabrica.actividadRemota())
        coEvery { dao.obtenerPorIdRemoto(1009) } returns null
        coEvery { dao.coincidenciasConRemota(any(), any(), any(), any()) } returns listOf(sinRemoto)

        assertTrue(repo.sincronizarCatalogoRemoto() is ResultadoAccion.Exito)
        coVerify { dao.actualizarIdRemoto(3, 1009) }
    }

    @Test
    fun eliminar_okRemotoBorraLocal() = runTest {
        val local = PruebaFabrica.actividadLocal(id = 8, estado = EstadoActividad.BORRADOR)
        coEvery { dao.obtener(8) } returns local
        coEvery { api.eliminarActividad(1009) } returns Response.success(Unit)

        assertTrue(repo.eliminarBorrador(8) is ResultadoAccion.Exito)
        coVerify { dao.eliminarActividad(8) }
    }

    @Test
    fun eliminar_conflictServidorNoBorraLocal() = runTest {
        val local = PruebaFabrica.actividadLocal(id = 8, estado = EstadoActividad.BORRADOR)
        coEvery { dao.obtener(8) } returns local
        coEvery { api.eliminarActividad(1009) } returns Response.error(409, "{}".toResponseBody())

        assertTrue(repo.eliminarBorrador(8) is ResultadoAccion.Error)
        coVerify(exactly = 0) { dao.eliminarActividad(any()) }
    }

    @Test
    fun eliminar_sinIdRemotoInformaError() = runTest {
        val local = PruebaFabrica.actividadLocal(idRemoto = null, estado = EstadoActividad.BORRADOR)
        coEvery { dao.obtener(3) } returns local

        assertTrue(repo.eliminarBorrador(3) is ResultadoAccion.Error)
        coVerify(exactly = 0) { api.eliminarActividad(any()) }
    }

    @Test
    fun cambiarEstado_sincronizaConServidor() = runTest {
        val local = PruebaFabrica.actividadLocal(estado = EstadoActividad.BORRADOR)
        coEvery { dao.obtener(3) } returns local
        coEvery { dao.contarPreguntas(3) } returns 2
        coEvery { api.editarActividad(any(), any()) } returns Response.success(
            PruebaFabrica.actividadRemota().copy(estado = "DISPONIBLE"),
        )

        val resultado = repo.cambiarEstado(3, EstadoActividad.DISPONIBLE)
        assertTrue(resultado is ResultadoAccion.Exito)
        coVerify { dao.cambiarEstado(3, EstadoActividad.DISPONIBLE) }
    }
}