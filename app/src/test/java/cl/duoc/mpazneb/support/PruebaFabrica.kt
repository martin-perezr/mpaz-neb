package cl.duoc.mpazneb.support

import cl.duoc.mpazneb.data.remote.dto.ActividadDto
import cl.duoc.mpazneb.data.remote.dto.AsignacionDto
import cl.duoc.mpazneb.data.sesion.SesionUsuario
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Estudiante
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Rol
import io.mockk.every
import io.mockk.mockk
import cl.duoc.mpazneb.data.repository.AuthRepository
import kotlinx.coroutines.flow.flowOf

object PruebaFabrica {
    const val AHORA = 5_000L
    const val ASISTENTE_ID = 2L

    fun actividadLocal(
        id: Long = 3,
        idRemoto: Long? = 1009,
        estado: EstadoActividad = EstadoActividad.DISPONIBLE,
    ) = Actividad(
        id = id,
        nombre = "El volantín de Tomás",
        modulo = Modulo.LECTURA,
        curso = 2,
        contenido = "Lectura",
        objetivo = "Objetivo de prueba suficientemente largo",
        dificultad = 1,
        instrucciones = "Instrucciones claras para el estudiante",
        intentosPermitidos = 2,
        estado = estado,
        textoTitulo = "El volantín de Tomás",
        textoCuerpo = "Texto remoto más largo de veinte caracteres.",
        idRemoto = idRemoto,
    )

    fun actividadRemota(idRemoto: Long = 1009) = ActividadDto(
        id = idRemoto,
        nombre = "El volantín de Tomás",
        modulo = "LECTURA",
        curso = 2,
        contenido = "Lectura",
        objetivo = "Objetivo de prueba suficientemente largo",
        dificultad = 1,
        instrucciones = "Instrucciones claras",
        intentosPermitidos = 2,
        estado = "DISPONIBLE",
        textoTitulo = "El volantín de Tomás",
        textoCuerpo = "Texto remoto más largo de veinte caracteres.",
        imagenRuta = null,
    )

    fun dtoAsignacion(
        codigo: String = "559389",
        actividadRemotaId: Long = 1009,
        estudianteId: Long = 4,
    ) = AsignacionDto(
        id = 10,
        estudianteId = estudianteId,
        actividadId = actividadRemotaId,
        asistenteId = ASISTENTE_ID,
        codigo = codigo,
        creadaEn = 1_000,
        venceEn = 9_000,
        estado = "PENDIENTE",
    )

    fun estudiante(id: Long = 4, curso: Int = 2) = Estudiante(id = id, alias = "est-$id", curso = curso)

    fun authAsistente(id: Long = ASISTENTE_ID): AuthRepository =
        mockk {
            every { sesionActual } returns flowOf(SesionUsuario(id, "asistente", Rol.ASISTENTE))
        }

    fun authSinSesion(): AuthRepository =
        mockk {
            every { sesionActual } returns flowOf(null)
        }

    fun authAdmin(): AuthRepository =
        mockk {
            every { sesionActual } returns flowOf(SesionUsuario(9, "admin", Rol.ADMIN))
        }
}