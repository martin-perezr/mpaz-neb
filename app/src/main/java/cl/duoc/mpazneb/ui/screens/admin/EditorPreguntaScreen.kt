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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.domain.validation.PreguntaValidator
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.TipoPregunta
import cl.duoc.mpazneb.ui.components.Aviso
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.CampoValidado
import cl.duoc.mpazneb.ui.components.Cargando
import cl.duoc.mpazneb.ui.components.MarcoAdulto
import cl.duoc.mpazneb.ui.components.SelectorChips
import cl.duoc.mpazneb.viewmodel.EditorPreguntaViewModel

@Composable
fun EditorPreguntaScreen(
    actividadId: Long,
    preguntaId: Long,
    onGuardada: () -> Unit,
    onVolver: () -> Unit,
    vm: EditorPreguntaViewModel = viewModel(factory = EditorPreguntaViewModel.factory(actividadId, preguntaId))
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado.sinPermiso) { if (estado.sinPermiso) onVolver() }
    LaunchedEffect(estado.guardada) { if (estado.guardada) onGuardada() }
    val tipos = estado.modulo?.let { TipoPregunta.de(it) }.orEmpty()

    MarcoAdulto(titulo = if (preguntaId == 0L) "Nueva pregunta" else "Editar pregunta", onVolver = onVolver) { padding ->
        if (estado.cargando) {
            Cargando(Modifier.padding(padding))
            return@MarcoAdulto
        }
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()).imePadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Aviso(estado.mensaje, estado.mensajeEsError)
            CampoValidado(estado.enunciado, vm::cambiarEnunciado, "Enunciado", estado.errores.enunciado, unaLinea = false, lineasMin = 2)
            if (tipos.isNotEmpty()) {
                SelectorChips(
                    "Tipo",
                    tipos.map { it.etiqueta },
                    tipos.indexOf(estado.tipo).takeIf { it >= 0 },
                    { vm.cambiarTipo(tipos[it]) },
                    error = estado.errores.tipo
                )
            }
            Text("Marca la alternativa correcta.")
            repeat(PreguntaValidator.MAX_ALTERNATIVAS) { indice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = estado.indiceCorrecta == indice,
                        onClick = { vm.marcarCorrecta(indice) }
                    )
                    CampoValidado(
                        estado.alternativas[indice],
                        { vm.cambiarAlternativa(indice, it) },
                        "Alternativa ${indice + 1}",
                        if (indice == 0) estado.errores.alternativas else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (estado.errores.correcta != null) {
                Text(estado.errores.correcta.orEmpty(), color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
            CampoValidado(estado.retroalimentacion, vm::cambiarRetroalimentacion, "Retroalimentación", estado.errores.retroalimentacion, unaLinea = false, lineasMin = 2)
            CampoValidado(estado.pista, vm::cambiarPista, "Pista para reintentar", estado.errores.pista, unaLinea = false, lineasMin = 2)
            if (estado.modulo == Modulo.MATEMATICA) {
                CampoValidado(estado.procedimiento, vm::cambiarProcedimiento, "Procedimiento", estado.errores.procedimiento, unaLinea = false, lineasMin = 2)
                CampoValidado(estado.numeroA, vm::cambiarNumeroA, "Número A (opcional)", estado.errores.numeros)
                CampoValidado(estado.numeroB, vm::cambiarNumeroB, "Número B (opcional)", null)
                SelectorChips(
                    "Operación",
                    listOf("+", "−", "×"),
                    listOf("+", "−", "×").indexOf(estado.operacion).takeIf { it >= 0 } ?: 0,
                    { vm.cambiarOperacion(listOf("+", "−", "×")[it]) }
                )
            }
            BotonPrincipal("Guardar pregunta", vm::guardar, Modifier.fillMaxWidth())
        }
    }
}
