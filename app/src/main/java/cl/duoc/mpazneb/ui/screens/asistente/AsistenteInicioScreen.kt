package cl.duoc.mpazneb.ui.screens.asistente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.ui.components.Aviso
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.Cargando
import cl.duoc.mpazneb.ui.components.ChipEstado
import cl.duoc.mpazneb.ui.components.MarcoAdulto
import cl.duoc.mpazneb.ui.components.SelectorChips
import cl.duoc.mpazneb.ui.components.TamanoVentana
import cl.duoc.mpazneb.ui.components.rememberTamanoVentana
import cl.duoc.mpazneb.ui.theme.Grafito
import cl.duoc.mpazneb.ui.theme.coloresDe
import cl.duoc.mpazneb.utils.etiquetaCurso
import cl.duoc.mpazneb.utils.etiquetaDificultad
import cl.duoc.mpazneb.utils.formatoFecha
import cl.duoc.mpazneb.viewmodel.AsistenteUiState
import cl.duoc.mpazneb.viewmodel.AsistenteViewModel

@Composable
fun AsistenteInicioScreen(
    onAsignar: (Long) -> Unit,
    onResultados: (Long) -> Unit,
    onSalir: () -> Unit,
    vm: AsistenteViewModel = viewModel(factory = AsistenteViewModel.Factory)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado.sinPermiso) { if (estado.sinPermiso) onSalir() }
    LaunchedEffect(estado.sesionCerrada) { if (estado.sesionCerrada) onSalir() }

    MarcoAdulto(titulo = "Asistente · ${estado.alias}", onCerrarSesion = vm::cerrarSesion) { padding ->
        if (estado.cargando) {
            Cargando(Modifier.padding(padding))
            return@MarcoAdulto
        }
        val expandida = rememberTamanoVentana() == TamanoVentana.EXPANDIDA
        if (expandida) {
            Row(Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PanelActividades(estado, onAsignar, vm, Modifier.weight(1f).verticalScroll(rememberScrollState()))
                PanelAsignaciones(estado, onResultados, vm, Modifier.weight(1f).verticalScroll(rememberScrollState()))
            }
        } else {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                PanelActividades(estado, onAsignar, vm, Modifier.fillMaxWidth())
                PanelAsignaciones(estado, onResultados, vm, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun PanelActividades(
    estado: AsistenteUiState,
    onAsignar: (Long) -> Unit,
    vm: AsistenteViewModel,
    modifier: Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Actividades disponibles", style = MaterialTheme.typography.titleLarge)
        Text(
            "Elige módulo, curso y dificultad. La aplicación no decide el nivel.",
            color = Grafito
        )
        Aviso(estado.mensaje, estado.mensajeEsError)
        SelectorChips(
            "Módulo",
            listOf("Lectura", "Matemática"),
            when (estado.modulo) {
                Modulo.LECTURA -> 0
                Modulo.MATEMATICA -> 1
                null -> null
            },
            { vm.elegirModulo(if (it == 0) Modulo.LECTURA else Modulo.MATEMATICA) }
        )
        SelectorChips("Curso", listOf("1.º", "2.º", "3.º", "4.º"), estado.curso?.minus(1), { vm.elegirCurso(it + 1) })
        SelectorChips(
            "Dificultad",
            listOf("Inicial", "Intermedia", "Avanzada"),
            estado.dificultad?.minus(1),
            { vm.elegirDificultad(it + 1) }
        )
        if (!estado.filtrosCompletos) {
            Text("Cuando elijas los tres filtros verás solo actividades disponibles.", color = Grafito)
        } else if (estado.actividades.isEmpty()) {
            Text("No hay actividades disponibles con ese filtro.", color = Grafito)
        }
        estado.actividades.forEach { actividad ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(actividad.nombre, style = MaterialTheme.typography.titleMedium)
                    Text("${etiquetaCurso(actividad.curso)} · ${etiquetaDificultad(actividad.dificultad)}", color = Grafito)
                    BotonPrincipal("Asignar", { onAsignar(actividad.id) })
                }
            }
        }
    }
}

@Composable
private fun PanelAsignaciones(
    estado: AsistenteUiState,
    onResultados: (Long) -> Unit,
    vm: AsistenteViewModel,
    modifier: Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Asignaciones", style = MaterialTheme.typography.titleLarge)
        if (estado.asignaciones.isEmpty()) {
            Text("Todavía no asignas actividades.", color = Grafito)
        }
        estado.asignaciones.forEach { detalle ->
            val colores = coloresDe(detalle.asignacion.estado)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipEstado(detalle.asignacion.estado.etiqueta, colores.first, colores.second)
                    }
                    Text(detalle.actividad.nombre, style = MaterialTheme.typography.titleMedium)
                    Text("${detalle.estudiante.alias} · código ${detalle.asignacion.codigo}", color = Grafito)
                    Text("Vence: ${formatoFecha(detalle.asignacion.venceEn)}", color = Grafito)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BotonPrincipal("Resultados", { onResultados(detalle.asignacion.id) }, Modifier.weight(1f))
                        if (detalle.asignacion.estado != EstadoAsignacion.FINALIZADA) {
                            BotonPrincipal("Nuevo código", { vm.renovarCodigo(detalle.asignacion.id) }, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
