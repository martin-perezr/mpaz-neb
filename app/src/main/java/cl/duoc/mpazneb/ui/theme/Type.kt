package cl.duoc.mpazneb.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import cl.duoc.mpazneb.R

// Andika: diseñada para lectores principiantes (a, g, I, l, 1 no se confunden).
val Andika = FontFamily(
    Font(R.font.andika_regular, FontWeight.Normal),
    Font(R.font.andika_bold, FontWeight.Bold)
)

// Nunito: interfaz de los adultos, redondeada y legible.
val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold)
)

val Tipografia = Typography(
    displaySmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = Nunito, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Nunito, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Nunito, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp)
)

/** Estilos grandes para lo que lee el estudiante de 1.° a 4.° básico. */
object TextoEstudiante {
    val titulo = TextStyle(fontFamily = Andika, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp)
    val lectura = TextStyle(fontFamily = Andika, fontSize = 26.sp, lineHeight = 42.sp)
    val pregunta = TextStyle(fontFamily = Andika, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 40.sp)
    val alternativa = TextStyle(fontFamily = Andika, fontSize = 24.sp, lineHeight = 32.sp)
    val instruccion = TextStyle(fontFamily = Andika, fontSize = 22.sp, lineHeight = 30.sp)
    val boton = TextStyle(fontFamily = Andika, fontWeight = FontWeight.Bold, fontSize = 22.sp)
}
