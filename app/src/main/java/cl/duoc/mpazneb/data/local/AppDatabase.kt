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
                    // MVP académico: si cambia el esquema se recrea la base; SemillaCallback vuelve a cargar los datos
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }

    /**
     * Carga los datos ficticios cada vez que la base se abre vacía: al crearla por primera vez
     * y también cuando se recrea por subir [version] (fallbackToDestructiveMigration borra las
     * tablas, pero Room no vuelve a llamar a onCreate). Es síncrono, así que ninguna consulta
     * de la app llega antes que los datos.
     */
    private class SemillaCallback : Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            val usuarios = db.query("SELECT COUNT(*) FROM usuarios").use { c -> c.moveToFirst(); c.getInt(0) }
            if (usuarios > 0) return
            db.beginTransaction()
            try {
                Semilla.poblar(db)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
