package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.local.ActividadDao
import cl.duoc.mpazneb.data.remote.ApiService
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.support.PruebaFabrica
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class ActividadRepositoryRemotoExtraTest {

    private val dao = mockk<ActividadDao>(relaxed = true)
    private val api = mockk<ApiService>()
    private val repo = ActividadRepository(dao, PruebaFabrica.authAdmin(), api)

    @Test
    fun idRemotoDe_enlazaDesdeListado() = runTest {
        val local = PruebaFabrica.actividadLocal(idRemoto = null)
        coEvery { api.listarActividades() } returns listOf(PruebaFabrica.actividadRemota())
        coEvery { dao.actualizarIdRemoto(3, 1009) } returns Unit

        val resultado = repo.idRemotoDe(local)
        assertTrue(resultado is ResultadoAccion.Exito)
    }

    @Test
    fun idRemotoDe_errorRed() = runTest {
        val local = PruebaFabrica.actividadLocal(idRemoto = null)
        coEvery { api.listarActividades() } throws IOException("offline")

        val resultado = repo.idRemotoDe(local)
        assertTrue(resultado is ResultadoAccion.Error)
    }

    @Test
    fun sincronizar_rechazaSinAdmin() = runTest {
        val repoAsistente = ActividadRepository(dao, PruebaFabrica.authAsistente(), api)
        assertTrue(repoAsistente.sincronizarCatalogoRemoto() is ResultadoAccion.Error)
    }
}