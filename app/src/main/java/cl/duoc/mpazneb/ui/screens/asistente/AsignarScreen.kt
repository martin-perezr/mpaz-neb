package cl.duoc.mpazneb.ui.screens.asistente

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.ui.components.Aviso
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.Cargando
import cl.duoc.mpazneb.ui.components.MarcoAdulto
import cl.duoc.mpazneb.ui.components.duracionAnimacion
import cl.duoc.mpazneb.ui.theme.AzulTinta
import cl.duoc.mpazneb.ui.theme.Grafito
import cl.duoc.mpazneb.utils.etiquetaCurso
import cl.duoc.mpazneb.viewmodel.AsignarViewModel
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row

@Composable
fun AsignarScreen(
    actividadId: Long,
    onVolver: () -> Unit,
    vm: AsignarViewModel = viewModel(factory = AsignarViewModel.factory(actividadId))
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    var confirmar by remember { mutableStateOf(false) }
    val duracion = duracionAnimacion(250)
    LaunchedEffect(estado.sinPermiso) { if (estado.sinPermiso) onVolver() }

    MarcoAdulto(titulo = "Asignar actividad", onVolver = onVolver) { padding ->
        if (estado.cargando) {
            Cargando(Modifier.padding(padding))
            return@MarcoAdulto
        }
        val actividad = estado.actividad
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Aviso(estado.mensaje, estado.mensajeEsError)
            if (actividad == null) return@Column
            Text(actividad.nombre, style = MaterialTheme.typography.headlineSmall)
            Text("${actividad.modulo.etiqueta} · ${etiquetaCurso(actividad.curso)}", color = Grafito)
            if (actividad.estado != EstadoActividad.DISPONIBLE) {
                Text("Esta actividad no está disponible para nuevas asignaciones.", color = MaterialTheme.colorScheme.error)
            }
            Text("Estudiante ficticio", style = MaterialTheme.typography.titleMedium)
            if (estado.errorEstudiante != null) {
                Text(estado.errorEstudiante.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
            estado.estudiantes.forEach { estudiante ->
                Card(onClick = { vm.elegirEstudiante(estudiante.id) }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = estado.estudianteId == estudiante.id, onClick = { vm.elegirEstudiante(estudiante.id) })
                        Text(estudiante.alias, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            if (estado.codigo == null && actividad.estado == EstadoActividad.DISPONIBLE) {
                BotonPrincipal("Generar código", { confirmar = true }, Modifier.fillMaxWidth())
            }
            AnimatedVisibility(visible = estado.codigo != null, enter = fadeIn(tween(duracion)) + scaleIn(tween(duracion))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Código de práctica", style = MaterialTheme.typography.titleMedium)
                    Text(estado.codigo.orEmpty(), style = MaterialTheme.typography.displaySmall, color = AzulTinta, textAlign = TextAlign.Center)
                    Text(
                        "Sirve solo para esta actividad en la tablet. No es una clave institucional. Vence en 30 minutos.",
                        color = Grafito,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Confirmar asignación") },
            text = { Text("Se guardará la asignación y se creará un código de seis números.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmar = false
                    vm.asignar()
                }) { Text("Asignar") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
    }
}
