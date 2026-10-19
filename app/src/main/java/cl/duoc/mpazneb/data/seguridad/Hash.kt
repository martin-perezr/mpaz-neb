package cl.duoc.mpazneb.data.seguridad

import java.security.MessageDigest
import java.security.SecureRandom

/** La contraseña nunca se guarda en texto plano: se guarda SHA-256(sal + clave). */
object Hash {

    fun nuevaSal(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.aHex()
    }

    fun sha256(clave: String, sal: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((sal + clave).toByteArray(Charsets.UTF_8))
            .aHex()

    private fun ByteArray.aHex(): String = joinToString("") { "%02x".format(it) }
}
