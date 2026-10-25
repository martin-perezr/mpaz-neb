package cl.duoc.mpazneb.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.mpazneb.contenedor
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AuthRepository
import cl.duoc.mpazneb.data.repository.ResultadoAccion
import cl.duoc.mpazneb.device.AlmacenImagenes
import cl.duoc.mpazneb.domain.validation.ActividadValidator
import cl.duoc.mpazneb.domain.validation.ErroresActividad
import cl.duoc.mpazneb.domain.validation.ImagenValidator
import cl.duoc.mpazneb.model.Actividad
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.PreguntaConAlternativas
import cl.duoc.mpazneb.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorActividadUiState(
    val cargando: Boolean = true,
    val id: Long = 0,
    val nombre: String = "",
    val modulo: Modulo? = null,
    val curso: Int? = null,
    val dificultad: Int? = null,
    val contenido: String = "",
    val objetivo: String = "",
    val instrucciones: String = "",
    val intentos: String = "2",
    val textoTitulo: String = "",
    val textoCuerpo: String = "",
    val estado: EstadoActividad = EstadoActividad.BORRADOR,

    // Ilustración actual y la que está guardada en Room.
    val imagenRuta: String? = null,
    val imagenGuardada: String? = null,
    val errorImagen: String? = null,

    val preguntas: List<PreguntaConAlternativas> = emptyList(),
    val intentoGuardar: Boolean = false,
    val mensaje: String? = null,
    val mensajeEsError: Boolean = false,
    val sinPermiso: Boolean = false,
    val navegarAId: Long? = null
) {
    val errores: ErroresActividad
        get() = if (!intentoGuardar) {
            ErroresActividad()
        } else {
            ActividadValidator.validar(
                nombre,
                modulo,
                curso,
                dificultad,
                contenido,
                objetivo,
                instrucciones,
                intentos,
                textoCuerpo
            )
        }
}

class EditorActividadViewModel(
    private val actividadIdInicial: Long,
    private val auth: AuthRepository,
    private val actividades: ActividadRepository,
    private val almacen: AlmacenImagenes
) : ViewModel() {

    private val _estado = MutableStateFlow(
        EditorActividadUiState(id = actividadIdInicial)
    )

    val estado: StateFlow<EditorActividadUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val sesion = auth.sesionActual.first()

            if (sesion == null || sesion.rol != Rol.ADMIN) {
                _estado.update {
                    it.copy(cargando = false, sinPermiso = true)
                }
                return@launch
            }

            if (actividadIdInicial == 0L) {
                _estado.update { it.copy(cargando = false) }
                return@launch
            }

            val actividad = actividades.obtener(actividadIdInicial)

            if (actividad == null) {
                _estado.update {
                    it.copy(
                        cargando = false,
                        mensaje = "No se encontró la actividad",
                        mensajeEsError = true
                    )
                }
                return@launch
            }

            _estado.update {
                it.copy(
                    cargando = false,
                    id = actividad.id,
                    nombre = actividad.nombre,
                    modulo = actividad.modulo,
                    curso = actividad.curso,
                    dificultad = actividad.dificultad,
                    contenido = actividad.contenido,
                    objetivo = actividad.objetivo,
                    instrucciones = actividad.instrucciones,
                    intentos = actividad.intentosPermitidos.toString(),
                    textoTitulo = actividad.textoTitulo.orEmpty(),
                    textoCuerpo = actividad.textoCuerpo.orEmpty(),
                    estado = actividad.estado,
                    imagenRuta = actividad.imagenRuta,
                    imagenGuardada = actividad.imagenRuta
                )
            }

            actividades.preguntasDe(actividad.id).collect { preguntas ->
                _estado.update { it.copy(preguntas = preguntas) }
            }
        }
    }

    fun cambiarNombre(valor: String) =
        _estado.update { it.copy(nombre = valor, mensaje = null) }

    fun cambiarContenido(valor: String) =
        _estado.update { it.copy(contenido = valor, mensaje = null) }

    fun cambiarObjetivo(valor: String) =
        _estado.update { it.copy(objetivo = valor, mensaje = null) }

    fun cambiarInstrucciones(valor: String) =
        _estado.update { it.copy(instrucciones = valor, mensaje = null) }

    fun cambiarIntentos(valor: String) =
        _estado.update {
            it.copy(
                intentos = valor.filter { c -> c.isDigit() }.take(1),
                mensaje = null
            )
        }

    fun cambiarTextoTitulo(valor: String) =
        _estado.update { it.copy(textoTitulo = valor, mensaje = null) }

    fun cambiarTextoCuerpo(valor: String) =
        _estado.update { it.copy(textoCuerpo = valor, mensaje = null) }

    fun cambiarCurso(curso: Int) =
        _estado.update { it.copy(curso = curso, mensaje = null) }

    fun cambiarDificultad(dificultad: Int) =
        _estado.update { it.copy(dificultad = dificultad, mensaje = null) }

    fun cambiarEstadoActividad(estado: EstadoActividad) =
        _estado.update { it.copy(estado = estado, mensaje = null) }

    fun cambiarModulo(modulo: Modulo) {
        if (_estado.value.preguntas.isNotEmpty()) {
            _estado.update {
                it.copy(
                    mensaje = "No cambies el módulo cuando ya hay preguntas.",
                    mensajeEsError = true
                )
            }
            return
        }

        _estado.update {
            it.copy(modulo = modulo, mensaje = null)
        }
    }

    fun guardar() {
        _estado.update {
            it.copy(intentoGuardar = true, mensaje = null)
        }

        val actual = _estado.value

        val errores = ActividadValidator.validar(
            actual.nombre,
            actual.modulo,
            actual.curso,
            actual.dificultad,
            actual.contenido,
            actual.objetivo,
            actual.instrucciones,
            actual.intentos,
            actual.textoCuerpo
        )

        if (!errores.valido) {
            _estado.update {
                it.copy(
                    mensaje = "Revisa los campos marcados.",
                    mensajeEsError = true
                )
            }
            return
        }

        val modulo = actual.modulo ?: return
        val curso = actual.curso ?: return
        val dificultad = actual.dificultad ?: return
        val esLectura = modulo == Modulo.LECTURA

        val actividad = Actividad(
            id = actual.id,
            nombre = actual.nombre.trim(),
            modulo = modulo,
            curso = curso,
            contenido = actual.contenido.trim(),
            objetivo = actual.objetivo.trim(),
            dificultad = dificultad,
            instrucciones = actual.instrucciones.trim(),
            intentosPermitidos = actual.intentos.toInt(),
            estado = actual.estado,
            textoTitulo = if (esLectura) {
                actual.textoTitulo.trim().ifBlank { null }
            } else {
                null
            },
            textoCuerpo = if (esLectura) {
                actual.textoCuerpo.trim().ifBlank { null }
            } else {
                null
            },
            imagenRuta = actual.imagenRuta
        )

        viewModelScope.launch {
            when (val resultado = actividades.guardar(actividad)) {
                is ResultadoAccion.Exito -> {
                    val eraNueva = actual.id == 0L
                    if (actual.imagenGuardada != actual.imagenRuta) {
                        almacen.borrar(actual.imagenGuardada)
                    }

                    _estado.update {
                        it.copy(
                            id = resultado.id,
                            imagenGuardada = actual.imagenRuta,
                            mensaje = "Actividad guardada.",
                            mensajeEsError = false,
                            navegarAId = if (eraNueva) resultado.id else null
                        )
                    }
                }

                is ResultadoAccion.Error -> {
                    _estado.update {
                        it.copy(
                            mensaje = resultado.mensaje,
                            mensajeEsError = true
                        )
                    }
                }
            }
        }
    }

    fun borrarPregunta(preguntaId: Long) {
        viewModelScope.launch {
            when (val resultado = actividades.borrarPregunta(preguntaId)) {
                is ResultadoAccion.Exito -> {
                    _estado.update {
                        it.copy(
                            mensaje = "Pregunta eliminada.",
                            mensajeEsError = false
                        )
                    }
                }

                is ResultadoAccion.Error -> {
                    _estado.update {
                        it.copy(
                            mensaje = resultado.mensaje,
                            mensajeEsError = true
                        )
                    }
                }
            }
        }
    }

    /** Valida la imagen elegida, la copia al almacenamiento privado y la muestra. */
    fun elegirImagen(uri: Uri) {
        viewModelScope.launch {
            val (tipo, bytes) = almacen.describir(uri)
            val error = ImagenValidator.validar(tipo, bytes)

            if (error != null) {
                _estado.update {
                    it.copy(errorImagen = error)
                }
                return@launch
            }

            val nueva = almacen.copiarAInterno(uri)

            if (nueva == null) {
                _estado.update {
                    it.copy(
                        errorImagen =
                            "No se pudo copiar la imagen. Prueba con otra."
                    )
                }
                return@launch
            }

            descartarSinGuardar()

            _estado.update {
                it.copy(
                    imagenRuta = nueva,
                    errorImagen = null,
                    mensaje = null
                )
            }
        }
    }

    fun quitarImagen() {
        descartarSinGuardar()

        _estado.update {
            it.copy(
                imagenRuta = null,
                errorImagen = null,
                mensaje = null
            )
        }
    }

    /** Borra la copia elegida en esta edición si todavía no quedó guardada. */
    private fun descartarSinGuardar() {
        val actual = _estado.value

        if (actual.imagenRuta != actual.imagenGuardada) {
            almacen.borrar(actual.imagenRuta)
        }
    }

    /** Si se sale del editor sin guardar, no deja archivos provisionales. */
    override fun onCleared() = descartarSinGuardar()

    fun navegacionHecha() =
        _estado.update { it.copy(navegarAId = null) }

    companion object {
        fun factory(actividadId: Long) = viewModelFactory {
            initializer {
                val contenedor = contenedor()

                EditorActividadViewModel(
                    actividadId,
                    contenedor.authRepository,
                    contenedor.actividadRepository,
                    contenedor.almacenImagenes
                )
            }
        }
    }
}