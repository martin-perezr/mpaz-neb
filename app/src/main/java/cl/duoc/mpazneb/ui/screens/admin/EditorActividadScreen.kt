package cl.duoc.mpazneb.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.ui.components.Aviso
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.CampoValidado
import cl.duoc.mpazneb.ui.components.Cargando
import cl.duoc.mpazneb.ui.components.MarcoAdulto
import cl.duoc.mpazneb.ui.components.SelectorChips
import cl.duoc.mpazneb.ui.components.TamanoVentana
import cl.duoc.mpazneb.ui.components.rememberTamanoVentana
import cl.duoc.mpazneb.ui.theme.Grafito
import cl.duoc.mpazneb.ui.theme.coloresDe
import cl.duoc.mpazneb.viewmodel.EditorActividadViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import java.io.File
import cl.duoc.mpazneb.ui.components.SelectorImagen

@Composable
fun EditorActividadScreen(
    actividadId: Long,
    onCreada: (Long) -> Unit,
    onEditarPregunta: (Long) -> Unit,
    onNuevaPregunta: () -> Unit,
    onVolver: () -> Unit,
    vm: EditorActividadViewModel = viewModel(factory = EditorActividadViewModel.factory(actividadId))
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    var preguntaABorrar by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(estado.sinPermiso) { if (estado.sinPermiso) onVolver() }
    LaunchedEffect(estado.navegarAId) {
        val id = estado.navegarAId ?: return@LaunchedEffect
        vm.navegacionHecha()
        onCreada(id)
    }

    MarcoAdulto(titulo = if (actividadId == 0L) "Nueva actividad" else "Editar actividad", onVolver = onVolver) { padding ->
        if (estado.cargando) {
            Cargando(Modifier.padding(padding))
            return@MarcoAdulto
        }
        val formulario = @Composable { mod: Modifier ->
            Column(mod, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Aviso(estado.mensaje, estado.mensajeEsError)
                Text(
                    "Ilustración de la actividad",
                    style = MaterialTheme.typography.titleMedium
                )
                SelectorImagen(
                    ruta = estado.imagenRuta,
                    error = estado.errorImagen,
                    onElegida = vm::elegirImagen,
                    onQuitar = vm::quitarImagen,
                    modifier = Modifier.fillMaxWidth()
                )
                CampoValidado(estado.nombre, vm::cambiarNombre, "Nombre", estado.errores.nombre, mostrarValido = estado.intentoGuardar)
                SelectorChips(
                    "Módulo",
                    listOf("Lectura", "Matemática"),
                    when (estado.modulo) {
                        Modulo.LECTURA -> 0
                        Modulo.MATEMATICA -> 1
                        null -> null
                    },
                    { indice -> vm.cambiarModulo(if (indice == 0) Modulo.LECTURA else Modulo.MATEMATICA) },
                    error = estado.errores.modulo
                )
                SelectorChips(
                    "Curso",
                    listOf("1.º", "2.º", "3.º", "4.º"),
                    estado.curso?.minus(1),
                    { vm.cambiarCurso(it + 1) },
                    error = estado.errores.curso
                )
                SelectorChips(
                    "Dificultad",
                    listOf("Inicial", "Intermedia", "Avanzada"),
                    estado.dificultad?.minus(1),
                    { vm.cambiarDificultad(it + 1) },
                    error = estado.errores.dificultad
                )
                CampoValidado(estado.contenido, vm::cambiarContenido, "Contenido", estado.errores.contenido)
                CampoValidado(estado.objetivo, vm::cambiarObjetivo, "Objetivo", estado.errores.objetivo, unaLinea = false, lineasMin = 2)
                CampoValidado(estado.instrucciones, vm::cambiarInstrucciones, "Instrucciones", estado.errores.instrucciones, unaLinea = false, lineasMin = 2)
                CampoValidado(estado.intentos, vm::cambiarIntentos, "Intentos (1 a 5)", estado.errores.intentos)
                SelectorChips(
                    "Estado",
                    EstadoActividad.entries.map { it.etiqueta },
                    EstadoActividad.entries.indexOf(estado.estado),
                    { vm.cambiarEstadoActividad(EstadoActividad.entries[it]) }
                )
                if (estado.modulo == Modulo.LECTURA) {
                    CampoValidado(estado.textoTitulo, vm::cambiarTextoTitulo, "Título del texto", null)
                    CampoValidado(
                        estado.textoCuerpo, vm::cambiarTextoCuerpo, "Texto de lectura", estado.errores.texto,
                        unaLinea = false, lineasMin = 4
                    )
                }
                BotonPrincipal("Guardar actividad", vm::guardar, Modifier.fillMaxWidth())
            }
        }
        val preguntas = @Composable { mod: Modifier ->
            Column(mod, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Preguntas", style = MaterialTheme.typography.titleLarge)
                if (estado.id == 0L) {
                    Text("Guarda la actividad antes de agregar preguntas.", color = Grafito)
                } else {
                    BotonPrincipal("Agregar pregunta", onNuevaPregunta)
                    if (estado.preguntas.isEmpty()) {
                        Text("Todavía no hay preguntas.", color = Grafito)
                    }
                    estado.preguntas.forEach { item ->
                        val correcta = item.alternativas.firstOrNull { it.esCorrecta }?.texto.orEmpty()
                        val colores = coloresDe(item.pregunta.tipo.modulo)
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(item.pregunta.tipo.etiqueta, color = colores.texto, style = MaterialTheme.typography.labelLarge)
                                Text(item.pregunta.enunciado, style = MaterialTheme.typography.titleMedium)
                                Text("Respuesta correcta: $correcta", color = Grafito)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    BotonPrincipal("Editar", { onEditarPregunta(item.pregunta.id) }, Modifier.weight(1f))
                                    BotonPrincipal("Eliminar", { preguntaABorrar = item.pregunta.id }, Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
        val expandida = rememberTamanoVentana() == TamanoVentana.EXPANDIDA
        if (expandida) {
            Row(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                formulario(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding())
                preguntas(Modifier.weight(1f).verticalScroll(rememberScrollState()))
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()).imePadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                formulario(Modifier.fillMaxWidth())
                preguntas(Modifier.fillMaxWidth())
            }
        }
    }

    if (preguntaABorrar != null) {
        AlertDialog(
            onDismissRequest = { preguntaABorrar = null },
            title = { Text("Eliminar pregunta") },
            text = { Text("La pregunta y sus alternativas se borran de la actividad.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.borrarPregunta(preguntaABorrar!!)
                    preguntaABorrar = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { preguntaABorrar = null }) { Text("Cancelar") } }
        )
    }
}
