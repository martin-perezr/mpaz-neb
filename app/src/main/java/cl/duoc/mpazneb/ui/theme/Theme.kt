package cl.duoc.mpazneb.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EsquemaCuaderno = lightColorScheme(
    primary = AzulTinta,
    onPrimary = Color.White,
    primaryContainer = AzulHoja,
    onPrimaryContainer = AzulNoche,
    secondary = NaranjaLapiz,
    onSecondary = Color.White,
    secondaryContainer = Durazno,
    onSecondaryContainer = CafeLectura,
    tertiary = VerdePizarra,
    onTertiary = Color.White,
    tertiaryContainer = Menta,
    onTertiaryContainer = VerdeOscuro,
    background = Papel,
    onBackground = TintaTexto,
    surface = Color.White,
    onSurface = TintaTexto,
    surfaceVariant = GrisSuave,
    onSurfaceVariant = Grafito,
    surfaceContainer = Color.White,
    surfaceContainerLow = Papel,
    surfaceContainerLowest = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = GrisSuave,
    outline = GrisContorno,
    outlineVariant = Borde,
    error = RojoError,
    onError = Color.White,
    errorContainer = RojoErrorFondo,
    onErrorContainer = RojoErrorTexto
)

val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MpazNebTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaCuaderno,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}
