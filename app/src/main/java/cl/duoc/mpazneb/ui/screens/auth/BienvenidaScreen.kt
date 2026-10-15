package cl.duoc.mpazneb.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import cl.duoc.mpazneb.ui.components.TamanoVentana
import cl.duoc.mpazneb.ui.components.rememberTamanoVentana
import cl.duoc.mpazneb.ui.theme.AzulHoja
import cl.duoc.mpazneb.ui.theme.AzulTinta
import cl.duoc.mpazneb.ui.theme.Durazno
import cl.duoc.mpazneb.ui.theme.Grafito
import cl.duoc.mpazneb.ui.theme.GrisSuave
import cl.duoc.mpazneb.ui.theme.NaranjaLapiz
import cl.duoc.mpazneb.ui.theme.TextoEstudiante

@Composable
fun BienvenidaScreen(onEquipo: () -> Unit, onEstudiante: () -> Unit) {
    val tamano = rememberTamanoVentana()
    if (tamano == TamanoVentana.EXPANDIDA) {
        Row(Modifier.fillMaxSize()) {
            PanelMarca(Modifier.fillMaxHeight().weight(0.42f))
            Opciones(onEquipo, onEstudiante, Modifier.weight(0.58f).fillMaxHeight())
        }
    } else {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            PanelMarca(Modifier.fillMaxWidth())
            Opciones(onEquipo, onEstudiante, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun PanelMarca(modifier: Modifier) {
    Column(
        modifier.background(AzulTinta).padding(40.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("ESCUELA ESCRITORA MARCELA PAZ", style = MaterialTheme.typography.labelLarge, color = AzulHoja)
        Text("MPAZ NEB", style = MaterialTheme.typography.displaySmall.copy(fontSize = MaterialTheme.typography.displaySmall.fontSize * 1.5f), color = Color.White)
        Text(
            "Práctica de Lectura y Matemática para 1.° a 4.° básico, siempre acompañada.",
            style = MaterialTheme.typography.titleMedium,
            color = AzulHoja
        )
    }
}

@Composable
private fun Opciones(onEquipo: () -> Unit, onEstudiante: () -> Unit, modifier: Modifier) {
    Box(modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 620.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("¿Quién va a usar la tablet?", style = MaterialTheme.typography.headlineMedium)
            OpcionPerfil(
                titulo = "Equipo de la escuela",
                detalle = "Docente, UTP o asistente de aula. Ingresa con tu cuenta de prueba.",
                icono = Icons.Filled.Groups,
                color = AzulTinta,
                fondo = AzulHoja,
                estiloTitulo = MaterialTheme.typography.headlineSmall,
                onClick = onEquipo
            )
            OpcionPerfil(
                titulo = "Soy estudiante",
                detalle = "Escribe el código que te dio tu asistente.",
                icono = Icons.Filled.SentimentSatisfied,
                color = NaranjaLapiz,
                fondo = Durazno,
                estiloTitulo = TextoEstudiante.titulo,
                onClick = onEstudiante
            )
            Row(
                Modifier.fillMaxWidth().background(GrisSuave, RoundedCornerShape(14.dp)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Grafito)
                Text(
                    "  Uso solo dentro de la escuela y con un adulto presente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Grafito
                )
            }
        }
    }
}

@Composable
private fun OpcionPerfil(
    titulo: String,
    detalle: String,
    icono: ImageVector,
    color: Color,
    fondo: Color,
    estiloTitulo: TextStyle,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 6.dp),
        modifier = Modifier.fillMaxWidth().border(2.dp, fondo, RoundedCornerShape(24.dp))
    ) {
        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(72.dp).background(fondo, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(38.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 20.dp)) {
                Text(titulo, style = estiloTitulo)
                Text(detalle, style = MaterialTheme.typography.bodyLarge, color = Grafito)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
        }
    }
}
