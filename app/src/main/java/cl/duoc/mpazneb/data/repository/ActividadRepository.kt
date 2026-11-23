package cl.duoc.mpazneb.data.repository
import android.util.Log
import cl.duoc.mpazneb.data.local.ActividadDao
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.ActividadConConteo
import cl.duoc.mpazneb.model.ActividadConPreguntas
import cl.duoc.mpazneb.model.Alternativa
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Pregunta
import cl.duoc.mpazneb.model.PreguntaConAlternativas
import cl.duoc.mpazneb.model.Rol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import cl.duoc.mpazneb.data.remote.ApiService
import cl.duoc.mpazneb.data.remote.dto.ActividadDto
import java.io.IOException
import retrofit2.Response



class ActividadRepository(
    private val dao: ActividadDao,
    private val auth: AuthRepository,
    private val apiService: ApiService
) {

    fun catalogo(): Flow<List<ActividadConConteo>> = dao.catalogo()

    fun disponibles(modulo: Modulo, curso: Int, dificultad: Int): Flow<List<Actividad>> =
        dao.disponibles(modulo, curso, dificultad)

    suspend fun obtener(id: Long): Actividad? = dao.obtener(id)

    suspend fun obtenerCompleta(id: Long): ActividadConPreguntas? = dao.obtenerCompleta(id)

    fun preguntasDe(actividadId: Long): Flow<List<PreguntaConAlternativas>> = dao.preguntasDe(actividadId)

    suspend fun obtenerPregunta(id: Long): PreguntaConAlternativas? = dao.obtenerPregunta(id)

    suspend fun contarPreguntas(actividadId: Long): Int = dao.contarPreguntas(actividadId)

    suspend fun obtenerPorIdRemoto(idRemoto: Long): Actividad? = dao.obtenerPorIdRemoto(idRemoto)

    /**
     * Para H4: enlaza una actividad del servidor con su copia local (p. ej. tablet recién instalada).
     */
    suspend fun actividadLocalParaIdRemoto(idRemoto: Long): ResolucionActividadLocal {
        dao.obtenerPorIdRemoto(idRemoto)?.let { return ResolucionActividadLocal.Lista(it) }
        val remotas = try {
            apiService.listarActividades()
        } catch (e: IOException) {
            Log.e("MPAZ_API", "Error al consultar catálogo remoto", e)
            return ResolucionActividadLocal.ErrorRed
        } catch (e: Exception) {
            Log.e("MPAZ_API", "Error inesperado en catálogo remoto", e)
            return ResolucionActividadLocal.ErrorRemoto
        }
        val remota = remotas.firstOrNull { it.id == idRemoto }
            ?: return ResolucionActividadLocal.SinActividadEnCatalogo
        val modulo = runCatching { Modulo.valueOf(remota.modulo) }.getOrNull()
            ?: return ResolucionActividadLocal.SinCoincidenciaLocal
        val locales = dao.coincidenciasConRemota(
            remota.nombre.trim(),
            modulo,
            remota.curso,
            remota.dificultad
        )
        return when (val enlace = enlazarActividadRemota(idRemoto, remota, locales)) {
            is EnlaceActividadRemota.Ambigua -> ResolucionActividadLocal.Ambigua
            is EnlaceActividadRemota.SinCoincidencia -> ResolucionActividadLocal.SinCoincidenciaLocal
            is EnlaceActividadRemota.Enlazada -> {
                dao.actualizarIdRemoto(enlace.local.id, idRemoto)
                ResolucionActividadLocal.Lista(enlace.local.copy(idRemoto = idRemoto))
            }
        }
    }

    /**
     * Busca en Spring Boot el identificador remoto de una actividad local.
     * Nunca asume que el id local coincide con el remoto.
     */
    suspend fun idRemotoDe(actividad: Actividad): ResultadoAccion {
        actividad.idRemoto?.let { return ResultadoAccion.Exito(id = it) }
        return try {
            val remotas = apiService.listarActividades()
            val coincidencia = remotas.firstOrNull { remota ->
                remota.nombre.equals(actividad.nombre.trim(), ignoreCase = true) &&
                    remota.modulo == actividad.modulo.name &&
                    remota.curso == actividad.curso &&
                    remota.dificultad == actividad.dificultad
            }
            if (coincidencia == null) {
                ResultadoAccion.Error(
                    "Esa actividad no está en el servidor. Publica o sincroniza el catálogo UTP primero."
                )
            } else {
                dao.actualizarIdRemoto(actividad.id, coincidencia.id)
                ResultadoAccion.Exito(id = coincidencia.id)
            }
        } catch (e: IOException) {
            Log.e("MPAZ_API", "Error al consultar actividades remotas", e)
            val detalle = e.message.orEmpty()
            val mensaje = when {
                detalle.contains("Cleartext", ignoreCase = true) ->
                    "La app no puede usar HTTP al servidor local. Reinstala la variante Debug."
                detalle.contains("failed to connect", ignoreCase = true) ||
                    detalle.contains("Connection refused", ignoreCase = true) ||
                    detalle.contains("ECONNREFUSED", ignoreCase = true) ->
                    "No hay conexión con el servidor. Comprueba que Spring Boot esté encendido."
                else ->
                    "No se pudo leer la respuesta del servidor. Comprueba Spring Boot y reintenta."
            }
            ResultadoAccion.Error(mensaje)
        } catch (e: Exception) {
            Log.e("MPAZ_API", "Error inesperado al consultar actividades remotas: ${e.javaClass.simpleName}", e)
            ResultadoAccion.Error("No se pudo consultar el catálogo del servidor.")
        }
    }

    /**
     * Trae el catálogo remoto y lo fusiona con Room sin duplicar filas con `idRemoto`.
     * Las preguntas permanecen solo en la copia local.
     */
    suspend fun sincronizarCatalogoRemoto(): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        return try {
            val remotas = apiService.listarActividades()
            for (remota in remotas) {
                fusionarRemotaEnRoom(remota)
            }
            ResultadoAccion.Exito()
        } catch (e: IOException) {
            Log.e("MPAZ_API", "Error al sincronizar catálogo", e)
            ResultadoAccion.Error("No hay conexión con el servidor. Comprueba Spring Boot y la red.")
        } catch (e: Exception) {
            Log.e("MPAZ_API", "Error inesperado al sincronizar catálogo", e)
            ResultadoAccion.Error("No se pudo sincronizar el catálogo remoto.")
        }
    }

    /** Inserta o actualiza en Spring Boot y luego persiste en Room con `idRemoto`. */
    suspend fun guardar(actividad: Actividad): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        if (actividad.estado == EstadoActividad.DISPONIBLE && preguntasParaActivar(actividad) == 0) {
            return ResultadoAccion.Error("Agrega al menos una pregunta antes de dejar la actividad disponible.")
        }
        val remota = when (val remoto = publicarEnServidor(actividad)) {
            is ResultadoRemoto.Exito -> remoto.dto
            is ResultadoRemoto.Error -> return ResultadoAccion.Error(remoto.mensaje)
        }
        val fusionada = remota.aActividadLocal(actividad)
        val id = if (fusionada.id == 0L) {
            dao.insertar(fusionada)
        } else {
            dao.actualizar(fusionada)
            fusionada.id
        }
        return ResultadoAccion.Exito(id = id)
    }

    suspend fun cambiarEstado(id: Long, estado: EstadoActividad): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        if (estado == EstadoActividad.DISPONIBLE && dao.contarPreguntas(id) == 0) {
            return ResultadoAccion.Error("Agrega al menos una pregunta antes de activar la actividad.")
        }
        val actual = dao.obtener(id) ?: return ResultadoAccion.Error("No se encontró la actividad.")
        val actualizada = actual.copy(estado = estado)
        val remota = when (val remoto = publicarEnServidor(actualizada)) {
            is ResultadoRemoto.Exito -> remoto.dto
            is ResultadoRemoto.Error -> return ResultadoAccion.Error(remoto.mensaje)
        }
        dao.actualizar(remota.aActividadLocal(actualizada))
        dao.cambiarEstado(id, estado)
        return ResultadoAccion.Exito(id = id)
    }

    /** Elimina en el servidor y, si responde 204, borra la copia local (incluye preguntas en cascada). */
    suspend fun eliminarBorrador(idLocal: Long): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        val local = dao.obtener(idLocal) ?: return ResultadoAccion.Error("No se encontró la actividad.")
        val idRemoto = local.idRemoto
            ?: return ResultadoAccion.Error("Publica la actividad en el servidor antes de eliminarla.")
        if (local.estado != EstadoActividad.BORRADOR) {
            return ResultadoAccion.Error("Solo se pueden eliminar borradores.")
        }
        return try {
            val respuesta = apiService.eliminarActividad(idRemoto)
            when {
                respuesta.isSuccessful -> {
                    dao.eliminarActividad(idLocal)
                    ResultadoAccion.Exito(id = idLocal)
                }
                else -> ResultadoAccion.Error(
                    mensajeHttpActividad(respuesta, "El servidor no permitió eliminar la actividad.")
                )
            }
        } catch (e: IOException) {
            Log.e("MPAZ_API", "Error al eliminar actividad remota", e)
            ResultadoAccion.Error("No hay conexión con el servidor.")
        }
    }

    suspend fun guardarPregunta(pregunta: Pregunta, alternativas: List<Alternativa>): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        val id = dao.guardarPreguntaCompleta(pregunta, alternativas)
        return ResultadoAccion.Exito(id = id)
    }

    suspend fun borrarPregunta(id: Long): ResultadoAccion {
        val prohibido = exigirAdmin()
        if (prohibido != null) return ResultadoAccion.Error(prohibido)
        dao.borrarPregunta(id)
        return ResultadoAccion.Exito(id = id)
    }

    private suspend fun preguntasParaActivar(actividad: Actividad): Int {
        if (actividad.id == 0L) return 0
        return dao.contarPreguntas(actividad.id)
    }

    private suspend fun publicarEnServidor(actividad: Actividad): ResultadoRemoto {
        val solicitud = actividad.aSolicitudRemota()
        return try {
            val respuesta = if (actividad.idRemoto != null) {
                apiService.editarActividad(actividad.idRemoto, solicitud)
            } else {
                apiService.crearActividad(solicitud)
            }
            dtoDesdeRespuesta(respuesta)
        } catch (e: IOException) {
            Log.e("MPAZ_API", "Error al publicar actividad", e)
            ResultadoRemoto.Error("No hay conexión con el servidor. Comprueba Spring Boot y reintenta.")
        }
    }

    private fun dtoDesdeRespuesta(respuesta: Response<ActividadDto>): ResultadoRemoto {
        val cuerpo = respuesta.body()
        if (respuesta.isSuccessful && cuerpo != null) {
            return ResultadoRemoto.Exito(cuerpo)
        }
        return ResultadoRemoto.Error(
            mensajeHttpActividad(respuesta, "El servidor rechazó la actividad.")
        )
    }

    private suspend fun fusionarRemotaEnRoom(remota: ActividadDto) {
        dao.obtenerPorIdRemoto(remota.id)?.let { local ->
            dao.actualizar(remota.aActividadLocal(local))
            return
        }
        val modulo = runCatching { Modulo.valueOf(remota.modulo) }.getOrNull() ?: return
        val locales = dao.coincidenciasConRemota(
            remota.nombre.trim(),
            modulo,
            remota.curso,
            remota.dificultad,
        )
        when (val enlace = enlazarActividadRemota(remota.id, remota, locales)) {
            is EnlaceActividadRemota.Enlazada -> {
                val fusionada = remota.aActividadLocal(enlace.local)
                dao.actualizarIdRemoto(enlace.local.id, remota.id)
                dao.actualizar(fusionada.copy(id = enlace.local.id))
            }
            is EnlaceActividadRemota.SinCoincidencia -> {
                dao.insertar(remota.aActividadLocal())
            }
            is EnlaceActividadRemota.Ambigua -> Unit
        }
    }

    /** Regla de negocio: solo administración modifica actividades. */
    private suspend fun exigirAdmin(): String? {
        val sesion = auth.sesionActual.first()
        if (sesion?.rol == Rol.ADMIN) return null
        return "Solo la administración pedagógica puede modificar actividades."
    }
}

sealed interface ResolucionActividadLocal {
    data class Lista(val actividad: Actividad) : ResolucionActividadLocal
    data object SinCoincidenciaLocal : ResolucionActividadLocal
    data object SinActividadEnCatalogo : ResolucionActividadLocal
    data object Ambigua : ResolucionActividadLocal
    data object ErrorRed : ResolucionActividadLocal
    data object ErrorRemoto : ResolucionActividadLocal
}

private sealed interface ResultadoRemoto {
    data class Exito(val dto: ActividadDto) : ResultadoRemoto
    data class Error(val mensaje: String) : ResultadoRemoto
}

sealed interface EnlaceActividadRemota {
    data class Enlazada(val local: Actividad) : EnlaceActividadRemota
    data object SinCoincidencia : EnlaceActividadRemota
    data object Ambigua : EnlaceActividadRemota
}

/** Elige la actividad local única que corresponde a los datos remotos (sin duplicar filas). */
internal fun enlazarActividadRemota(
    idRemoto: Long,
    remota: ActividadDto,
    locales: List<Actividad>
): EnlaceActividadRemota {
    if (locales.isEmpty()) return EnlaceActividadRemota.SinCoincidencia
    val candidatos = locales.filter { local ->
        local.idRemoto == null || local.idRemoto == idRemoto
    }
    return when {
        candidatos.size == 1 -> EnlaceActividadRemota.Enlazada(candidatos[0])
        candidatos.isEmpty() -> EnlaceActividadRemota.SinCoincidencia
        else -> EnlaceActividadRemota.Ambigua
    }
}
