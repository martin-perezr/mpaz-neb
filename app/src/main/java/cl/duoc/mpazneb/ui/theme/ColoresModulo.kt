package cl.duoc.mpazneb.ui.theme

import androidx.compose.ui.graphics.Color
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.EstadoAsignacion
import cl.duoc.mpazneb.model.Modulo

/** Colores de cada módulo: fuerte (botones), fondo (tarjetas) y texto sobre el fondo. */
data class ColoresModulo(val fuerte: Color, val fondo: Color, val texto: Color)

fun coloresDe(modulo: Modulo): ColoresModulo = when (modulo) {
    Modulo.LECTURA -> ColoresModulo(NaranjaLapiz, Durazno, CafeLectura)
    Modulo.MATEMATICA -> ColoresModulo(VerdePizarra, Menta, VerdeOscuro)
}

/** Fondo y texto de las etiquetas de estado. */
fun coloresDe(estado: EstadoActividad): Pair<Color, Color> = when (estado) {
    EstadoActividad.BORRADOR -> Color(0xFFEDEFF3) to Color(0xFF3D4452)
    EstadoActividad.DISPONIBLE -> VerdeLogroFondo to VerdeLogroTexto
    EstadoActividad.DESACTIVADA -> Color(0xFFF4E1E1) to RojoErrorTexto
}

fun coloresDe(estado: EstadoAsignacion): Pair<Color, Color> = when (estado) {
    EstadoAsignacion.PENDIENTE -> AmarilloAnimo to AmarilloTexto
    EstadoAsignacion.EN_DESARROLLO -> AzulHoja to AzulNoche
    EstadoAsignacion.FINALIZADA -> VerdeLogroFondo to VerdeLogroTexto
}
