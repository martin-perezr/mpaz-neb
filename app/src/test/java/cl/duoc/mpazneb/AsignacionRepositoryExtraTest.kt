package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.local.ActividadDao
import cl.duoc.mpazneb.data.remote.ApiService
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.data.repository.ResultadoCodigo
import cl.duoc.mpazneb.support.InMemoryAsignacionDao
import cl.duoc.mpazneb.support.PruebaFabrica
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AsignacionRepositoryExtraTest {

    private lateinit var dao: InMemoryAsignacionDao
    private lateinit var api: ApiService
    private lateinit var actividades: ActividadRepository
    private lateinit var repo: AsignacionRepository

    @Before
    fun preparar() {
        dao = InMemoryAsignacionDao()
        dao.estudiantes += PruebaFabrica.estudiante()
        dao.asistentesRegistrados = setOf(PruebaFabrica.ASISTENTE_ID)
        api = mockk()
        val actividadDao = mockk<ActividadDao>(relaxed = true)
        actividades = ActividadRepository(actividadDao, PruebaFabrica.authAsistente(), api)
        repo = AsignacionRepository(dao, PruebaFabrica.authAsistente(), actividades, api) { PruebaFabrica.AHORA }
    }

    @Test
    fun crear_fallaSiIdRemotoNoSeResuelve() = runTest {
        val actividadDao = mockk<ActividadDao>()
        coEvery { actividadDao.obtener(3) } returns PruebaFabrica.actividadLocal(idRemoto = null)
        coEvery { api.listarActividades() } returns emptyList()
        actividades = ActividadRepository(actividadDao, PruebaFabrica.authAsistente(), api)
        repo = AsignacionRepository(dao, PruebaFabrica.authAsistente(), actividades, api) { PruebaFabrica.AHORA }

        val resultado = repo.crear(4, 3)
        assertTrue(resultado is ResultadoAccion.Error)
    }

    @Test
    fun validarCodigo_remoto410() = runTest {
        coEvery { api.validarCodigo(any()) } returns Response.error(410, "".toResponseBody())
        assertEquals(ResultadoCodigo.Vencido, repo.validarCodigo("123456"))
    }

    @Test
    fun validarCodigo_remoto409() = runTest {
        coEvery { api.validarCodigo(any()) } returns Response.error(409, "".toResponseBody())
        val r = repo.validarCodigo("123456")
        assertTrue(r is ResultadoCodigo.Conflicto)
    }

    @Test
    fun guardarObservacion_ok() = runTest {
        dao.insertar(
            cl.duoc.mpazneb.model.Asignacion(
                id = 9, estudianteId = 4, actividadId = 3, asistenteId = 2,
                codigo = "111111", creadaEn = 0, venceEn = 9_000,
            ),
        )
        val resultado = repo.guardarObservacion(9, "Participación", "Texto suficiente para prueba.")
        assertTrue(resultado is ResultadoAccion.Exito)
        assertEquals(1, dao.observacionesList.size)
    }

    @Test
    fun guardarObservacion_rechazaAdmin() = runTest {
        repo = AsignacionRepository(dao, PruebaFabrica.authAdmin(), actividades, api) { PruebaFabrica.AHORA }
        assertTrue(repo.guardarObservacion(1, "Participación", "Texto largo") is ResultadoAccion.Error)
    }

    @Test
    fun crearEstudiante_yAlias() = runTest {
        assertEquals(1L, repo.crearEstudiante("nuevo-01", 2))
        assertTrue(repo.aliasExiste("nuevo-01"))
    }
}