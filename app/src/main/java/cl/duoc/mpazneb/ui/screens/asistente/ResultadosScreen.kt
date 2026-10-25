package cl.duoc.mpazneb.ui.screens.asistente

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.device.ExportadorCsv
import cl.duoc.mpazneb.domain.validation.ObservacionValidator
import cl.duoc.mpazneb.ui.components.Aviso
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.CampoValidado
import cl.duoc.mpazneb.ui.components.Cargando
import cl.duoc.mpazneb.ui.components.MarcoAdulto
import cl.duoc.mpazneb.ui.components.SelectorChips
import cl.duoc.mpazneb.ui.components.TamanoVentana
import cl.duoc.mpazneb.ui.components.rememberTamanoVentana
import cl.duoc.mpazneb.ui.theme.Grafito
import cl.duoc.mpazneb.utils.formatoFecha
import cl.duoc.mpazneb.viewmodel.ResultadosViewModel

@Composable
fun ResultadosScreen(
    asignacionId: Long,
    onVolver: () -> Unit,
    vm: ResultadosViewModel = viewModel(factory = ResultadosViewModel.factory(asignacionId))
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val contenidoActual = androidx.compose.runtime.rememberUpdatedState(estado.contenidoCsv)
    val exportador = ExportadorCsv(contexto)
    val guardarArchivo = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) {
            vm.avisarExportacion("Exportación cancelada.", true)
        } else {
            val error = exportador.escribir(uri, contenidoActual.value)
            if (error == null) {
                vm.avisarExportacion("Resultados guardados.", false)
                Toast.makeText(contexto, "Archivo guardado", Toast.LENGTH_SHORT).show()
            } else {
                vm.avisarExportacion(error, true)
            }
        }
    }
    LaunchedEffect(estado.sinPermiso) { if (estado.sinPermiso) onVolver() }
    LaunchedEffect(estado.pedirArchivo) {
        if (estado.pedirArchivo) {
            vm.archivoPedido()
            guardarArchivo.launch("resultados-mpaz-neb.csv")
        }
    }

    MarcoAdulto(titulo = "Resultados", onVolver = onVolver) { padding ->
        if (estado.cargando) {
            Cargando(Modifier.padding(padding))
            return@MarcoAdulto
        }
        val resumen = @Composable { mod: Modifier ->
            Column(mod, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Aviso(estado.mensaje, estado.mensajeEsError)
                Text(estado.actividad, style = MaterialTheme.typography.headlineSmall)
                Text("${estado.estudiante} · código ${estado.codigo}", color = Grafito)
                Text("Estado: ${estado.estadoAsignacion}", color = Grafito)
                Card(Modifier.fillMaxWidth()) {
                    Text(estado.resultado, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                }
                Text("Respuestas e intentos", style = MaterialTheme.typography.titleLarge)
                if (estado.respuestas.isEmpty()) {
                    Text("Todavía no hay respuestas guardadas.", color = Grafito)
                }
                estado.respuestas.forEach { fila ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(fila.enunciado, style = MaterialTheme.typography.titleSmall)
                            Text("Eligió: ${fila.elegida}", color = Grafito)
                            Text(
                                if (fila.correcta) "Correcta · intento ${fila.intento}" else "Incorrecta · intento ${fila.intento}",
                                color = Grafito
                            )
                        }
                    }
                }
            }
        }
        val observaciones = @Composable { mod: Modifier ->
            Column(mod, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Observaciones ficticias", style = MaterialTheme.typography.titleLarge)
                if (estado.observaciones.isEmpty()) {
                    Text("Todavía no hay observaciones.", color = Grafito)
                }
                estado.observaciones.forEach { observacion ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(observacion.categoria, style = MaterialTheme.typography.labelLarge)
                            Text(observacion.texto)
                            Text(formatoFecha(observacion.fecha), color = Grafito, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                SelectorChips(
                    "Categoría",
                    ObservacionValidator.CATEGORIAS,
                    ObservacionValidator.CATEGORIAS.indexOf(estado.categoria).takeIf { it >= 0 },
                    { vm.cambiarCategoria(ObservacionValidator.CATEGORIAS[it]) },
                    error = estado.errores.categoria
                )
                CampoValidado(
                    estado.textoObservacion,
                    vm::cambiarTexto,
                    "Observación",
                    estado.errores.texto,
                    unaLinea = false,
                    lineasMin = 3,
                    maxCaracteres = ObservacionValidator.TEXTO_MAX
                )
                BotonPrincipal("Guardar observación", vm::guardarObservacion, Modifier.fillMaxWidth())
                BotonPrincipal("Exportar CSV", vm::pedirExportacion, Modifier.fillMaxWidth())
                Text(
                    "El archivo se guarda donde tú elijas. No es una calificación oficial.",
                    color = Grafito
                )
            }
        }
        val expandida = rememberTamanoVentana() == TamanoVentana.EXPANDIDA
        if (expandida) {
            Row(Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                resumen(Modifier.weight(1f).verticalScroll(rememberScrollState()))
                observaciones(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding())
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).imePadding().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                resumen(Modifier.fillMaxWidth())
                observaciones(Modifier.fillMaxWidth())
            }
        }
    }
}
