package cl.duoc.mpazneb.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.Alternativa
import cl.duoc.mpazneb.model.Asignacion
import cl.duoc.mpazneb.model.Estudiante
import cl.duoc.mpazneb.model.Observacion
import cl.duoc.mpazneb.model.Pregunta
import cl.duoc.mpazneb.model.Respuesta
import cl.duoc.mpazneb.model.Usuario

@Database(
    entities = [
        Usuario::class, Estudiante::class, Actividad::class, Pregunta::class,
        Alternativa::class, Asignacion::class, Respuesta::class, Observacion::class
    ],
    // Versión 3: la semilla suma actividades de 2.°, 3.° y 4.° básico. Si la base ya existía, se vuelve a crear.
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun actividadDao(): ActividadDao
    abstract fun asignacionDao(): AsignacionDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        /** Singleton: una sola conexión a la base para toda la app. */
        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpazneb.db"
                )
                    .addCallback(SemillaCallback())
                    // MVP académico: si cambia el esquema se recrea la base con la semilla
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }

    /**
     * Al crear la base por primera vez se cargan los datos ficticios en la misma transacción,
     * antes de que la app pueda hacer cualquier consulta.
     */
    private class SemillaCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Semilla.poblar(db)
        }
    }
}
