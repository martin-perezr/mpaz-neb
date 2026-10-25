package cl.duoc.mpazneb.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Opciones que se acomodan en varias líneas. Sirve en tablet y en teléfono.
 * [seleccionado] es el índice de la opción marcada. Null significa que no hay elección.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectorChips(
    etiqueta: String,
    opciones: List<String>,
    seleccionado: Int?,
    onElegir: (Int) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    androidx.compose.foundation.layout.Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.titleSmall,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            opciones.forEachIndexed { indice, texto ->
                FilterChip(
                    selected = seleccionado == indice,
                    onClick = { onElegir(indice) },
                    label = { Text(texto) }
                )
            }
        }
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}
