package cl.duoc.mpazneb.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.mpazneb.ui.theme.AzulTinta
import cl.duoc.mpazneb.ui.theme.GrisDeshabilitado
import cl.duoc.mpazneb.ui.theme.Grafito
import cl.duoc.mpazneb.ui.theme.TextoEstudiante
import cl.duoc.mpazneb.ui.theme.RojoErrorFondo
import cl.duoc.mpazneb.ui.theme.RojoErrorTexto
import cl.duoc.mpazneb.ui.theme.VerdeLogro
import cl.duoc.mpazneb.ui.theme.VerdeLogroFondo
import cl.duoc.mpazneb.ui.theme.VerdeLogroTexto

/**
 * Campo de texto con validación visual: borde rojo + ícono de error + mensaje bajo el campo,
 * o borde verde + check cuando es válido. El mensaje lo calcula un validador, no este componente.
 */
@Composable
fun CampoValidado(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    modifier: Modifier = Modifier,
    iconoInicio: ImageVector? = null,
    ayuda: String? = null,
    maxCaracteres: Int? = null,
    mostrarValido: Boolean = false,
    unaLinea: Boolean = true,
    lineasMin: Int = 1,
    teclado: KeyboardOptions = KeyboardOptions.Default,
    transformacion: VisualTransformation = VisualTransformation.None,
    accionFinal: (@Composable () -> Unit)? = null
) {
    val esValido = mostrarValido && error == null && valor.isNotBlank()
    val colorBorde = if (esValido) VerdeLogro else MaterialTheme.colorScheme.outline
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        modifier = modifier.fillMaxWidth(),
        isError = error != null,
        singleLine = unaLinea,
        minLines = lineasMin,
        keyboardOptions = teclado,
        visualTransformation = transformacion,
        shape = MaterialTheme.shapes.small,
        leadingIcon = iconoInicio?.let { icono -> { Icon(icono, contentDescription = null) } },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    error != null -> Icon(
                        Icons.Filled.Error,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    esValido -> Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Campo válido",
                        tint = VerdeLogro,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                accionFinal?.invoke()
            }
        },
        supportingText = {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = error ?: ayuda.orEmpty(),
                    modifier = Modifier.weight(1f),
                    color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (maxCaracteres != null) {
                    Text(
                        text = "${valor.length} / $maxCaracteres",
                        color = if (valor.length > maxCaracteres) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (esValido) VerdeLogro else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = colorBorde
        )
    )
}

/** Botón principal de los adultos (48 dp mínimo). */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icono: ImageVector? = null,
    color: Color = AzulTinta
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 52.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = GrisDeshabilitado,
            disabledContentColor = Grafito
        ),
        contentPadding = PaddingValues(horizontal = 22.dp)
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Botón grande para el estudiante (72 dp). Se encoge un poco al presionarlo:
 * es una animación de retroalimentación táctil.
 */
@Composable
fun BotonEstudiante(
    texto: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icono: ImageVector? = null
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionado) 0.96f else 1f, label = "escalaBoton")
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaccion,
        modifier = modifier
            .heightIn(min = 72.dp)
            .scale(escala),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = GrisDeshabilitado,
            disabledContentColor = Grafito
        ),
        contentPadding = PaddingValues(horizontal = 28.dp)
    ) {
        Text(texto, style = TextoEstudiante.boton)
        if (icono != null) {
            Spacer(Modifier.width(12.dp))
            Icon(icono, contentDescription = null, modifier = Modifier.size(28.dp))
        }
    }
}

/** Botón "Escuchar" que activa la síntesis de voz. */
@Composable
fun BotonEscuchar(onClick: () -> Unit, modifier: Modifier = Modifier, texto: String = "Escuchar") {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 64.dp),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(28.dp))
        if (texto.isNotEmpty()) {
            Spacer(Modifier.width(10.dp))
            Text(texto, style = TextoEstudiante.boton.copy(color = AzulTinta))
        }
    }
}

/** Etiqueta de estado (Borrador, Disponible, Pendiente, etc.). */
@Composable
fun ChipEstado(texto: String, fondo: Color, tinta: Color, modifier: Modifier = Modifier) {
    Surface(color = fondo, shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Text(
            texto,
            color = tinta,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Barra de avance que se llena con animación cada vez que cambia el progreso. */
@Composable
fun BarraAvance(progreso: Float, color: Color, fondo: Color, modifier: Modifier = Modifier) {
    val animado by animateFloatAsState(progreso.coerceIn(0f, 1f), tween(500), label = "avance")
    LinearProgressIndicator(
        progress = { animado },
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(CircleShape),
        color = color,
        trackColor = fondo,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}

/** Pantalla de carga simple. */
@Composable
fun Cargando(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Mensaje de éxito o error que aparece con una animación corta. */
@Composable
fun Aviso(texto: String?, esError: Boolean, modifier: Modifier = Modifier) {
    val duracion = duracionAnimacion(220)
    AnimatedVisibility(visible = !texto.isNullOrBlank(), modifier = modifier, enter = fadeIn(tween(duracion))) {
        Text(
            text = texto.orEmpty(),
            color = if (esError) RojoErrorTexto else VerdeLogroTexto,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .background(if (esError) RojoErrorFondo else VerdeLogroFondo, RoundedCornerShape(12.dp))
                .padding(12.dp)
        )
    }
}

/** Marcador temporal para rutas que otro integrante aún no termina. */
@Composable
fun PantallaPendiente(nombre: String) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(nombre, style = MaterialTheme.typography.headlineSmall)
        Text("En construcción", style = MaterialTheme.typography.bodyLarge, color = Grafito)
    }
}
