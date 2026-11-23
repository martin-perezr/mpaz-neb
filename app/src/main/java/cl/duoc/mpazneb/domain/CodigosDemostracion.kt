package cl.duoc.mpazneb.domain

/** Códigos de la semilla local: no se consultan en Spring Boot. */
object CodigosDemostracion {
    private val SEMILLA = setOf("482196", "111111", "222222")

    fun esDeSemilla(codigo: String): Boolean = codigo in SEMILLA
}