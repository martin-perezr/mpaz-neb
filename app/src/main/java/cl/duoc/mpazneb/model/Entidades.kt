package cl.duoc.mpazneb.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Model del patrón MVVM: cada data class es una tabla de Room.
// Todos los datos son ficticios: alias, nunca nombres ni RUT reales.

@Entity(tableName = "usuarios", indices = [Index(value = ["alias"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alias: String,
    val rol: Rol,
    val hashClave: String,
    val sal: String,
    val activo: Boolean = true,
    val ultimoAcceso: Long? = null
)

@Entity(tableName = "estudiantes", indices = [Index(value = ["alias"], unique = true)])
data class Estudiante(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alias: String,
    val curso: Int
)

@Entity(tableName = "actividades")
data class Actividad(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val modulo: Modulo,
    val curso: Int,
    val contenido: String,
    val objetivo: String,
    val dificultad: Int,
    val instrucciones: String,
    val intentosPermitidos: Int,
    val estado: EstadoActividad = EstadoActividad.BORRADOR,
    // Solo Lectura: el texto que se lee antes de las preguntas
    val textoTitulo: String? = null,
    val textoCuerpo: String? = null,
    // Ruta interna de la ilustración elegida desde la galería
    val imagenRuta: String? = null
)

@Entity(
    tableName = "preguntas",
    foreignKeys = [ForeignKey(
        entity = Actividad::class,
        parentColumns = ["id"],
        childColumns = ["actividadId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("actividadId")]
)
data class Pregunta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actividadId: Long,
    val orden: Int,
    val tipo: TipoPregunta,
    val enunciado: String,
    val retroalimentacion: String,
    val pista: String,
    // Solo Matemática
    val numeroA: Int? = null,
    val numeroB: Int? = null,
    val operacion: String? = null,
    val procedimiento: String? = null
)

@Entity(
    tableName = "alternativas",
    foreignKeys = [ForeignKey(
        entity = Pregunta::class,
        parentColumns = ["id"],
        childColumns = ["preguntaId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("preguntaId")]
)
data class Alternativa(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val preguntaId: Long,
    val texto: String,
    val esCorrecta: Boolean
)

@Entity(
    tableName = "asignaciones",
    foreignKeys = [
        ForeignKey(entity = Estudiante::class, parentColumns = ["id"], childColumns = ["estudianteId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Actividad::class, parentColumns = ["id"], childColumns = ["actividadId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["asistenteId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("estudianteId"), Index("actividadId"), Index("asistenteId"), Index("codigo")]
)
data class Asignacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val estudianteId: Long,
    val actividadId: Long,
    val asistenteId: Long,
    val codigo: String,
    val creadaEn: Long,
    val venceEn: Long,
    val estado: EstadoAsignacion = EstadoAsignacion.PENDIENTE,
    val iniciadaEn: Long? = null,
    val finalizadaEn: Long? = null
)

@Entity(
    tableName = "respuestas",
    foreignKeys = [
        ForeignKey(entity = Asignacion::class, parentColumns = ["id"], childColumns = ["asignacionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Pregunta::class, parentColumns = ["id"], childColumns = ["preguntaId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("asignacionId"), Index("preguntaId")]
)
data class Respuesta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val asignacionId: Long,
    val preguntaId: Long,
    val alternativaId: Long,
    val correcta: Boolean,
    val intento: Int,
    val fecha: Long
)

@Entity(
    tableName = "observaciones",
    foreignKeys = [ForeignKey(entity = Asignacion::class, parentColumns = ["id"], childColumns = ["asignacionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("asignacionId")]
)
data class Observacion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val asignacionId: Long,
    val categoria: String,
    val texto: String,
    val fecha: Long
)
