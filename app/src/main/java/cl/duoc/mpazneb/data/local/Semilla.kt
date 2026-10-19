package cl.duoc.mpazneb.data.local

import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.mpazneb.data.seguridad.Hash
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Rol
import cl.duoc.mpazneb.model.TipoPregunta

/**
 * Datos de prueba 100 % ficticios. Cuentas:
 *  - utp.demo / Utp2026        (Docente / UTP)
 *  - asistente.aula2 / Aula2026 (Asistente de aula)
 * Código de estudiante de prueba: 4821 (Pudú-12, "El volantín de Tomás").
 *
 * Se inserta directamente en la base que se está creando (dentro de onCreate), de forma síncrona:
 * así ninguna consulta de la app puede llegar antes de que los datos estén cargados.
 */
object Semilla {

    fun poblar(db: SupportSQLiteDatabase) {
        usuario(db, "utp.demo", "Utp2026", Rol.ADMIN)
        val asistenteId = usuario(db, "asistente.aula2", "Aula2026", Rol.ASISTENTE)

        val alumnos = listOf(
            "Zorro-07" to 2, "Pudú-12" to 2, "Cóndor-03" to 2, "Huemul-21" to 2,
            "Chinchilla-05" to 1, "Puma-09" to 3, "Llama-14" to 4
        ).associate { (alias, curso) -> alias to db.insertar("estudiantes", "alias" to alias, "curso" to curso) }

        // ---------- Lectura ----------
        val volantin = actividad(
            db, "El volantín de Tomás", Modulo.LECTURA, 2, "Comprensión lectora",
            "Extraer información explícita e implícita de un texto narrativo breve.", 1,
            "Lee el texto con calma. Después responde 3 preguntas.", 2, EstadoActividad.DISPONIBLE,
            "El volantín de Tomás",
            "Era septiembre y el cielo estaba lleno de volantines. Tomás armó uno rojo con papel de diario " +
                "y palitos de coligüe. Su abuela le prestó un carrete de hilo. Cuando el viento sopló fuerte, " +
                "el volantín subió tan alto que parecía un punto. Tomás sonrió y gritó: —¡Mira, abuela, toca las nubes!"
        )
        pregunta(db, volantin, 1, TipoPregunta.EXPLICITA, "¿De qué color era el volantín de Tomás?",
            listOf("Rojo", "Azul", "Verde", "Amarillo"), 0,
            "¡Muy bien! El texto dice que armó uno rojo.", "Busca la parte donde Tomás arma su volantín.")
        pregunta(db, volantin, 2, TipoPregunta.IMPLICITA, "¿Cómo se sintió Tomás al final?",
            listOf("Contento", "Asustado", "Enojado"), 0,
            "¡Muy bien! Sonrió y gritó: eso muestra que estaba contento.", "Fíjate en lo que hizo Tomás cuando el volantín subió.")
        pregunta(db, volantin, 3, TipoPregunta.VOCABULARIO, "En el texto, la palabra “carrete” quiere decir…",
            listOf("Un rollo para enrollar hilo", "Una fiesta", "Un pájaro"), 0,
            "¡Muy bien! La abuela le prestó un carrete de hilo.", "Lee otra vez: “un carrete de hilo”. ¿Qué cosa tiene hilo?")

        val pudu = actividad(
            db, "El pudú perdido", Modulo.LECTURA, 2, "Inferencias",
            "Inferir causas y sentimientos de un personaje.", 2,
            "Lee la historia de Lolo y responde 2 preguntas.", 2, EstadoActividad.BORRADOR,
            "El pudú perdido",
            "En el bosque del sur vivía un pudú pequeño llamado Lolo. Una mañana de lluvia se alejó de su mamá " +
                "buscando hojas tiernas. Cuando quiso volver, todos los árboles le parecían iguales. Entonces escuchó " +
                "un silbido conocido: era su mamá llamándolo. Lolo corrió feliz y se acurrucó a su lado."
        )
        pregunta(db, pudu, 1, TipoPregunta.EXPLICITA, "¿Qué buscaba Lolo cuando se alejó?",
            listOf("Hojas tiernas", "Piedras", "Agua"), 0,
            "¡Muy bien! Buscaba hojas tiernas.", "Lee la segunda oración del texto.")
        pregunta(db, pudu, 2, TipoPregunta.IMPLICITA, "¿Por qué Lolo no podía volver?",
            listOf("Porque se perdió", "Porque estaba dormido", "Porque no quería"), 0,
            "¡Muy bien! Todos los árboles le parecían iguales: se perdió.", "¿Qué le pasaba cuando miraba los árboles?")

        // ---------- Matemática ----------
        val lapices = actividad(
            db, "Lápices de la sala", Modulo.MATEMATICA, 2, "Adición y sustracción hasta 100",
            "Resolver adiciones y sustracciones sin reserva hasta 100.", 1,
            "Resuelve cada problema. Puedes usar los bloques para contar.", 3, EstadoActividad.DISPONIBLE
        )
        val p1 = pregunta(db, lapices, 1, TipoPregunta.PROBLEMA,
            "En la sala hay 24 lápices. La profesora trae 15 más. ¿Cuántos lápices hay ahora?",
            listOf("39", "9", "29", "49"), 0, "¡Muy bien! 24 + 15 = 39.", "Junta las decenas y luego las unidades.",
            24, 15, "+", "4 + 5 = 9 unidades; 2 + 1 = 3 decenas: 39")
        val p2 = pregunta(db, lapices, 2, TipoPregunta.CALCULO, "¿Cuánto es 32 + 6?",
            listOf("38", "26", "92"), 0, "¡Muy bien! 32 + 6 = 38.", "Suma solo las unidades: 2 + 6.",
            32, 6, "+", "2 + 6 = 8 unidades; quedan 3 decenas: 38")
        val p3 = pregunta(db, lapices, 3, TipoPregunta.PROBLEMA,
            "Hay 45 hojas en el cuaderno y se usan 23. ¿Cuántas quedan?",
            listOf("22", "68", "21"), 0, "¡Muy bien! 45 − 23 = 22.", "Quitar es restar: 45 − 23.",
            45, 23, "−", "5 − 3 = 2 unidades; 4 − 2 = 2 decenas: 22")

        val feria = actividad(
            db, "Problemas en la feria", Modulo.MATEMATICA, 2, "Problemas de adición y sustracción",
            "Resolver problemas cotidianos con adición y sustracción.", 2,
            "Lee cada problema de la feria y elige el resultado.", 2, EstadoActividad.DISPONIBLE
        )
        pregunta(db, feria, 1, TipoPregunta.PROBLEMA, "En la feria hay 56 paltas. Se venden 30. ¿Cuántas quedan?",
            listOf("26", "86", "36"), 0, "¡Muy bien! 56 − 30 = 26.", "Se venden: entonces quedan menos.", 56, 30, "−", "56 − 30 = 26")
        pregunta(db, feria, 2, TipoPregunta.PROBLEMA, "Pedro tiene 18 láminas y su prima le regala 21. ¿Cuántas tiene ahora?",
            listOf("39", "3", "49"), 0, "¡Muy bien! 18 + 21 = 39.", "Le regalan más: hay que sumar.", 18, 21, "+", "18 + 21 = 39")

        // ---------- Una sesión ya finalizada para ver Resultados ----------
        val ahora = System.currentTimeMillis()
        val finalizada = db.insertar(
            "asignaciones",
            "estudianteId" to alumnos.getValue("Cóndor-03"), "actividadId" to lapices, "asistenteId" to asistenteId,
            "codigo" to "0000", "creadaEn" to ahora - 3_600_000, "venceEn" to ahora - 1_800_000,
            "estado" to EstadoAsignacion.FINALIZADA.name, "iniciadaEn" to ahora - 3_500_000, "finalizadaEn" to ahora - 3_000_000
        )
        responder(db, finalizada, p1, correcta = true, intento = 1, fecha = ahora - 3_400_000)
        responder(db, finalizada, p2, correcta = false, intento = 1, fecha = ahora - 3_300_000)
        responder(db, finalizada, p2, correcta = true, intento = 2, fecha = ahora - 3_250_000)
        responder(db, finalizada, p3, correcta = true, intento = 1, fecha = ahora - 3_100_000)

        // ---------- Una sesión pendiente para probar el modo estudiante con el código 4821 ----------
        db.insertar(
            "asignaciones",
            "estudianteId" to alumnos.getValue("Pudú-12"), "actividadId" to volantin, "asistenteId" to asistenteId,
            "codigo" to "4821", "creadaEn" to ahora, "venceEn" to ahora + 365L * 24 * 3_600_000,
            "estado" to EstadoAsignacion.PENDIENTE.name
        )
    }

    // ---------- Ayudantes ----------

    /** Inserta una fila con los pares columna → valor y devuelve su id. */
    private fun SupportSQLiteDatabase.insertar(tabla: String, vararg valores: Pair<String, Any?>): Long =
        insert(tabla, SQLiteDatabase.CONFLICT_ABORT, contentValuesOf(*valores))

    private fun usuario(db: SupportSQLiteDatabase, alias: String, clave: String, rol: Rol): Long {
        val sal = Hash.nuevaSal()
        return db.insertar(
            "usuarios",
            "alias" to alias, "rol" to rol.name, "hashClave" to Hash.sha256(clave, sal), "sal" to sal,
            "activo" to true, "ultimoAcceso" to null
        )
    }

    private fun actividad(
        db: SupportSQLiteDatabase, nombre: String, modulo: Modulo, curso: Int, contenido: String, objetivo: String,
        dificultad: Int, instrucciones: String, intentos: Int, estado: EstadoActividad,
        textoTitulo: String? = null, textoCuerpo: String? = null
    ): Long = db.insertar(
        "actividades",
        "nombre" to nombre, "modulo" to modulo.name, "curso" to curso, "contenido" to contenido,
        "objetivo" to objetivo, "dificultad" to dificultad, "instrucciones" to instrucciones,
        "intentosPermitidos" to intentos, "estado" to estado.name,
        "textoTitulo" to textoTitulo, "textoCuerpo" to textoCuerpo, "imagenRuta" to null
    )

    /** Inserta la pregunta y sus alternativas; [correcta] es el índice de la alternativa correcta. Devuelve el id de la pregunta. */
    private fun pregunta(
        db: SupportSQLiteDatabase, actividadId: Long, orden: Int, tipo: TipoPregunta, enunciado: String,
        alternativas: List<String>, correcta: Int, retro: String, pista: String,
        a: Int? = null, b: Int? = null, operacion: String? = null, procedimiento: String? = null
    ): Long {
        val id = db.insertar(
            "preguntas",
            "actividadId" to actividadId, "orden" to orden, "tipo" to tipo.name, "enunciado" to enunciado,
            "retroalimentacion" to retro, "pista" to pista, "numeroA" to a, "numeroB" to b,
            "operacion" to operacion, "procedimiento" to procedimiento
        )
        alternativas.forEachIndexed { i, texto ->
            db.insertar("alternativas", "preguntaId" to id, "texto" to texto, "esCorrecta" to (i == correcta))
        }
        return id
    }

    /** Registra una respuesta eligiendo la primera alternativa correcta o incorrecta de la pregunta. */
    private fun responder(db: SupportSQLiteDatabase, asignacionId: Long, preguntaId: Long, correcta: Boolean, intento: Int, fecha: Long) {
        val alternativaId = db.query(
            "SELECT id FROM alternativas WHERE preguntaId = ? AND esCorrecta = ? ORDER BY id LIMIT 1",
            arrayOf<Any>(preguntaId, if (correcta) 1 else 0)
        ).use { c -> c.moveToFirst(); c.getLong(0) }
        db.insertar(
            "respuestas",
            "asignacionId" to asignacionId, "preguntaId" to preguntaId, "alternativaId" to alternativaId,
            "correcta" to correcta, "intento" to intento, "fecha" to fecha
        )
    }
}
