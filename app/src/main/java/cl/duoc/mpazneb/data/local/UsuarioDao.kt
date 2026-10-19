package cl.duoc.mpazneb.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.mpazneb.model.Usuario

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE alias = :alias LIMIT 1")
    suspend fun buscarPorAlias(alias: String): Usuario?

    @Query("UPDATE usuarios SET ultimoAcceso = :fecha WHERE id = :id")
    suspend fun registrarAcceso(id: Long, fecha: Long)
}
