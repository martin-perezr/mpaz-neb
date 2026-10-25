package cl.duoc.mpazneb.model

import androidx.room.Embedded
import androidx.room.Relation

// Clases que Room arma a partir de varias tablas (consultas con @Relation).

data class PreguntaConAlternativas(
    @Embedded val pregunta: Pregunta,
    @Relation(parentColumn = "id", entityColumn = "preguntaId")
    val alternativas: List<Alternativa>
)

data class ActividadConPreguntas(
    @Embedded val actividad: Actividad,
    @Relation(entity = Pregunta::class, parentColumn = "id", entityColumn = "actividadId")
    val preguntas: List<PreguntaConAlternativas>
)

/** Fila del catálogo: la actividad más cuántas preguntas tiene. */
data class ActividadConConteo(
    @Embedded val actividad: Actividad,
    val totalPreguntas: Int
)

data class AsignacionDetalle(
    @Embedded val asignacion: Asignacion,
    @Relation(parentColumn = "estudianteId", entityColumn = "id")
    val estudiante: Estudiante,
    @Relation(parentColumn = "actividadId", entityColumn = "id")
    val actividad: Actividad
)
