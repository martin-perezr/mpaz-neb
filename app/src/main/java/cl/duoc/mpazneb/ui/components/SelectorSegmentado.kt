package cl.duoc.mpazneb.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Grupo de botones segmentados (Material 3) con etiqueta y mensaje de error opcional. */
@Composable
fun SelectorSegmentado(
    etiqueta: String,
    opciones: List<String>,
    seleccionado: Int?,
    onElegir: (Int) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    habilitado: Boolean = true
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.titleSmall,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            opciones.forEachIndexed { i, texto ->
                SegmentedButton(
                    selected = seleccionado == i,
                    onClick = { onElegir(i) },
                    enabled = habilitado,
                    shape = SegmentedButtonDefaults.itemShape(index = i, count = opciones.size)
                ) { Text(texto) }
            }
        }
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
