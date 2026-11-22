package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.local.ActividadDao
import cl.duoc.mpazneb.data.remote.ApiService
import cl.duoc.mpazneb.data.remote.SaludDto
import cl.duoc.mpazneb.data.remote.dto.ActividadDto
import cl.duoc.mpazneb.data.remote.dto.ActividadRequest
import cl.duoc.mpazneb.data.remote.dto.AsignacionDto
import cl.duoc.mpazneb.data.remote.dto.AsignacionRequest
import cl.duoc.mpazneb.data.remote.dto.ValidarCodigoRequest
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.ActividadConConteo
import cl.duoc.mpazneb.model.ActividadConPreguntas
import cl.duoc.mpazneb.model.Alternativa
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Pregunta
import cl.duoc.mpazneb.model.PreguntaConAlternativas
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

/** Servidor simulado: registra qué rutas se llamaron y responde lo que cada prueba configure. */
private class ApiFalsa : ApiService {
    val llamadas = mutableListOf<String>()
    var solicitudRecibida: ActividadRequest? = null
    var alListar: suspend () -> List<ActividadDto> = { emptyList() }
    var alCrear: suspend (ActividadRequest) -> Response<ActividadDto> = { throw IOException("sin configurar") }
    var alEditar: suspend (Long, ActividadRequest) -> Response<ActividadDto> = { _, _ -> throw IOException("sin configurar") }
    var alEliminar: suspend (Long) -> Response<Unit> = { throw IOException("sin configurar") }

    override suspend fun salud(): SaludDto = SaludDto("ok")

    override suspend fun listarActividades(): List<ActividadDto> {
        llamadas += "GET"
        return alListar()
    }

    override suspend fun crearActividad(solicitud: ActividadRequest): Response<ActividadDto> {
        llamadas += "POST"
        solicitudRecibida = solicitud
        return alCrear(solicitud)
    }

    override suspend fun editarActividad(id: Long, solicitud: ActividadRequest): Response<ActividadDto> {
        llamadas += "PUT"
        solicitudRecibida = solicitud
        return alEditar(id, solicitud)
    }

    override suspend fun eliminarActividad(id: Long): Response<Unit> {
        llamadas += "DELETE"
        return alEliminar(id)
    }

    override suspend fun crearAsignacion(solicitud: AsignacionRequest): Response<AsignacionDto> =
        throw NotImplementedError()

    override suspend fun validarCodigo(solicitud: ValidarCodigoRequest): Response<AsignacionDto> =
        throw NotImplementedError()
}

/** Room simulado en memoria: solo lo que usa el repositorio de actividades. */
private class DaoFalso : ActividadDao() {
    val filas = mutableMapOf<Long, Actividad>()
    var preguntas = 0
    var asignaciones = 0

    override fun catalogo(): Flow<List<ActividadConConteo>> = flowOf(emptyList())
    override fun disponibles(modulo: Modulo, curso: Int, dificultad: Int): Flow<List<Actividad>> = flowOf(emptyList())
    override suspend fun obtener(id: Long): Actividad? = filas[id]
    override suspend fun obtenerCompleta(id: Long): ActividadConPreguntas? = null

    override suspend fun insertar(actividad: Actividad): Long {
        val id = (filas.keys.maxOrNull() ?: 0L) + 1
        filas[id] = actividad.copy(id = id)
        return id
    }

    override suspend fun actualizar(actividad: Actividad) {
        filas[actividad.id] = actividad
    }

    override suspend fun cambiarEstado(id: Long, estado: EstadoActividad) {
        filas[id]?.let { filas[id] = it.copy(estado = estado) }
    }

    override fun preguntasDe(actividadId: Long): Flow<List<PreguntaConAlternativas>> = flowOf(emptyList())
    override suspend fun obtenerPregunta(id: Long): PreguntaConAlternativas? = null
    override suspend fun contarPreguntas(actividadId: Long): Int = preguntas
    override suspend fun insertarPregunta(pregunta: Pregunta): Long = 0L
    override suspend fun actualizarPregunta(pregunta: Pregunta) {}
    override suspend fun borrarPregunta(id: Long) {}
    override suspend fun insertarAlternativas(alternativas: List<Alternativa>) {}
    override suspend fun borrarAlternativas(preguntaId: Long) {}

    override suspend fun idsExistentes(): List<Long> = filas.keys.toList()

    override suspend fun upsertActividades(actividades: List<Actividad>) {
        actividades.forEach { filas[it.id] = it }
    }

    override suspend fun contarAsignaciones(actividadId: Long): Int = asignaciones

    override suspend fun borrarActividad(id: Long) {
        filas.remove(id)
    }

    override suspend fun borrarRemotasAusentes(base: Long, idsServidor: List<Long>) {
        if (asignaciones > 0) return
        filas.keys.filter { it > base && it !in idsServidor }.forEach { filas.remove(it) }
    }
}

class ActividadRepositoryTest {

    private val api = ApiFalsa()
    private val dao = DaoFalso()
    private var admin = true
    private val repo = ActividadRepository(dao, api, esAdmin = { admin })

    private fun dto(id: Long, nombre: String = "Sumas", estado: String = "BORRADOR") = ActividadDto(
        id = id, nombre = nombre, modulo = "MATEMATICA", curso = 2, contenido = "Sumas hasta 20",
        objetivo = "Practicar sumas", dificultad = 1, instrucciones = "Resuelve con calma.",
        intentosPermitidos = 2, estado = estado
    )

    private fun local(
        id: Long = 0,
        nombre: String = "Sumas",
        estado: EstadoActividad = EstadoActividad.BORRADOR,
        imagen: String? = null
    ) = Actividad(
        id = id, nombre = nombre, modulo = Modulo.MATEMATICA, curso = 2, contenido = "Sumas hasta 20",
        objetivo = "Practicar sumas", dificultad = 1, instrucciones = "Resuelve con calma.",
        intentosPermitidos = 2, estado = estado, imagenRuta = imagen
    )

    private fun <T> errorHttp(codigo: Int, json: String): Response<T> =
        Response.error(codigo, json.toResponseBody("application/json".toMediaType()))

    // ---------- Carga exitosa ----------

    @Test
    fun cargaExitosaGuardaYConvierteLosDatosDelServidor() = runBlocking<Unit> {
        api.alListar = { listOf(dto(1001, "Remota")) }
        val resultado = repo.refrescarCatalogo()
        assertTrue(resultado is ResultadoAccion.Exito)
        assertEquals("Remota", dao.filas[1001L]?.nombre)
        assertEquals(Modulo.MATEMATICA, dao.filas[1001L]?.modulo)
        assertEquals(EstadoActividad.BORRADOR, dao.filas[1001L]?.estado)
    }

    @Test
    fun unEstadoInvalidoDelServidorEsErrorDeDatosYNoGuardaNada() = runBlocking<Unit> {
        api.alListar = { listOf(dto(1001, estado = "ROTO")) }
        val resultado = repo.refrescarCatalogo()
        assertTrue(resultado is ResultadoAccion.Error)
        assertTrue(dao.filas.isEmpty())
    }

    @Test
    fun unIdRemotoSobreUnaFilaDeLaSemillaNoLaPisa() = runBlocking<Unit> {
        dao.filas[1L] = local(1, "Semilla")
        api.alListar = { listOf(dto(1, "Servidor")) }
        repo.refrescarCatalogo()
        assertEquals("Semilla", dao.filas[1L]?.nombre)
    }

    @Test
    fun alRefrescarSeConservaLaImagenLocal() = runBlocking<Unit> {
        dao.filas[1001L] = local(1001, "Vieja", imagen = "/files/foto.jpg")
        api.alListar = { listOf(dto(1001, "Renombrada")) }
        repo.refrescarCatalogo()
        assertEquals("Renombrada", dao.filas[1001L]?.nombre)
        assertEquals("/files/foto.jpg", dao.filas[1001L]?.imagenRuta)
    }

    @Test
    fun listaVaciaConservaLaSemillaYQuitaLasRemotasQueYaNoEstan() = runBlocking<Unit> {
        dao.filas[1L] = local(1, "Semilla")
        dao.filas[1005L] = local(1005, "Borrada en el servidor")
        api.alListar = { emptyList() }
        repo.refrescarCatalogo()
        assertEquals("Semilla", dao.filas[1L]?.nombre)
        assertNull(dao.filas[1005L])
    }

    // ---------- Rechazo o desconexión al cargar ----------

    @Test
    fun siFallaLaRedAlCargarSeInformaYSeConservaElCatalogo() = runBlocking<Unit> {
        dao.filas[1L] = local(1, "Semilla")
        api.alListar = { throw IOException("sin red") }
        val resultado = repo.refrescarCatalogo()
        assertTrue(resultado is ResultadoAccion.Error)
        assertEquals(1, dao.filas.size)
    }

    // ---------- Crear y editar ----------

    @Test
    fun crearUsaPostYGuardaElIdDelServidor() = runBlocking<Unit> {
        api.alCrear = { Response.success(201, dto(1001, it.nombre)) }
        val resultado = repo.guardar(local(0, "Nueva"))
        assertEquals(ResultadoAccion.Exito(id = 1001), resultado)
        assertEquals(listOf("POST"), api.llamadas)
        assertEquals("Nueva", dao.filas[1001L]?.nombre)
    }

    @Test
    fun editarUnaRemotaUsaPutYNoEnviaLaImagenLocal() = runBlocking<Unit> {
        dao.filas[1001L] = local(1001, "Vieja", imagen = "/files/foto.jpg")
        api.alEditar = { id, solicitud -> Response.success(dto(id, solicitud.nombre)) }
        val resultado = repo.guardar(local(1001, "Nueva", imagen = "/files/foto.jpg"))
        assertEquals(ResultadoAccion.Exito(id = 1001), resultado)
        assertEquals(listOf("PUT"), api.llamadas)
        assertNull(api.solicitudRecibida?.imagenRuta)
        assertEquals("Nueva", dao.filas[1001L]?.nombre)
        assertEquals("/files/foto.jpg", dao.filas[1001L]?.imagenRuta)
    }

    @Test
    fun editarUnaActividadDeLaSemillaNoLlamaAlServidor() = runBlocking<Unit> {
        dao.filas[5L] = local(5, "Semilla")
        val resultado = repo.guardar(local(5, "Semilla editada"))
        assertEquals(ResultadoAccion.Exito(id = 5), resultado)
        assertTrue(api.llamadas.isEmpty())
        assertEquals("Semilla editada", dao.filas[5L]?.nombre)
    }

    @Test
    fun cambiarElEstadoDeUnaRemotaPasaPorElServidor() = runBlocking<Unit> {
        dao.filas[1001L] = local(1001, "Remota")
        dao.preguntas = 1
        api.alEditar = { id, solicitud -> Response.success(dto(id, solicitud.nombre, solicitud.estado)) }
        val resultado = repo.cambiarEstado(1001, EstadoActividad.DISPONIBLE)
        assertTrue(resultado is ResultadoAccion.Exito)
        assertEquals(listOf("PUT"), api.llamadas)
        assertEquals(EstadoActividad.DISPONIBLE, dao.filas[1001L]?.estado)
    }

    // ---------- Rechazo, desconexión y reglas al guardar ----------

    @Test
    fun siElServidorRechazaSeMuestraSuMensajeYRoomNoCambia() = runBlocking<Unit> {
        api.alCrear = { errorHttp(400, "{\"error\":\"Escribe el nombre de la actividad\"}") }
        val resultado = repo.guardar(local(0, "Nueva"))
        assertEquals(ResultadoAccion.Error("Escribe el nombre de la actividad"), resultado)
        assertTrue(dao.filas.isEmpty())
    }

    @Test
    fun siSeCaeLaRedAlEditarSeConservaElEstadoAnterior() = runBlocking<Unit> {
        dao.filas[1001L] = local(1001, "Vieja")
        api.alEditar = { _, _ -> throw IOException("sin red") }
        val resultado = repo.guardar(local(1001, "Nueva"))
        assertTrue(resultado is ResultadoAccion.Error)
        assertEquals("Vieja", dao.filas[1001L]?.nombre)
    }

    @Test
    fun quienNoEsAdminNoLlegaAlServidor() = runBlocking<Unit> {
        admin = false
        val resultado = repo.guardar(local(0, "Nueva"))
        assertEquals(
            ResultadoAccion.Error("Solo la administración pedagógica puede modificar actividades."),
            resultado
        )
        assertTrue(api.llamadas.isEmpty())
    }

    @Test
    fun noSePuedeDejarDisponibleSinPreguntas() = runBlocking<Unit> {
        dao.filas[1001L] = local(1001, "Remota")
        val resultado = repo.guardar(local(1001, "Remota", EstadoActividad.DISPONIBLE))
        assertTrue(resultado is ResultadoAccion.Error)
        assertTrue(api.llamadas.isEmpty())
    }

    // ---------- Borrado ----------

    @Test
    fun elBorradoRetiraLaFilaSoloCuandoElServidorConfirma() = runBlocking<Unit> {
        dao.filas[1001L] = local(1001, "Borrador")
        api.alEliminar = { Response.success(204, Unit) }
        val resultado = repo.eliminar(1001)
        assertEquals(ResultadoAccion.Exito(id = 1001), resultado)
        assertNull(dao.filas[1001L])
    }

    @Test
    fun siElServidorRechazaElBorradoLaFilaSeConserva() = runBlocking<Unit> {
        dao.filas[1001L] = local(1001, "Borrador")
        api.alEliminar = { errorHttp(409, "{\"error\":\"La actividad tiene asignaciones y no se puede eliminar\"}") }
        val resultado = repo.eliminar(1001)
        assertEquals(
            ResultadoAccion.Error("La actividad tiene asignaciones y no se puede eliminar"),
            resultado
        )
        assertEquals("Borrador", dao.filas[1001L]?.nombre)
    }

    @Test
    fun sinConexionElBorradoConservaLaFila() = runBlocking<Unit> {
        dao.filas[1001L] = local(1001, "Borrador")
        api.alEliminar = { throw IOException("sin red") }
        val resultado = repo.eliminar(1001)
        assertTrue(resultado is ResultadoAccion.Error)
        assertEquals("Borrador", dao.filas[1001L]?.nombre)
    }

    @Test
    fun conAsignacionesLocalesNiSiquieraSeLlamaAlServidor() = runBlocking<Unit> {
        dao.filas[1001L] = local(1001, "Borrador")
        dao.asignaciones = 1
        val resultado = repo.eliminar(1001)
        assertTrue(resultado is ResultadoAccion.Error)
        assertTrue(api.llamadas.isEmpty())
        assertEquals("Borrador", dao.filas[1001L]?.nombre)
    }

    @Test
    fun soloSeEliminanBorradoresDelServidor() = runBlocking<Unit> {
        dao.filas[3L] = local(3, "Semilla")
        dao.filas[1002L] = local(1002, "Activa", EstadoActividad.DISPONIBLE)
        assertTrue(repo.eliminar(3) is ResultadoAccion.Error)
        assertTrue(repo.eliminar(1002) is ResultadoAccion.Error)
        assertTrue(api.llamadas.isEmpty())
    }

    // ---------- Reglas puras ----------

    @Test
    fun soloUnIdBajoYExistenteEsConflicto() {
        assertTrue(ActividadRepository.esConflictoDeId(3, setOf(1L, 2L, 3L)))
        assertFalse(ActividadRepository.esConflictoDeId(1001, setOf(1L, 2L, 1001L)))
        assertFalse(ActividadRepository.esConflictoDeId(7, setOf(1L, 2L)))
    }

    @Test
    fun elMensajeDelServidorSeLeeDelJson() {
        assertEquals("Texto", ActividadRepository.mensajeDeError("{\"error\":\"Texto\"}", 400))
        assertEquals(
            "El servidor rechazó el guardado (código 500).",
            ActividadRepository.mensajeDeError(null, 500)
        )
    }
}