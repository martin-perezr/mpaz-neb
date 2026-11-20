package cl.duoc.mpazneb.data.remote

import cl.duoc.mpazneb.data.remote.dto.ActividadDto
import cl.duoc.mpazneb.data.remote.dto.ActividadRequest
import cl.duoc.mpazneb.data.remote.dto.AsignacionDto
import cl.duoc.mpazneb.data.remote.dto.AsignacionRequest
import cl.duoc.mpazneb.data.remote.dto.ValidarCodigoRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("api/salud")
    suspend fun salud(): SaludDto

    @GET("api/actividades")
    suspend fun listarActividades(): List<ActividadDto>

    @POST("api/actividades")
    suspend fun crearActividad(@Body solicitud: ActividadRequest): Response<ActividadDto>

    @PUT("api/actividades/{id}")
    suspend fun editarActividad(
        @Path("id") id: Long,
        @Body solicitud: ActividadRequest
    ): Response<ActividadDto>

    @DELETE("api/actividades/{id}")
    suspend fun eliminarActividad(@Path("id") id: Long): Response<Unit>

    @POST("api/asignaciones")
    suspend fun crearAsignacion(@Body solicitud: AsignacionRequest): Response<AsignacionDto>

    @POST("api/codigos/validar")
    suspend fun validarCodigo(@Body solicitud: ValidarCodigoRequest): Response<AsignacionDto>
}

data class SaludDto(val estado: String)

