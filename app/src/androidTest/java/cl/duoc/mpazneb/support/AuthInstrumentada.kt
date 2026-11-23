package cl.duoc.mpazneb.support

import cl.duoc.mpazneb.data.repository.AuthRepository
import cl.duoc.mpazneb.data.sesion.SesionUsuario
import cl.duoc.mpazneb.model.Rol
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

object AuthInstrumentada {
    fun admin(): AuthRepository =
        mockk {
            every { sesionActual } returns flowOf(
                SesionUsuario(1, "utp.demo", Rol.ADMIN),
            )
        }
}