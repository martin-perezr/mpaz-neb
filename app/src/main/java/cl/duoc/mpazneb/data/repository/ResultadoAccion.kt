package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.model.Asignacion

/**
 * Resultado de una operación que guarda datos.
 * Exito trae el id o la asignación creada. Error trae un mensaje para la pantalla.
 */
sealed interface ResultadoAccion {
    data class Exito(val id: Long = 0, val asignacion: Asignacion? = null) : ResultadoAccion
    data class Error(val mensaje: String) : ResultadoAccion
}
