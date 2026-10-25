package cl.duoc.mpazneb.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.model.EstadoActividad
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
import cl.duoc.mpazneb.viewmodel.CatalogoUiState
import cl.duoc.mpazneb.viewmodel.CatalogoViewModel
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun CatalogoScreen(
    onNueva: () -> Unit,
    onEditar: (Long) -> Unit,
    onSalir: () -> Unit,
    vm: CatalogoViewModel = viewModel(factory = CatalogoViewModel.Factory)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado.sinPermiso) { if (estado.sinPermiso) onSalir() }
    LaunchedEffect(estado.sesionCerrada) { if (estado.sesionCerrada) onSalir() }

    MarcoAdulto(
        titulo = "Catálogo · ${estado.alias}",
        onCerrarSesion = vm::cerrarSesion,
        acciones = {
            TextButton(onClick = onNueva) { Text("Nueva", color = Color.White) }
        }
    ) { padding ->
        if (estado.cargando) {
            Cargando(Modifier.padding(padding))
        } else {
            val expandida = rememberTamanoVentana() == TamanoVentana.EXPANDIDA
            if (expandida) {
                Row(Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FiltrosCatalogo(
                        estado,
                        vm,
                        Modifier.widthIn(max = 320.dp).weight(0.34f).verticalScroll(rememberScrollState())
                    )
                    ListaCatalogo(estado, onEditar, vm, Modifier.weight(0.66f))
                }
            } else {
                ListaCatalogo(estado, onEditar, vm, Modifier.padding(padding), mostrarFiltros = true)
            }
        }
    }
}

@Composable
private fun FiltrosCatalogo(estado: CatalogoUiState, vm: CatalogoViewModel, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Filtros", style = MaterialTheme.typography.titleLarge)
        SelectorChips(
            etiqueta = "Módulo",
            opciones = listOf("Todos", "Lectura", "Matemática"),
            seleccionado = when (estado.filtroModulo) {
                Modulo.LECTURA -> 1
                Modulo.MATEMATICA -> 2
                null -> 0
            },
            onElegir = { indice ->
                vm.filtrarModulo(when (indice) {
                    1 -> Modulo.LECTURA
                    2 -> Modulo.MATEMATICA
                    else -> null
                })
            }
        )
        SelectorChips(
            etiqueta = "Curso",
            opciones = listOf("Todos", "1.º", "2.º", "3.º", "4.º"),
            seleccionado = estado.filtroCurso ?: 0,
            onElegir = { indice -> vm.filtrarCurso(if (indice == 0) null else indice) }
        )
        SelectorChips(
            etiqueta = "Dificultad",
            opciones = listOf("Todas", "Inicial", "Intermedia", "Avanzada"),
            seleccionado = estado.filtroDificultad ?: 0,
            onElegir = { indice -> vm.filtrarDificultad(if (indice == 0) null else indice) }
        )
        Text(
            "Solo administración puede crear, editar, activar o desactivar.",
            style = MaterialTheme.typography.bodyMedium,
            color = Grafito
        )
    }
}

@Composable
private fun ListaCatalogo(
    estado: CatalogoUiState,
    onEditar: (Long) -> Unit,
    vm: CatalogoViewModel,
    modifier: Modifier,
    mostrarFiltros: Boolean = false
) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Aviso(estado.mensaje, estado.mensajeEsError) }
        if (mostrarFiltros) {
            item { FiltrosCatalogo(estado, vm, Modifier.fillMaxWidth()) }
        }
        if (estado.visibles.isEmpty()) {
            item { Text("No hay actividades con ese filtro.", style = MaterialTheme.typography.bodyLarge, color = Grafito) }
        }
        items(estado.visibles, key = { it.actividad.id }) { fila ->
            val actividad = fila.actividad
            val coloresEstado = coloresDe(actividad.estado)
            val coloresModulo = coloresDe(actividad.modulo)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipEstado(actividad.modulo.etiqueta, coloresModulo.fondo, coloresModulo.texto)
                        ChipEstado(actividad.estado.etiqueta, coloresEstado.first, coloresEstado.second)
                    }
                    actividad.imagenRuta?.let { ruta ->
                        AsyncImage(
                            model = File(ruta),
                            contentDescription = "Ilustración de ${actividad.nombre}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp))
                        )
                    }
                    Text(actividad.nombre, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${etiquetaCurso(actividad.curso)} · ${etiquetaDificultad(actividad.dificultad)} · ${fila.totalPreguntas} preguntas",
                        color = Grafito
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BotonPrincipal("Editar", { onEditar(actividad.id) }, Modifier.weight(1f))
                        if (actividad.estado == EstadoActividad.DISPONIBLE) {
                            BotonPrincipal("Desactivar", { vm.cambiarEstado(actividad.id, EstadoActividad.DESACTIVADA) }, Modifier.weight(1f))
                        } else {
                            BotonPrincipal("Activar", { vm.cambiarEstado(actividad.id, EstadoActividad.DISPONIBLE) }, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
