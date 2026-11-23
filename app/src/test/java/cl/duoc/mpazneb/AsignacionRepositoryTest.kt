package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.local.ActividadDao
import cl.duoc.mpazneb.data.remote.ApiService
import cl.duoc.mpazneb.data.remote.dto.AsignacionRequest
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.data.repository.ResultadoCodigo
import cl.duoc.mpazneb.support.InMemoryAsignacionDao
import cl.duoc.mpazneb.support.PruebaFabrica
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AsignacionRepositoryTest {

    private lateinit var dao: InMemoryAsignacionDao
    private lateinit var api: ApiService
    private lateinit var actividadDao: ActividadDao
    private lateinit var actividades: ActividadRepository
    private lateinit var repo: AsignacionRepository

    @Before
    fun preparar() {
        dao = InMemoryAsignacionDao()
        dao.estudiantes += PruebaFabrica.estudiante()
        dao.asistentesRegistrados = setOf(PruebaFabrica.ASISTENTE_ID)

        api = mockk()
        actividadDao = mockk(relaxed = true)
        actividades = ActividadRepository(actividadDao, PruebaFabrica.authAsistente(), api)
    }

  private fun repoConReloj() {
        repo = AsignacionRepository(
            dao,
            PruebaFabrica.authAsistente(),
            actividades,
            api,
        ) { PruebaFabrica.AHORA }
    }

    @Test
    fun crear_guardaCodigoDelServidorConIdLocal() = runTest {
        val actividad = PruebaFabrica.actividadLocal()
        coEvery { actividadDao.obtener(3) } returns actividad
        val dto = PruebaFabrica.dtoAsignacion(codigo = "048213")
        coEvery { api.crearAsignacion(any()) } returns Response.success(dto)

        repoConReloj()
        val resultado = repo.crear(estudianteId = 4, actividadId = 3)

        assertTrue(resultado is ResultadoAccion.Exito)
        val exito = resultado as ResultadoAccion.Exito
        assertEquals("048213", exito.asignacion?.codigo)
        assertEquals(3L, exito.asignacion?.actividadId)
        assertEquals(1, dao.asignaciones.size)
        val solicitud = slot<AsignacionRequest>()
        coVerify { api.crearAsignacion(capture(solicitud)) }
        assertEquals(1009L, solicitud.captured.actividadId)
    }

    @Test
    fun crear_rechazaSiNoHayAsistenteEnSesion() = runTest {
        repo = AsignacionRepository(
            dao,
            PruebaFabrica.authAdmin(),
            actividades,
            api,
        ) { PruebaFabrica.AHORA }

        val resultado = repo.crear(4, 3)
        assertTrue(resultado is ResultadoAccion.Error)
        assertEquals(0, dao.asignaciones.size)
        coVerify(exactly = 0) { api.crearAsignacion(any()) }
    }

    @Test
    fun crear_rechazaActividadNoDisponible() = runTest {
        coEvery { actividadDao.obtener(3) } returns
            PruebaFabrica.actividadLocal(estado = cl.duoc.mpazneb.model.EstadoActividad.BORRADOR)
        repoConReloj()

        val resultado = repo.crear(4, 3)
        assertTrue(resultado is ResultadoAccion.Error)
        coVerify(exactly = 0) { api.crearAsignacion(any()) }
    }

    @Test
    fun crear_rechazaEstudianteConSesionAbierta() = runTest {
        coEvery { actividadDao.obtener(3) } returns PruebaFabrica.actividadLocal()
        dao.insertar(
            cl.duoc.mpazneb.model.Asignacion(
                estudianteId = 4,
                actividadId = 99,
                asistenteId = 2,
                codigo = "111111",
                creadaEn = 0,
                venceEn = 9_999,
            )
        )
        repoConReloj()

        val resultado = repo.crear(4, 3)
        assertTrue(resultado is ResultadoAccion.Error)
        assertTrue((resultado as ResultadoAccion.Error).mensaje.contains("abierta"))
    }

    @Test
    fun crear_conAsignacionVigenteMismaActividadRechazaPorSesionAbierta() = runTest {
        coEvery { actividadDao.obtener(3) } returns PruebaFabrica.actividadLocal()
        dao.insertar(
            cl.duoc.mpazneb.model.Asignacion(
                id = 7,
                estudianteId = 4,
                actividadId = 3,
                asistenteId = 2,
                codigo = "222222",
                creadaEn = 1,
                venceEn = 8_000,
            )
        )
        repoConReloj()

        val resultado = repo.crear(4, 3)
        assertTrue(resultado is ResultadoAccion.Error)
        assertTrue((resultado as ResultadoAccion.Error).mensaje.contains("abierta"))
        coVerify(exactly = 0) { api.crearAsignacion(any()) }
    }

    @Test
    fun crear_mensajeClaroSiElServidorResponde400() = runTest {
        coEvery { actividadDao.obtener(3) } returns PruebaFabrica.actividadLocal()
        coEvery { api.crearAsignacion(any()) } returns Response.error(
            400,
            "".toResponseBody("text/plain".toMediaType()),
        )
        repoConReloj()

        val resultado = repo.crear(4, 3)
        assertTrue(resultado is ResultadoAccion.Error)
        assertEquals("Datos inválidos para la asignación.", (resultado as ResultadoAccion.Error).mensaje)
    }

    @Test
    fun crear_registraErrorRedSiFallaLaConexion() = runTest {
        coEvery { actividadDao.obtener(3) } returns PruebaFabrica.actividadLocal()
        coEvery { api.crearAsignacion(any()) } throws IOException("sin red")
        repoConReloj()

        val resultado = repo.crear(4, 3)
        assertTrue(resultado is ResultadoAccion.Error)
        assertTrue((resultado as ResultadoAccion.Error).mensaje.contains("conexión"))
        assertEquals(0, dao.asignaciones.size)
    }

    @Test
    fun validarCodigo_usaCopiaLocalSinConsultarApi() = runTest {
        dao.insertar(
            cl.duoc.mpazneb.model.Asignacion(
                id = 1,
                estudianteId = 4,
                actividadId = 3,
                asistenteId = 2,
                codigo = "333333",
                creadaEn = 1,
                venceEn = 8_000,
            )
        )
        repoConReloj()

        val resultado = repo.validarCodigo("333333")
        assertTrue(resultado is ResultadoCodigo.Valido)
        coVerify(exactly = 0) { api.validarCodigo(any()) }
    }

    @Test
    fun validarCodigo_remoto404() = runTest {
        coEvery { api.validarCodigo(any()) } returns Response.error(
            404,
            "".toResponseBody("text/plain".toMediaType()),
        )
        repoConReloj()

        assertEquals(ResultadoCodigo.NoExiste, repo.validarCodigo("999999"))
    }

    @Test
    fun validarCodigo_tabletNuevaPersisteConActividadLocalEnlazada() = runTest {
        val local = PruebaFabrica.actividadLocal(idRemoto = null)
        coEvery { actividadDao.obtenerPorIdRemoto(1009) } returns null
        coEvery { api.listarActividades() } returns listOf(PruebaFabrica.actividadRemota())
        coEvery {
            actividadDao.coincidenciasConRemota(
                local.nombre.trim(),
                local.modulo,
                local.curso,
                local.dificultad,
            )
        } returns listOf(local)
        coEvery { actividadDao.actualizarIdRemoto(3, 1009) } returns Unit

        val dto = PruebaFabrica.dtoAsignacion(codigo = "559389")
        coEvery { api.validarCodigo(any()) } returns Response.success(dto)

        repoConReloj()
        val resultado = repo.validarCodigo("559389")

        assertTrue(resultado is ResultadoCodigo.Valido)
        val asignacion = (resultado as ResultadoCodigo.Valido).asignacion
        assertEquals(3L, asignacion.actividadId)
        assertEquals("559389", asignacion.codigo)
        assertEquals(1, dao.asignaciones.size)
        coVerify { actividadDao.actualizarIdRemoto(3, 1009) }
    }

    @Test
    fun validarCodigo_referenciasFaltantesSiNoHayActividadLocal() = runTest {
        coEvery { actividadDao.obtenerPorIdRemoto(1009) } returns null
        coEvery { api.listarActividades() } returns listOf(PruebaFabrica.actividadRemota())
        coEvery { actividadDao.coincidenciasConRemota(any(), any(), any(), any()) } returns emptyList()
        coEvery { api.validarCodigo(any()) } returns Response.success(PruebaFabrica.dtoAsignacion())

        repoConReloj()
        val resultado = repo.validarCodigo("559389")
        assertTrue(resultado is ResultadoCodigo.ReferenciasLocalesFaltantes)
        assertEquals(0, dao.asignaciones.size)
    }

    @Test
    fun validarCodigo_errorRedAlConsultarCatalogo() = runTest {
        coEvery { actividadDao.obtenerPorIdRemoto(1009) } returns null
        coEvery { api.listarActividades() } throws IOException("timeout")
        coEvery { api.validarCodigo(any()) } returns Response.success(PruebaFabrica.dtoAsignacion())

        repoConReloj()
        val resultado = repo.validarCodigo("559389")
        assertTrue(resultado is ResultadoCodigo.ErrorRed)
    }
}