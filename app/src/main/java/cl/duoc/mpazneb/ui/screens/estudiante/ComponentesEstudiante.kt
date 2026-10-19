package cl.duoc.mpazneb.ui.screens.estudiante

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.mpazneb.ui.components.BarraAvance
import cl.duoc.mpazneb.ui.theme.AmarilloAnimo
import cl.duoc.mpazneb.ui.theme.AmarilloBorde
import cl.duoc.mpazneb.ui.theme.Andika
import cl.duoc.mpazneb.ui.theme.Borde
import cl.duoc.mpazneb.ui.theme.ColoresModulo
import cl.duoc.mpazneb.ui.theme.Estrella
import cl.duoc.mpazneb.ui.theme.Grafito
import cl.duoc.mpazneb.ui.theme.GrisSuave
import cl.duoc.mpazneb.ui.theme.LineaCuaderno
import cl.duoc.mpazneb.ui.theme.MargenCuaderno
import cl.duoc.mpazneb.ui.theme.TextoEstudiante
import cl.duoc.mpazneb.ui.theme.TintaTexto
import cl.duoc.mpazneb.ui.theme.VerdeBloque
import cl.duoc.mpazneb.ui.theme.VerdeLogro
import cl.duoc.mpazneb.ui.theme.VerdeLogroFondo
import cl.duoc.mpazneb.ui.theme.VerdePizarra

/** Barra superior del estudiante: candado (solo asistente), título, avance y estrellas. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EncabezadoEstudiante(
    titulo: String,
    subtitulo: String,
    colores: ColoresModulo,
    progreso: Float,
    estrellas: Int,
    onSalirLargo: () -> Unit
) {
    Column(Modifier.fillMaxWidth().background(colores.fuerte).padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // Mantener presionado: así un niño no sale por accidente.
            Box(
                Modifier.size(48.dp).background(Color.White.copy(alpha = 0.16f), CircleShape)
                    .combinedClickable(onClick = {}, onLongClick = onSalirLargo, onClickLabel = "Salir (mantener presionado)"),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Lock, contentDescription = "Salir, solo asistente", tint = Color.White) }
            Column(Modifier.weight(1f)) {
                Text(titulo, style = TextoEstudiante.titulo, color = Color.White)
                Text(subtitulo, style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.9f))
            }
            Row(
                Modifier.background(Color.White.copy(alpha = 0.16f), CircleShape).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Estrella)
                Text(" $estrellas", style = TextoEstudiante.boton, color = Color.White)
            }
        }
        BarraAvance(progreso, Color.White, Color.White.copy(alpha = 0.25f), Modifier.padding(top = 12.dp))
    }
}

/**
 * Hoja de cuaderno con líneas y margen rojo dibujados detrás del texto.
 * El interlineado del texto coincide con la separación de las líneas.
 */
@Composable
fun CuadernoLineas(texto: String, modifier: Modifier = Modifier, tamanoLetra: Int = 26, interlineado: Int = 42) {
    val espacio = with(LocalDensity.current) { interlineado.sp.toPx() }
    val margen = with(LocalDensity.current) { 44.dp.toPx() }
    val arriba = with(LocalDensity.current) { 16.dp.toPx() }
    Box(
        modifier
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(2.dp, Borde, RoundedCornerShape(20.dp))
            .drawBehind {
                var y = arriba + espacio
                while (y < size.height) {
                    drawLine(LineaCuaderno, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
                    y += espacio
                }
                drawLine(MargenCuaderno, Offset(margen, 0f), Offset(margen, size.height), strokeWidth = 4f)
            }
            .padding(start = 60.dp, end = 24.dp, top = 16.dp, bottom = 24.dp)
    ) {
        Text(texto, style = TextoEstudiante.lectura.copy(fontSize = tamanoLetra.sp, lineHeight = interlineado.sp), color = TintaTexto)
    }
}

enum class EstadoAlternativa { NORMAL, SELECCIONADA, CASI, CORRECTA, DESCARTADA }

/** Alternativa grande. Cambia de color con animación y se encoge al presionarla. */
@Composable
fun AlternativaBoton(
    letra: Char,
    texto: String,
    estado: EstadoAlternativa,
    colores: ColoresModulo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borde by animateColorAsState(
        when (estado) {
            EstadoAlternativa.SELECCIONADA -> colores.fuerte
            EstadoAlternativa.CASI -> AmarilloBorde
            EstadoAlternativa.CORRECTA -> VerdeLogro
            else -> Borde
        }, label = "bordeAlternativa"
    )
    val fondo by animateColorAsState(
        when (estado) {
            EstadoAlternativa.SELECCIONADA -> colores.fondo
            EstadoAlternativa.CASI -> AmarilloAnimo
            EstadoAlternativa.CORRECTA -> VerdeLogroFondo
            EstadoAlternativa.DESCARTADA -> GrisSuave
            EstadoAlternativa.NORMAL -> Color.White
        }, label = "fondoAlternativa"
    )
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionado) 0.97f else 1f, label = "escalaAlternativa")
    OutlinedButton(
        onClick = onClick,
        enabled = estado != EstadoAlternativa.DESCARTADA,
        interactionSource = interaccion,
        modifier = modifier.fillMaxWidth().heightIn(min = 80.dp).scale(escala),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(3.dp, borde),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            containerColor = fondo, contentColor = TintaTexto,
            disabledContainerColor = GrisSuave, disabledContentColor = Grafito
        )
    ) {
        Box(
            Modifier.size(48.dp).background(
                when (estado) {
                    EstadoAlternativa.SELECCIONADA -> colores.fuerte
                    EstadoAlternativa.CORRECTA -> VerdeLogro
                    else -> GrisSuave
                }, RoundedCornerShape(14.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                letra.toString(), fontFamily = Andika, fontSize = 24.sp,
                color = if (estado == EstadoAlternativa.SELECCIONADA || estado == EstadoAlternativa.CORRECTA) Color.White else TintaTexto
            )
        }
        Text(texto, style = TextoEstudiante.alternativa, modifier = Modifier.weight(1f).padding(start = 18.dp))
        if (estado == EstadoAlternativa.CORRECTA) {
            Icon(Icons.Filled.Check, contentDescription = "Correcta", tint = VerdeLogro, modifier = Modifier.size(34.dp))
        }
    }
}

/** Estrella que aparece con un rebote (resorte) después de [retrasoMs]. */
@Composable
fun EstrellaAnimada(retrasoMs: Long, tamano: Int = 56, ganada: Boolean = true) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(retrasoMs)
        visible = true
    }
    val escala by animateFloatAsState(
        if (visible) 1f else 0f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "estrella"
    )
    Icon(
        Icons.Filled.Star, contentDescription = null,
        tint = if (ganada) Estrella else Borde,
        modifier = Modifier.size(tamano.dp).scale(escala)
    )
}

/** Bloques de decenas y unidades para apoyar el cálculo (números hasta 99). */
@Composable
fun BloquesDecenas(a: Int, b: Int, operacion: String, modifier: Modifier = Modifier) {
    Row(
        modifier.background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(18.dp)).padding(18.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        GrupoBloques(a)
        Text(operacion, fontFamily = Andika, fontSize = 44.sp, color = VerdePizarra, modifier = Modifier.padding(bottom = 36.dp))
        GrupoBloques(b)
        Text("=", fontFamily = Andika, fontSize = 44.sp, color = VerdePizarra, modifier = Modifier.padding(bottom = 36.dp))
        Box(
            Modifier.padding(bottom = 30.dp).size(80.dp).border(3.dp, VerdePizarra, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) { Text("?", fontFamily = Andika, fontSize = 40.sp, color = VerdePizarra) }
    }
}

@Composable
private fun GrupoBloques(n: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (n in 0..99) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(n / 10) { Box(Modifier.width(16.dp).height(110.dp).background(VerdePizarra, RoundedCornerShape(4.dp))) }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat((n % 10 + 1) / 2) { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(if (fila * 2 + 1 < n % 10) 2 else 1) {
                                Box(Modifier.size(16.dp).background(VerdeBloque, RoundedCornerShape(4.dp)))
                            }
                        }
                    }
                }
            }
        }
        Text("$n", fontFamily = Andika, fontSize = 28.sp, color = TintaTexto)
    }
}
