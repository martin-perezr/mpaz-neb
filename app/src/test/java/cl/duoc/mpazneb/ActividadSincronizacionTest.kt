package cl.duoc.mpazneb

import cl.duoc.mpazneb.data.repository.aActividadLocal
import cl.duoc.mpazneb.data.repository.aSolicitudRemota
import cl.duoc.mpazneb.data.repository.mensajeHttpActividad
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.support.PruebaFabrica
import org.junit.Assert.assertEquals
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response

class ActividadSincronizacionTest {

    @Test
    fun aSolicitudRemotaUsaNombreDelModulo() {
        val solicitud = PruebaFabrica.actividadLocal().aSolicitudRemota()
        assertEquals("LECTURA", solicitud.modulo)
        assertEquals("DISPONIBLE", solicitud.estado)
    }

    @Test
    fun aActividadLocalConservaImagenLocal() {
        val base = PruebaFabrica.actividadLocal().copy(imagenRuta = "/local/foto.jpg")
        val remota = PruebaFabrica.actividadRemota().copy(imagenRuta = "/remoto/otra.jpg")
        val fusionada = remota.aActividadLocal(base)
        assertEquals("/local/foto.jpg", fusionada.imagenRuta)
        assertEquals(1009L, fusionada.idRemoto)
    }

    @Test
    fun mensajeHttp409ParaEliminar() {
        val resp = Response.error<Unit>(409, "{}".toResponseBody())
        assertEquals(
            "No se puede eliminar: revisa asignaciones o el estado en el servidor.",
            mensajeHttpActividad(resp, "x"),
        )
    }

    @Test
    fun fusionPreservaIdLocal() {
        val base = PruebaFabrica.actividadLocal(id = 99)
        val remota = PruebaFabrica.actividadRemota().copy(estado = "BORRADOR")
        val fusion = remota.aActividadLocal(base)
        assertEquals(99L, fusion.id)
        assertEquals(EstadoActividad.BORRADOR, fusion.estado)
    }
}