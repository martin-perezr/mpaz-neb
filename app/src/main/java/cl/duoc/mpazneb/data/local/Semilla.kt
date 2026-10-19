package cl.duoc.mpazneb.data.local

import androidx.room.withTransaction
import cl.duoc.mpazneb.data.seguridad.Hash
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.Alternativa
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.Estudiante
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Pregunta
import cl.duoc.mpazneb.model.Respuesta
import cl.duoc.mpazneb.model.Rol
import cl.duoc.mpazneb.model.TipoPregunta
import cl.duoc.mpazneb.model.Usuario

/**
 * Datos de prueba 100 % ficticios. Cuentas:
 *  - utp.demo / Utp2026        (Docente / UTP)
 *  - asistente.aula2 / Aula2026 (Asistente de aula)
 * Código de estudiante de prueba: 4821 (Pudú-12, "El volantín de Tomás").
 */
object Semilla {

    suspend fun poblar(db: AppDatabase) = db.withTransaction {
        val usuarios = db.usuarioDao()
        val actividades = db.actividadDao()
        val asignaciones = db.asignacionDao()

        usuarios.insertar(usuario("utp.demo", "Utp2026", Rol.ADMIN))
        val asistenteId = usuarios.insertar(usuario("asistente.aula2", "Aula2026", Rol.ASISTENTE))

        val alumnos = listOf(
            "Zorro-07" to 2, "Pudú-12" to 2, "Cóndor-03" to 2, "Huemul-21" to 2,
            "Chinchilla-05" to 1, "Puma-09" to 3, "Llama-14" to 4
        ).associate { (alias, curso) -> alias to asignaciones.insertarEstudiante(Estudiante(alias = alias, curso = curso)) }

        // ---------- Lectura ----------
        val volantin = actividades.insertar(
            Actividad(
                nombre = "El volantín de Tomás", modulo = Modulo.LECTURA, curso = 2,
                contenido = "Comprensión lectora",
                objetivo = "Extraer información explícita e implícita de un texto narrativo breve.",
                dificultad = 1, instrucciones = "Lee el texto con calma. Después responde 3 preguntas.",
                intentosPermitidos = 2, estado = EstadoActividad.DISPONIBLE,
                textoTitulo = "El volantín de Tomás",
                textoCuerpo = "Era septiembre y el cielo estaba lleno de volantines. Tomás armó uno rojo con papel de diario " +
                    "y palitos de coligüe. Su abuela le prestó un carrete de hilo. Cuando el viento sopló fuerte, " +
                    "el volantín subió tan alto que parecía un punto. Tomás sonrió y gritó: —¡Mira, abuela, toca las nubes!"
            )
        )
        pregunta(actividades, volantin, 1, TipoPregunta.EXPLICITA, "¿De qué color era el volantín de Tomás?",
            listOf("Rojo", "Azul", "Verde", "Amarillo"), 0,
            "¡Muy bien! El texto dice que armó uno rojo.", "Busca la parte donde Tomás arma su volantín.")
        pregunta(actividades, volantin, 2, TipoPregunta.IMPLICITA, "¿Cómo se sintió Tomás al final?",
            listOf("Contento", "Asustado", "Enojado"), 0,
            "¡Muy bien! Sonrió y gritó: eso muestra que estaba contento.", "Fíjate en lo que hizo Tomás cuando el volantín subió.")
        pregunta(actividades, volantin, 3, TipoPregunta.VOCABULARIO, "En el texto, la palabra “carrete” quiere decir…",
            listOf("Un rollo para enrollar hilo", "Una fiesta", "Un pájaro"), 0,
            "¡Muy bien! La abuela le prestó un carrete de hilo.", "Lee otra vez: “un carrete de hilo”. ¿Qué cosa tiene hilo?")

        val pudu = actividades.insertar(
            Actividad(
                nombre = "El pudú perdido", modulo = Modulo.LECTURA, curso = 2,
                contenido = "Inferencias", objetivo = "Inferir causas y sentimientos de un personaje.",
                dificultad = 2, instrucciones = "Lee la historia de Lolo y responde 2 preguntas.",
                intentosPermitidos = 2, estado = EstadoActividad.BORRADOR,
                textoTitulo = "El pudú perdido",
                textoCuerpo = "En el bosque del sur vivía un pudú pequeño llamado Lolo. Una mañana de lluvia se alejó de su mamá " +
                    "buscando hojas tiernas. Cuando quiso volver, todos los árboles le parecían iguales. Entonces escuchó " +
                    "un silbido conocido: era su mamá llamándolo. Lolo corrió feliz y se acurrucó a su lado."
            )
        )
        pregunta(actividades, pudu, 1, TipoPregunta.EXPLICITA, "¿Qué buscaba Lolo cuando se alejó?",
            listOf("Hojas tiernas", "Piedras", "Agua"), 0,
            "¡Muy bien! Buscaba hojas tiernas.", "Lee la segunda oración del texto.")
        pregunta(actividades, pudu, 2, TipoPregunta.IMPLICITA, "¿Por qué Lolo no podía volver?",
            listOf("Porque se perdió", "Porque estaba dormido", "Porque no quería"), 0,
            "¡Muy bien! Todos los árboles le parecían iguales: se perdió.", "¿Qué le pasaba cuando miraba los árboles?")

        // ---------- Matemática ----------
        val lapices = actividades.insertar(
            Actividad(
                nombre = "Lápices de la sala", modulo = Modulo.MATEMATICA, curso = 2,
                contenido = "Adición y sustracción hasta 100", objetivo = "Resolver adiciones y sustracciones sin reserva hasta 100.",
                dificultad = 1, instrucciones = "Resuelve cada problema. Puedes usar los bloques para contar.",
                intentosPermitidos = 3, estado = EstadoActividad.DISPONIBLE
            )
        )
        val p1 = pregunta(actividades, lapices, 1, TipoPregunta.PROBLEMA,
            "En la sala hay 24 lápices. La profesora trae 15 más. ¿Cuántos lápices hay ahora?",
            listOf("39", "9", "29", "49"), 0, "¡Muy bien! 24 + 15 = 39.", "Junta las decenas y luego las unidades.",
            24, 15, "+", "4 + 5 = 9 unidades; 2 + 1 = 3 decenas: 39")
        val p2 = pregunta(actividades, lapices, 2, TipoPregunta.CALCULO, "¿Cuánto es 32 + 6?",
            listOf("38", "26", "92"), 0, "¡Muy bien! 32 + 6 = 38.", "Suma solo las unidades: 2 + 6.",
            32, 6, "+", "2 + 6 = 8 unidades; quedan 3 decenas: 38")
        val p3 = pregunta(actividades, lapices, 3, TipoPregunta.PROBLEMA,
            "Hay 45 hojas en el cuaderno y se usan 23. ¿Cuántas quedan?",
            listOf("22", "68", "21"), 0, "¡Muy bien! 45 − 23 = 22.", "Quitar es restar: 45 − 23.",
            45, 23, "−", "5 − 3 = 2 unidades; 4 − 2 = 2 decenas: 22")

        val feria = actividades.insertar(
            Actividad(
                nombre = "Problemas en la feria", modulo = Modulo.MATEMATICA, curso = 2,
                contenido = "Problemas de adición y sustracción", objetivo = "Resolver problemas cotidianos con adición y sustracción.",
                dificultad = 2, instrucciones = "Lee cada problema de la feria y elige el resultado.",
                intentosPermitidos = 2, estado = EstadoActividad.DISPONIBLE
            )
        )
        pregunta(actividades, feria, 1, TipoPregunta.PROBLEMA, "En la feria hay 56 paltas. Se venden 30. ¿Cuántas quedan?",
            listOf("26", "86", "36"), 0, "¡Muy bien! 56 − 30 = 26.", "Se venden: entonces quedan menos.", 56, 30, "−", "56 − 30 = 26")
        pregunta(actividades, feria, 2, TipoPregunta.PROBLEMA, "Pedro tiene 18 láminas y su prima le regala 21. ¿Cuántas tiene ahora?",
            listOf("39", "3", "49"), 0, "¡Muy bien! 18 + 21 = 39.", "Le regalan más: hay que sumar.", 18, 21, "+", "18 + 21 = 39")

        // ---------- Una sesión ya finalizada para ver Resultados ----------
        val ahora = System.currentTimeMillis()
        val asignacionId = asignaciones.insertar(
            Asignacion(
                estudianteId = alumnos.getValue("Cóndor-03"), actividadId = lapices, asistenteId = asistenteId,
                codigo = "0000", creadaEn = ahora - 3_600_000, venceEn = ahora - 1_800_000,
                estado = EstadoAsignacion.FINALIZADA, iniciadaEn = ahora - 3_500_000, finalizadaEn = ahora - 3_000_000
            )
        )
        responder(db, asignacionId, p1, correcta = true, intento = 1, fecha = ahora - 3_400_000)
        responder(db, asignacionId, p2, correcta = false, intento = 1, fecha = ahora - 3_300_000)
        responder(db, asignacionId, p2, correcta = true, intento = 2, fecha = ahora - 3_250_000)
        responder(db, asignacionId, p3, correcta = true, intento = 1, fecha = ahora - 3_100_000)

        // ---------- Una sesión pendiente para probar el modo estudiante con el código 4821 ----------
        asignaciones.insertar(
            Asignacion(
                estudianteId = alumnos.getValue("Pudú-12"), actividadId = volantin, asistenteId = asistenteId,
                codigo = "4821", creadaEn = ahora, venceEn = ahora + 365L * 24 * 3_600_000
            )
        )
    }

    private fun usuario(alias: String, clave: String, rol: Rol): Usuario {
        val sal = Hash.nuevaSal()
        return Usuario(alias = alias, rol = rol, hashClave = Hash.sha256(clave, sal), sal = sal)
    }

    /** Inserta una pregunta con sus alternativas; [correcta] es el índice de la alternativa correcta. */
    private suspend fun pregunta(
        dao: ActividadDao, actividadId: Long, orden: Int, tipo: TipoPregunta, enunciado: String,
        alternativas: List<String>, correcta: Int, retro: String, pista: String,
        a: Int? = null, b: Int? = null, operacion: String? = null, procedimiento: String? = null
    ): Long = dao.guardarPreguntaCompleta(
        Pregunta(
            actividadId = actividadId, orden = orden, tipo = tipo, enunciado = enunciado,
            retroalimentacion = retro, pista = pista, numeroA = a, numeroB = b,
            operacion = operacion, procedimiento = procedimiento
        ),
        alternativas.mapIndexed { i, texto -> Alternativa(preguntaId = 0, texto = texto, esCorrecta = i == correcta) }
    )

    private suspend fun responder(db: AppDatabase, asignacionId: Long, preguntaId: Long, correcta: Boolean, intento: Int, fecha: Long) {
        val alternativas = db.actividadDao().obtenerPregunta(preguntaId)?.alternativas.orEmpty()
        val elegida = alternativas.first { it.esCorrecta == correcta }
        db.asignacionDao().insertarRespuesta(
            Respuesta(asignacionId = asignacionId, preguntaId = preguntaId, alternativaId = elegida.id,
                correcta = correcta, intento = intento, fecha = fecha)
        )
    }
}
