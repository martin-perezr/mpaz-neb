package cl.duoc.mpazneb.support

import cl.duoc.mpazneb.data.local.UsuarioDao
import cl.duoc.mpazneb.model.Usuario

class InMemoryUsuarioDao : UsuarioDao {
    val usuarios = mutableListOf<Usuario>()

    override suspend fun insertar(usuario: Usuario): Long {
        val id = (usuarios.maxOfOrNull { it.id } ?: 0) + 1
        usuarios += usuario.copy(id = id)
        return id
    }

    override suspend fun buscarPorAlias(alias: String): Usuario? =
        usuarios.firstOrNull { it.alias.equals(alias.trim(), ignoreCase = true) }

    override suspend fun registrarAcceso(id: Long, fecha: Long) {
        val idx = usuarios.indexOfFirst { it.id == id }
        if (idx >= 0) usuarios[idx] = usuarios[idx].copy(ultimoAcceso = fecha)
    }
}