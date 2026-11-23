package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.remote.dto.AsignacionDto
import cl.duoc.mpazneb.data.remote.dto.ActividadDto
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.data.repository.EnlaceActividadRemota
import cl.duoc.mpazneb.data.repository.ResultadoCodigo
import cl.duoc.mpazneb.data.repository.asignacionLocalDesdeRemota
import cl.duoc.mpazneb.data.repository.clasificarCodigo
import cl.duoc.mpazneb.data.repository.enlazarActividadRemota
import cl.duoc.mpazneb.data.repository.interpretarValidacionHttp
import cl.duoc.mpazneb.domain.CodigosDemostracion
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Actividad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AsignacionesYCodigosTest {

    @Test
    fun crearAsignacionConservaCodigoDelServidor() {
        val dto = AsignacionDto(1, 5, 1001, 2, "048213", 1_000, 2_000, "PENDIENTE")
        val local = asignacionLocalDesdeRemota(dto, actividadLocalId = 3)
        assertEquals("048213", local.codigo)
        assertEquals(3L, local.actividadId)
        assertNotEquals(dto.actividadId, local.actividadId)
    }

    @Test
    fun codigoValidoUsaIdLocal() {
        val dto = AsignacionDto(1, 5, 1001, 2, "555555", 1_000, 9_000, "PENDIENTE")
        val local = asignacionLocalDesdeRemota(dto, actividadLocalId = 7)
        val resultado = clasificarCodigo(local.copy(id = 42), 5_000)
        assertTrue(resultado is ResultadoCodigo.Valido)
        assertEquals(7L, (resultado as ResultadoCodigo.Valido).asignacion.actividadId)
        assertEquals(42L, resultado.asignacion.id)
    }

    @Test
    fun codigoInexistenteYVencido() {
        assertEquals(ResultadoCodigo.NoExiste, interpretarValidacionHttp(404))
        assertEquals(ResultadoCodigo.Vencido, interpretarValidacionHttp(410))
        assertTrue(interpretarValidacionHttp(409) is ResultadoCodigo.Conflicto)
    }

    @Test
    fun conexionFallidaTieneMensajeClaro() {
        val red = ResultadoCodigo.ErrorRed("No hay conexión con el servidor. Comprueba la red y vuelve a intentar.")
        assertTrue(red.mensaje.contains("conexión"))
    }

    @Test
    fun codigoRepetidoUsaMismaAsignacion() {
        val primero = Asignacion(
            id = 50,
            estudianteId = 5,
            actividadId = 7,
            asistenteId = 2,
            codigo = "666666",
            creadaEn = 1_000,
            venceEn = 9_000
        )
        val segundoIntento = clasificarCodigo(primero, 5_000)
        assertTrue(segundoIntento is ResultadoCodigo.Valido)
        assertEquals(50L, (segundoIntento as ResultadoCodigo.Valido).asignacion.id)
    }

    @Test
    fun limiteDeVencimiento() {
        val base = Asignacion(
            estudianteId = 1,
            actividadId = 1,
            asistenteId = 1,
            codigo = "123456",
            creadaEn = 0,
            venceEn = 10_000
        )
        assertTrue(clasificarCodigo(base, 9_999) is ResultadoCodigo.Valido)
        assertTrue(clasificarCodigo(base, 10_000) is ResultadoCodigo.Valido)
        assertTrue(clasificarCodigo(base, 10_001) is ResultadoCodigo.Vencido)
        assertEquals(AsignacionRepository.DURACION_CODIGO_MS, 30 * 60 * 1000L)
    }

    @Test
    fun tabletNuevaEnlazaActividadSinIdRemoto() {
        val remota = ActividadDto(
            id = 1009,
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
            imagenRuta = null
        )
        val localSinEnlace = Actividad(
            id = 3,
            nombre = "El volantín de Tomás",
            modulo = Modulo.LECTURA,
            curso = 2,
            contenido = "Lectura",
            objetivo = "Objetivo de prueba suficientemente largo",
            dificultad = 1,
            instrucciones = "Instrucciones claras para el estudiante",
            intentosPermitidos = 2,
            estado = EstadoActividad.DISPONIBLE,
            idRemoto = null
        )
        val enlace = enlazarActividadRemota(1009, remota, listOf(localSinEnlace))
        assertTrue(enlace is EnlaceActividadRemota.Enlazada)
        assertEquals(3L, (enlace as EnlaceActividadRemota.Enlazada).local.id)

        val dto = AsignacionDto(10, 4, 1009, 2, "559389", 1_000, 9_000, "PENDIENTE")
        val asignacion = asignacionLocalDesdeRemota(dto, actividadLocalId = 3)
        assertEquals(3L, asignacion.actividadId)
        assertNotEquals(1009L, asignacion.actividadId)
    }

    @Test
    fun correspondenciaActividadesRemotas() {
        val localSemilla = Actividad(
            id = 1,
            nombre = "El volantín de Tomás",
            modulo = Modulo.LECTURA,
            curso = 2,
            contenido = "Lectura",
            objetivo = "Objetivo de prueba suficientemente largo",
            dificultad = 1,
            instrucciones = "Instrucciones claras para el estudiante",
            intentosPermitidos = 2,
            estado = EstadoActividad.DISPONIBLE,
            idRemoto = null
        )
        val enlazada = Actividad(
            id = 2,
            nombre = "Copia en servidor",
            modulo = Modulo.LECTURA,
            curso = 2,
            contenido = "Lectura",
            objetivo = "Objetivo de prueba suficientemente largo",
            dificultad = 1,
            instrucciones = "Instrucciones claras para el estudiante",
            intentosPermitidos = 2,
            estado = EstadoActividad.DISPONIBLE,
            idRemoto = 1001
        )
        val porRemoto = listOf(localSemilla, enlazada).firstOrNull { it.idRemoto == 1001L }
        assertEquals(2L, porRemoto?.id)
        assertNull(listOf(localSemilla).firstOrNull { it.idRemoto == 1001L })
        assertTrue(CodigosDemostracion.esDeSemilla("482196"))
        assertEquals(1001L, enlazada.idRemoto)
        assertNotEquals(localSemilla.id, enlazada.idRemoto)
    }
}