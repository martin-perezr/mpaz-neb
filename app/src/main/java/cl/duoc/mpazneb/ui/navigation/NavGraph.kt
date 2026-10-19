package cl.duoc.mpazneb.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.mpazneb.model.Rol
import cl.duoc.mpazneb.ui.components.PantallaPendiente
import cl.duoc.mpazneb.ui.screens.auth.BienvenidaScreen
import cl.duoc.mpazneb.ui.screens.auth.LoginScreen

/** Vuelve a Bienvenida borrando todo el historial (al salir o al terminar una sesión). */
private fun NavHostController.irABienvenida() = navigate(Rutas.BIENVENIDA) {
    popUpTo(graph.id) { inclusive = true }
}

@Composable
fun MpazNavHost(nav: NavHostController = rememberNavController()) {
    NavHost(
        navController = nav,
        startDestination = Rutas.BIENVENIDA,
        // Animación de transición entre pantallas: entra deslizando y con fundido.
        enterTransition = { fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 10 } },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(250)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(250)) { it / 10 } }
    ) {
        // ---------- Acceso (Martín y Matías) ----------
        composable(Rutas.BIENVENIDA) {
            BienvenidaScreen(
                onEquipo = { nav.navigate(Rutas.LOGIN) },
                onEstudiante = { nav.navigate(Rutas.CODIGO) }
            )
        }
        composable(Rutas.LOGIN) {
            LoginScreen(
                onIngresado = { rol ->
                    val destino = if (rol == Rol.ADMIN) Rutas.CATALOGO else Rutas.ASISTENTE_INICIO
                    nav.navigate(destino) { popUpTo(Rutas.BIENVENIDA) }
                },
                onVolver = { nav.popBackStack() }
            )
        }
        composable(Rutas.CODIGO) { PantallaPendiente("Código del estudiante") }

        // ---------- Docente / UTP (Matías) ----------
        composable(Rutas.CATALOGO) { PantallaPendiente("Catálogo") }
        composable(Rutas.EDITOR_ACTIVIDAD) { PantallaPendiente("Editor de actividad") }
        composable(Rutas.EDITOR_PREGUNTA) { PantallaPendiente("Editor de pregunta") }

        // ---------- Asistente de aula (Hana) ----------
        composable(Rutas.ASISTENTE_INICIO) { PantallaPendiente("Inicio de la asistente") }
        composable(Rutas.ASIGNAR) { PantallaPendiente("Asignar actividad") }
        composable(Rutas.RESULTADOS) { PantallaPendiente("Resultados") }

        // ---------- Estudiante (Martín) ----------
        composable(Rutas.SESION) { PantallaPendiente("Sesión del estudiante") }
    }
}
