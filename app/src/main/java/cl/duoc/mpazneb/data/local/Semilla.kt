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
 * Código de estudiante de prueba: 482196 (Pudú-12, "El volantín de Tomás").
 * Código vencido de prueba: 111111. Código ya usado: 222222.
 *
 * Se inserta directamente en la base que se está creando (dentro de onCreate), de forma síncrona:
 * así ninguna consulta de la app puede llegar antes de que los datos estén cargados.
 */
object  Semilla {

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

        val gato = actividad(
            db, "El gato Mimí", Modulo.LECTURA, 1, "Comprensión lectora",
            "Identificar información explícita en un texto muy breve.", 1,
            "Lee el texto con calma y responde.", 2, EstadoActividad.DISPONIBLE,
            "El gato Mimí",
            "Mimí es un gato gris. Vive en una casa azul. En la mañana toma leche tibia y después duerme al sol."
        )
        pregunta(db, gato, 1, TipoPregunta.EXPLICITA, "¿De qué color es el gato Mimí?",
            listOf("Gris", "Azul", "Blanco"), 0,
            "¡Muy bien! El texto dice que Mimí es un gato gris.", "Lee la primera oración.")
        pregunta(db, gato, 2, TipoPregunta.VOCABULARIO, "En el texto, “tibia” quiere decir…",
            listOf("Un poco caliente", "Muy fría", "Con azúcar"), 0,
            "¡Muy bien! La leche tibia está un poco caliente.", "Piensa en cómo se siente la leche.")

        val manzanas = actividad(
            db, "Manzanas en la mesa", Modulo.MATEMATICA, 1, "Conteo y suma hasta 10",
            "Contar y sumar cantidades pequeñas.", 1,
            "Cuenta con calma y elige el resultado.", 2, EstadoActividad.DISPONIBLE
        )
        pregunta(db, manzanas, 1, TipoPregunta.CALCULO, "¿Cuánto es 3 + 2?",
            listOf("5", "1", "4"), 0, "¡Muy bien! 3 + 2 = 5.", "Junta los dos grupos y vuelve a contar.",
            3, 2, "+", "3 + 2 = 5")
        pregunta(db, manzanas, 2, TipoPregunta.PROBLEMA, "Hay 6 manzanas y se comen 1. ¿Cuántas quedan?",
            listOf("5", "7", "6"), 0, "¡Muy bien! 6 − 1 = 5.", "Se come una: queda una menos.",
            6, 1, "−", "6 − 1 = 5")

        val cajas = actividad(
            db, "Cajas de lápices", Modulo.MATEMATICA, 3, "Multiplicación inicial",
            "Resolver una multiplicación como suma repetida.", 2,
            "Lee el problema y elige el resultado.", 2, EstadoActividad.DISPONIBLE
        )
        pregunta(db, cajas, 1, TipoPregunta.PROBLEMA, "Hay 4 cajas con 3 lápices cada una. ¿Cuántos lápices hay?",
            listOf("12", "7", "9"), 0, "¡Muy bien! 4 veces 3 es 12.", "Puedes sumar 3 + 3 + 3 + 3.",
            4, 3, "×", "4 × 3 = 12")
        pregunta(db, cajas, 2, TipoPregunta.CALCULO, "¿Cuánto es 5 × 2?",
            listOf("10", "7", "52"), 0, "¡Muy bien! 5 × 2 = 10.", "Es lo mismo que 2 + 2 + 2 + 2 + 2.",
            5, 2, "×", "5 × 2 = 10")

        val puente = actividad(
            db, "El puente de madera", Modulo.LECTURA, 4, "Comprensión lectora",
            "Inferir una acción y reconocer el vocabulario de un texto breve.", 3,
            "Lee el texto y responde las preguntas.", 2, EstadoActividad.DISPONIBLE,
            "El puente de madera",
            "Lara llegó al estero y vio que una tabla del puente estaba suelta. " +
                "Avisó a su tío y juntos la afirmaron con una cuerda. Al cruzar, el agua sonaba debajo de sus pies."
        )
        pregunta(db, puente, 1, TipoPregunta.EXPLICITA, "¿Quién ayudó a Lara a afirmar la tabla?",
            listOf("Su tío", "Su gato", "Una vecina"), 0,
            "¡Muy bien! El texto dice que avisó a su tío.", "Busca con quién trabajó Lara.")
        pregunta(db, puente, 2, TipoPregunta.IMPLICITA, "¿Por qué Lara avisó a su tío?",
            listOf("Porque el puente estaba inseguro", "Porque quería nadar", "Porque tenía hambre"), 0,
            "¡Muy bien! La tabla suelta hacía inseguro el puente.", "Fíjate en lo que Lara vio al llegar.")
        pregunta(db, puente, 3, TipoPregunta.VOCABULARIO, "En el texto, “estero” es…",
            listOf("Un curso de agua", "Un puente", "Una cuerda"), 0,
            "¡Muy bien! El estero es el curso de agua que Lara debía cruzar.", "Lara llega allí antes de ver el puente.")

        val recreo = actividad(
            db, "Sumas en el recreo", Modulo.MATEMATICA, 2, "Adición",
            "Sumar cantidades pequeñas de un juego.", 1,
            "Resuelve la suma.", 2, EstadoActividad.DESACTIVADA
        )
        pregunta(db, recreo, 1, TipoPregunta.CALCULO, "¿Cuánto es 8 + 4?",
            listOf("12", "4", "84"), 0, "¡Muy bien! 8 + 4 = 12.", "Suma las unidades.",
            8, 4, "+", "8 + 4 = 12")

        // ---------- Lectura: 2.° avanzada y 3.° intermedia ----------
        val carta = actividad(
            db, "Carta a la abuela", Modulo.LECTURA, 2, "Propósito del texto",
            "Reconocer para qué se escribe una carta y deducir palabras por el contexto.", 3,
            "Lee la carta de Sofía y responde 3 preguntas.", 2, EstadoActividad.DISPONIBLE,
            "Carta a la abuela",
            "Querida abuela Rosa: te escribo desde Valparaíso. Ayer subimos al cerro en ascensor y desde arriba " +
                    "vimos el mar lleno de barcos. Mi hermano Lucas se asustó un poco, pero después se rio. " +
                    "Te extraño mucho. ¿Me enseñas a hacer sopaipillas cuando vuelva a Talca? Un abrazo grande, Sofía."
        )
        pregunta(db, carta, 1, TipoPregunta.IMPLICITA, "¿Para qué escribió Sofía esta carta?",
            listOf("Para contarle su viaje a la abuela", "Para vender sopaipillas", "Para explicar cómo funciona un ascensor"), 0,
            "¡Muy bien! Sofía le cuenta a su abuela lo que hizo en el viaje.", "Piensa en a quién le escribe y qué le cuenta.")
        pregunta(db, carta, 2, TipoPregunta.EXPLICITA, "¿Cómo subieron al cerro?",
            listOf("En ascensor", "En bicicleta", "Caminando"), 0,
            "¡Muy bien! La carta dice que subieron en ascensor.", "Busca la oración que habla del cerro.")
        pregunta(db, carta, 3, TipoPregunta.VOCABULARIO, "En la carta, “te extraño” quiere decir…",
            listOf("Te echo de menos", "Me das miedo", "Eres muy rara"), 0,
            "¡Muy bien! Sofía echa de menos a su abuela.", "¿Qué siente Sofía por su abuela que está lejos?")

        val huerta = actividad(
            db, "La huerta de la escuela", Modulo.LECTURA, 3, "Inferencias",
            "Inferir causas y consecuencias en un texto narrativo breve.", 2,
            "Lee el texto y responde 3 preguntas.", 2, EstadoActividad.DISPONIBLE,
            "La huerta de la escuela",
            "En marzo, el 3.° B sembró lechugas en la huerta de la escuela. Cada curso regaba un día distinto. " +
                    "Una semana hizo mucho calor y nadie fue a regar el viernes. El lunes, las hojas estaban caídas y amarillas. " +
                    "Desde ese día, Joaquín pegó un calendario en la puerta de la sala para que nadie olvidara su turno."
        )
        pregunta(db, huerta, 1, TipoPregunta.EXPLICITA, "¿Qué sembró el 3.° B?",
            listOf("Lechugas", "Tomates", "Papas"), 0,
            "¡Muy bien! El texto dice que sembraron lechugas.", "Lee la primera oración.")
        pregunta(db, huerta, 2, TipoPregunta.IMPLICITA, "¿Por qué las hojas estaban amarillas el lunes?",
            listOf("Porque nadie las regó con el calor", "Porque llovió mucho", "Porque eran de otro curso"), 0,
            "¡Muy bien! Hizo calor y nadie regó el viernes.", "¿Qué pasó el viernes de esa semana?")
        pregunta(db, huerta, 3, TipoPregunta.IMPLICITA, "¿Para qué pegó Joaquín el calendario?",
            listOf("Para que nadie olvidara regar", "Para decorar la sala", "Para anotar los cumpleaños"), 0,
            "¡Muy bien! El calendario recuerda a quién le toca regar.", "Lee la última oración.")

        // ---------- Matemática: 2.° avanzada y 4.° intermedia ----------
        val kiosco = actividad(
            db, "El kiosco del colegio", Modulo.MATEMATICA, 2, "Problemas de dos pasos",
            "Resolver problemas de adición y sustracción de dos pasos con dinero.", 3,
            "Lee cada problema del kiosco y elige el resultado.", 2, EstadoActividad.DISPONIBLE
        )
        pregunta(db, kiosco, 1, TipoPregunta.PROBLEMA,
            "Ana tiene $50. Compra un jugo de $20 y un pan de $10. ¿Cuánto dinero le queda?",
            listOf("$20", "$30", "$80"), 0, "¡Muy bien! 20 + 10 = 30 y 50 − 30 = 20.",
            "Primero suma lo que gastó y después réstalo de 50.", 50, 30, "−", "20 + 10 = 30; 50 − 30 = 20")
        pregunta(db, kiosco, 2, TipoPregunta.PROBLEMA, "Tomás compra un lápiz de $35 y una goma de $25. ¿Cuánto paga?",
            listOf("$60", "$10", "$50"), 0, "¡Muy bien! 35 + 25 = 60.",
            "Suma las unidades: 5 + 5 = 10. ¡Eso forma una decena más!", 35, 25, "+", "5 + 5 = 10; 30 + 20 + 10 = 60")
        pregunta(db, kiosco, 3, TipoPregunta.CALCULO, "¿Cuánto es 70 − 45?",
            listOf("25", "35", "115"), 0, "¡Muy bien! 70 − 45 = 25.", "Quita primero 40 y después 5.",
            70, 45, "−", "70 − 40 = 30; 30 − 5 = 25")

        val micro = actividad(
            db, "El viaje en micro", Modulo.MATEMATICA, 4, "Multiplicación y problemas hasta 1.000",
            "Resolver problemas de multiplicación y adición con números hasta 1.000.", 2,
            "Lee cada problema del viaje y elige el resultado.", 2, EstadoActividad.DISPONIBLE
        )
        pregunta(db, micro, 1, TipoPregunta.PROBLEMA, "Una micro tiene 6 filas con 4 asientos cada una. ¿Cuántos asientos hay?",
            listOf("24", "10", "20"), 0, "¡Muy bien! 6 × 4 = 24.", "Son 6 grupos de 4: suma 4 seis veces.",
            6, 4, "×", "6 × 4 = 24")
        pregunta(db, micro, 2, TipoPregunta.PROBLEMA, "El viaje de ida dura 125 minutos y el de vuelta, 125 minutos. ¿Cuántos minutos son en total?",
            listOf("250", "225", "350"), 0, "¡Muy bien! 125 + 125 = 250.", "Suma centenas, decenas y unidades por separado.",
            125, 125, "+", "100 + 100 = 200; 20 + 20 = 40; 5 + 5 = 10; 200 + 40 + 10 = 250")
        pregunta(db, micro, 3, TipoPregunta.CALCULO, "¿Cuánto es 8 × 7?",
            listOf("56", "15", "54"), 0, "¡Muy bien! 8 × 7 = 56.", "Puedes pensar en 8 × 5 = 40 y sumar 8 × 2 = 16.",
            8, 7, "×", "8 × 5 = 40; 8 × 2 = 16; 40 + 16 = 56")

        // ---------- Una sesión ya finalizada para ver Resultados ----------
        val ahora = System.currentTimeMillis()
        val finalizada = db.insertar(
            "asignaciones",
            "estudianteId" to alumnos.getValue("Cóndor-03"), "actividadId" to lapices, "asistenteId" to asistenteId,
            "codigo" to "000000", "creadaEn" to ahora - 3_600_000, "venceEn" to ahora - 1_800_000,
            "estado" to EstadoAsignacion.FINALIZADA.name, "iniciadaEn" to ahora - 3_500_000, "finalizadaEn" to ahora - 3_000_000
        )
        responder(db, finalizada, p1, correcta = true, intento = 1, fecha = ahora - 3_400_000)
        responder(db, finalizada, p2, correcta = false, intento = 1, fecha = ahora - 3_300_000)
        responder(db, finalizada, p2, correcta = true, intento = 2, fecha = ahora - 3_250_000)
        responder(db, finalizada, p3, correcta = true, intento = 1, fecha = ahora - 3_100_000)
        db.insertar(
            "observaciones",
            "asignacionId" to finalizada,
            "categoria" to "Participación",
            "texto" to "Observación ficticia: Cóndor-03 resolvió los problemas con calma.",
            "fecha" to ahora - 2_900_000
        )

        // Código vigente de demostración. Dura un año para poder probarlo en la defensa.
        // Los códigos que crea la asistente vencen a los 30 minutos.
        db.insertar(
            "asignaciones",
            "estudianteId" to alumnos.getValue("Pudú-12"), "actividadId" to volantin, "asistenteId" to asistenteId,
            "codigo" to "482196", "creadaEn" to ahora, "venceEn" to ahora + 365L * 24 * 3_600_000,
            "estado" to EstadoAsignacion.PENDIENTE.name
        )

        // Código vencido: existe, sigue pendiente, pero la hora ya pasó.
        db.insertar(
            "asignaciones",
            "estudianteId" to alumnos.getValue("Zorro-07"), "actividadId" to lapices, "asistenteId" to asistenteId,
            "codigo" to "111111", "creadaEn" to ahora - 3_600_000, "venceEn" to ahora - 60_000,
            "estado" to EstadoAsignacion.PENDIENTE.name
        )

        // Código ya usado: la actividad terminó y el plazo todavía no vence.
        db.insertar(
            "asignaciones",
            "estudianteId" to alumnos.getValue("Huemul-21"), "actividadId" to feria, "asistenteId" to asistenteId,
            "codigo" to "222222", "creadaEn" to ahora, "venceEn" to ahora + 365L * 24 * 3_600_000,
            "estado" to EstadoAsignacion.FINALIZADA.name, "iniciadaEn" to ahora, "finalizadaEn" to ahora
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
