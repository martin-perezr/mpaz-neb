package cl.duoc.mpazneb.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.CampoValidado
import cl.duoc.mpazneb.ui.theme.Grafito
import cl.duoc.mpazneb.viewmodel.EditorActividadUiState

@Composable
fun BuscarLibrosSection(
    estado: EditorActividadUiState,
  onConsulta: (String) -> Unit,
  onBuscar: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Buscar libros", style = MaterialTheme.typography.titleMedium)
        CampoValidado(
            valor = estado.consultaLibros,
            onCambio = onConsulta,
            etiqueta = "Título o autor",
            error = null
        )
        BotonPrincipal("Buscar", onBuscar, Modifier.fillMaxWidth())
        if (estado.buscandoLibros) {
            CircularProgressIndicator()
        }
        estado.mensajeLibros?.let { Text(it, color = Grafito) }
        estado.librosEncontrados.forEach { libro ->
            val autor = libro.autores.firstOrNull()?.trim().orEmpty()
            Text(
                libro.titulo,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                if (autor.isEmpty()) "Autor desconocido" else autor,
                color = Grafito,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}