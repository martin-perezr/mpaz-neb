package cl.duoc.mpazneb.support

import android.content.Context
import androidx.room.Room
import cl.duoc.mpazneb.data.local.AppDatabase

/** Base Room temporal en memoria; no usa `mpazneb.db` del dispositivo. */
object BaseRoomInstrumentada {
    fun crear(context: Context): AppDatabase =
        Room.inMemoryDatabaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
}