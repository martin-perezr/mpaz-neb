package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.remote.dto.ActividadDto
import cl.duoc.mpazneb.data.remote.dto.ActividadRequest
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import retrofit2.Response

fun Actividad.aSolicitudRemota(): ActividadRequest = ActividadRequest(
    nombre = nombre.trim(),
    modulo = modulo.name,
    curso = curso,
    contenido = contenido.trim(),
    objetivo = objetivo.trim(),
    dificultad = dificultad,
    instrucciones = instrucciones.trim(),
    intentosPermitidos = intentosPermitidos,
    estado = estado.name,
    textoTitulo = textoTitulo,
    textoCuerpo = textoCuerpo,
    imagenRuta = imagenRuta,
)

fun ActividadDto.aActividadLocal(base: Actividad? = null): Actividad {
    val moduloLocal = runCatching { Modulo.valueOf(modulo) }.getOrElse { base?.modulo ?: Modulo.LECTURA }
    val estadoLocal = runCatching { EstadoActividad.valueOf(estado) }.getOrElse { base?.estado ?: EstadoActividad.BORRADOR }
    return Actividad(
        id = base?.id ?: 0L,
        idRemoto = id,
        nombre = nombre,
        modulo = moduloLocal,
        curso = curso,
        contenido = contenido,
        objetivo = objetivo,
        dificultad = dificultad,
        instrucciones = instrucciones,
        intentosPermitidos = intentosPermitidos,
        estado = estadoLocal,
        textoTitulo = textoTitulo,
        textoCuerpo = textoCuerpo,
        imagenRuta = base?.imagenRuta?.takeIf { it.isNotBlank() } ?: imagenRuta,
    )
}

internal fun mensajeHttpActividad(respuesta: Response<*>, defecto: String): String = when (respuesta.code()) {
    400 -> "El servidor rechazó los datos de la actividad."
    404 -> "La actividad no existe en el servidor."
    409 -> "No se puede eliminar: revisa asignaciones o el estado en el servidor."
    else -> defecto
}