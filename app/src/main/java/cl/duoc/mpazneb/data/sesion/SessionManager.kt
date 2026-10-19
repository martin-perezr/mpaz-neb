package cl.duoc.mpazneb.data.sesion

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.duoc.mpazneb.model.Rol
import cl.duoc.mpazneb.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sesion")

data class SesionUsuario(val id: Long, val alias: String, val rol: Rol)

/** Guarda quién inició sesión (DataStore) para no pedir login cada vez que se abre la app. */
class SessionManager(private val context: Context) {

    private object Claves {
        val ID = longPreferencesKey("usuario_id")
        val ALIAS = stringPreferencesKey("alias")
        val ROL = stringPreferencesKey("rol")
    }

    val sesion: Flow<SesionUsuario?> = context.dataStore.data.map { prefs ->
        val id = prefs[Claves.ID] ?: return@map null
        SesionUsuario(
            id = id,
            alias = prefs[Claves.ALIAS].orEmpty(),
            rol = Rol.valueOf(prefs[Claves.ROL] ?: Rol.ASISTENTE.name)
        )
    }

    suspend fun guardar(usuario: Usuario) {
        context.dataStore.edit { prefs ->
            prefs[Claves.ID] = usuario.id
            prefs[Claves.ALIAS] = usuario.alias
            prefs[Claves.ROL] = usuario.rol.name
        }
    }

    suspend fun cerrar() {
        context.dataStore.edit { it.clear() }
    }
}
