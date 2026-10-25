package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.repository.ResultadoCodigo
import cl.duoc.mpazneb.data.repository.clasificarCodigo
import cl.duoc.mpazneb.domain.FilaObservacion
import cl.duoc.mpazneb.domain.FilaResultado
import cl.duoc.mpazneb.domain.ResultadoDescriptivo
import cl.duoc.mpazneb.domain.TextoCsv
import cl.duoc.mpazneb.domain.validation.ActividadValidator
import cl.duoc.mpazneb.domain.validation.AsignacionFormValidator
import cl.duoc.mpazneb.domain.validation.CodigoValidator
import cl.duoc.mpazneb.domain.validation.LoginValidator
import cl.duoc.mpazneb.domain.validation.ObservacionValidator
import cl.duoc.mpazneb.domain.validation.PreguntaValidator
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.TipoPregunta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadoresTest {

    @Test
    fun codigoDeSeisNumeros() {
        assertEquals(6, CodigoValidator.LARGO)
        assertNull(CodigoValidator.validar("482196"))
        assertEquals("000007", CodigoValidator.formatear(7))
        assertNotNull(CodigoValidator.validar(""))
        assertNotNull(CodigoValidator.validar("4821"))
        assertNotNull(CodigoValidator.validar("4821967"))
        assertNotNull(CodigoValidator.validar("48219A"))
    }

    @Test
    fun loginRechazaCamposVaciosYClaveCorta() {
        assertNotNull(LoginValidator.validarAlias(""))
        assertNotNull(LoginValidator.validarAlias("ab"))
        assertNull(LoginValidator.validarAlias("utp.demo"))
        assertNotNull(LoginValidator.validarClave("abc"))
        assertNotNull(LoginValidator.validarClave("soloLetras"))
        assertNull(LoginValidator.validarClave("Utp2026"))
    }

    @Test
    fun actividadExigeTextoSoloEnLectura() {
        assertNotNull(ActividadValidator.validarTexto(Modulo.LECTURA, "corto"))
        assertNull(ActividadValidator.validarTexto(Modulo.MATEMATICA, ""))
        assertNotNull(ActividadValidator.validarIntentos("0"))
        assertNull(ActividadValidator.validarIntentos("3"))
        assertNotNull(ActividadValidator.validarCurso(5))
        val errores = ActividadValidator.validar(
            nombre = "El gato",
            modulo = Modulo.LECTURA,
            curso = 1,
            dificultad = 1,
            contenido = "Lectura breve",
            objetivo = "Identificar datos del texto",
            instrucciones = "Lee y responde con calma",
            intentos = "2",
            texto = "Mimí es un gato gris que vive en una casa azul y toma leche."
        )
        assertTrue(errores.valido)
    }

    @Test
    fun preguntaPideDosAlternativasYUnaCorrecta() {
        val (textos, error) = PreguntaValidator.leerAlternativas(listOf("Rojo", "Azul", "", ""))
        assertNull(error)
        assertEquals(listOf("Rojo", "Azul"), textos)
        assertNotNull(PreguntaValidator.leerAlternativas(listOf("Rojo", "", "Azul")).second)
        assertNotNull(PreguntaValidator.validarCorrecta(null, 2))
        assertNotNull(PreguntaValidator.validarProcedimiento(Modulo.MATEMATICA, ""))
        assertNull(PreguntaValidator.validarProcedimiento(Modulo.LECTURA, ""))
        val errores = PreguntaValidator.validar(
            modulo = Modulo.MATEMATICA,
            enunciado = "¿Cuánto es 3 + 2?",
            tipo = TipoPregunta.CALCULO,
            alternativas = listOf("5", "4", "", ""),
            indiceCorrecta = 0,
            retroalimentacion = "3 + 2 = 5",
            pista = "Junta los dos grupos",
            procedimiento = "3 + 2 = 5",
            numeroA = "3",
            numeroB = "2"
        )
        assertTrue(errores.valido)
    }

    @Test
    fun asignacionYObservacion() {
        assertNotNull(AsignacionFormValidator.validarFiltro(null, 2, 1))
        assertNull(AsignacionFormValidator.validarFiltro(Modulo.LECTURA, 2, 1))
        assertNotNull(AsignacionFormValidator.validarEstudiante(null))
        assertNotNull(ObservacionValidator.validarTexto("corta"))
        assertTrue(ObservacionValidator.validar("Participación", "Observación ficticia de prueba.").valido)
        assertNotNull(ObservacionValidator.validar("Diagnóstico", "Esto no debe aceptarse nunca.").categoria)
    }

    @Test
    fun codigoVigenteVencidoYUsado() {
        val base = Asignacion(
            estudianteId = 1,
            actividadId = 1,
            asistenteId = 1,
            codigo = "482196",
            creadaEn = 1_000,
            venceEn = 5_000
        )
        assertTrue(clasificarCodigo(null, 2_000) is ResultadoCodigo.NoExiste)
        assertTrue(clasificarCodigo(base, 2_000) is ResultadoCodigo.Valido)
        assertTrue(clasificarCodigo(base.copy(estado = EstadoAsignacion.EN_DESARROLLO), 2_000) is ResultadoCodigo.Valido)
        assertTrue(clasificarCodigo(base, 6_000) is ResultadoCodigo.Vencido)
        assertTrue(clasificarCodigo(base.copy(estado = EstadoAsignacion.FINALIZADA), 2_000) is ResultadoCodigo.YaUsado)
    }

    @Test
    fun resultadoNoEsNotaYElCsvEscapaComillas() {
        assertTrue(ResultadoDescriptivo.frase(2, 2).contains("No es una nota"))
        assertTrue(ResultadoDescriptivo.frase(1, 3).contains("Logro parcial"))
        val csv = TextoCsv.documento(
            estudiante = "Pudú-12",
            actividad = "El volantín",
            codigo = "482196",
            estado = "Pendiente",
            resultado = "Logro parcial",
            respuestas = listOf(FilaResultado("¿Color?", "Rojo", true, 1)),
            observaciones = listOf(FilaObservacion("Ánimo", "Dijo \"listo\""))
        )
        assertTrue(csv.contains("482196"))
        assertTrue(csv.contains("\"\"listo\"\""))
    }
}
