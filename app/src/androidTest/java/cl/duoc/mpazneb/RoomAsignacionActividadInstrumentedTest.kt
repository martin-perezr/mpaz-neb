package cl.duoc.mpazneb

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.Estudiante
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Rol
import cl.duoc.mpazneb.model.Usuario
import cl.duoc.mpazneb.support.BaseRoomInstrumentada
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomAsignacionActividadInstrumentedTest {

    private lateinit var base: cl.duoc.mpazneb.data.local.AppDatabase

    @Before
    fun abrirBaseTemporal() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        base = BaseRoomInstrumentada.crear(context)
    }

    @After
    fun cerrarBaseTemporal() {
        base.close()
    }

    @Test
    fun actividad_actualizaYConsultaIdRemoto() = runBlocking {
        val actividadDao = base.actividadDao()
        val idLocal = actividadDao.insertar(
            Actividad(
                nombre = "El volantín de Tomás",
                modulo = Modulo.LECTURA,
                curso = 2,
                contenido = "Lectura",
                objetivo = "Objetivo de prueba suficientemente largo",
                dificultad = 1,
                instrucciones = "Instrucciones claras",
                intentosPermitidos = 2,
                estado = EstadoActividad.DISPONIBLE,
            ),
        )
        assertNull(actividadDao.obtenerPorIdRemoto(1009))

        actividadDao.actualizarIdRemoto(idLocal, 1009)
        val enlazada = actividadDao.obtenerPorIdRemoto(1009)

        assertNotNull(enlazada)
        assertEquals(idLocal, enlazada!!.id)
        assertEquals(1009L, enlazada.idRemoto)
    }

    @Test
    fun asignacion_persisteConsultaYVigencia() = runBlocking {
        val actividadDao = base.actividadDao()
        val asignacionDao = base.asignacionDao()
        val usuarioDao = base.usuarioDao()

        val actividadId = actividadDao.insertar(
            Actividad(
                nombre = "Actividad prueba",
                modulo = Modulo.LECTURA,
                curso = 2,
                contenido = "Lectura",
                objetivo = "Objetivo de prueba suficientemente largo",
                dificultad = 1,
                instrucciones = "Instrucciones",
                intentosPermitidos = 2,
                estado = EstadoActividad.DISPONIBLE,
                idRemoto = 1001,
            ),
        )
        val estId = asignacionDao.insertarEstudiante(Estudiante(alias = "est-a", curso = 2))
        val asistenteId = usuarioDao.insertar(
            Usuario(
                alias = "asist.test",
                rol = Rol.ASISTENTE,
                hashClave = "x",
                sal = "y",
            ),
        )
        val ahora = 5_000L
        val idAsignacion = asignacionDao.insertar(
            Asignacion(
                estudianteId = estId,
                actividadId = actividadId,
                asistenteId = asistenteId,
                codigo = "482196",
                creadaEn = 1_000,
                venceEn = 9_000,
                estado = EstadoAsignacion.PENDIENTE,
            ),
        )

        val porCodigo = asignacionDao.buscarPorCodigo("482196")
        assertNotNull(porCodigo)
        assertEquals(idAsignacion, porCodigo!!.id)
        assertEquals(actividadId, porCodigo.actividadId)

        val vigente = asignacionDao.asignacionVigente(estId, actividadId, ahora)
        assertEquals("482196", vigente?.codigo)
        assertEquals(1, asignacionDao.existeAsistente(asistenteId))
    }
}