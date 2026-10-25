package cl.duoc.mpazneb.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.mpazneb.model.Rol
import cl.duoc.mpazneb.ui.screens.admin.CatalogoScreen
import cl.duoc.mpazneb.ui.screens.admin.EditorActividadScreen
import cl.duoc.mpazneb.ui.screens.admin.EditorPreguntaScreen
import cl.duoc.mpazneb.ui.screens.asistente.AsignarScreen
import cl.duoc.mpazneb.ui.screens.asistente.AsistenteInicioScreen
import cl.duoc.mpazneb.ui.screens.asistente.ResultadosScreen
import cl.duoc.mpazneb.ui.screens.auth.BienvenidaScreen
import cl.duoc.mpazneb.ui.screens.auth.LoginScreen
import cl.duoc.mpazneb.ui.screens.estudiante.CodigoScreen
import cl.duoc.mpazneb.ui.screens.estudiante.SesionEstudianteScreen

/** Vuelve a Bienvenida borrando todo el historial (al salir o al terminar una sesión). */
private fun NavHostController.irABienvenida() = navigate(Rutas.BIENVENIDA) {
    popUpTo(graph.id) { inclusive = true }
}

@Composable
fun MpazNavHost(nav: NavHostController = rememberNavController()) {
    NavHost(
        navController = nav,
        startDestination = Rutas.BIENVENIDA,
        enterTransition = { fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 10 } },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(250)) },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(250)) { it / 10 } }
    ) {
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
        composable(Rutas.CODIGO) {
            CodigoScreen(
                onCodigoValido = { id ->
                    nav.navigate(Rutas.sesion(id)) { popUpTo(Rutas.CODIGO) { inclusive = true } }
                },
                onVolver = { nav.popBackStack() }
            )
        }

        composable(Rutas.CATALOGO) {
            CatalogoScreen(
                onNueva = { nav.navigate(Rutas.editorActividad(0)) },
                onEditar = { id -> nav.navigate(Rutas.editorActividad(id)) },
                onSalir = { nav.irABienvenida() }
            )
        }
        composable(
            Rutas.EDITOR_ACTIVIDAD,
            arguments = listOf(navArgument("actividadId") { type = NavType.LongType })
        ) { entrada ->
            val actividadId = entrada.arguments?.getLong("actividadId") ?: 0L
            EditorActividadScreen(
                actividadId = actividadId,
                onCreada = { id ->
                    nav.navigate(Rutas.editorActividad(id)) {
                        popUpTo(Rutas.EDITOR_ACTIVIDAD) { inclusive = true }
                    }
                },
                onEditarPregunta = { preguntaId -> nav.navigate(Rutas.editorPregunta(actividadId, preguntaId)) },
                onNuevaPregunta = { nav.navigate(Rutas.editorPregunta(actividadId, 0)) },
                onVolver = { nav.popBackStack() }
            )
        }
        composable(
            Rutas.EDITOR_PREGUNTA,
            arguments = listOf(
                navArgument("actividadId") { type = NavType.LongType },
                navArgument("preguntaId") { type = NavType.LongType }
            )
        ) { entrada ->
            EditorPreguntaScreen(
                actividadId = entrada.arguments?.getLong("actividadId") ?: 0L,
                preguntaId = entrada.arguments?.getLong("preguntaId") ?: 0L,
                onGuardada = { nav.popBackStack() },
                onVolver = { nav.popBackStack() }
            )
        }

        composable(Rutas.ASISTENTE_INICIO) {
            AsistenteInicioScreen(
                onAsignar = { id -> nav.navigate(Rutas.asignar(id)) },
                onResultados = { id -> nav.navigate(Rutas.resultados(id)) },
                onSalir = { nav.irABienvenida() }
            )
        }
        composable(
            Rutas.ASIGNAR,
            arguments = listOf(navArgument("actividadId") { type = NavType.LongType })
        ) { entrada ->
            AsignarScreen(
                actividadId = entrada.arguments?.getLong("actividadId") ?: 0L,
                onVolver = { nav.popBackStack() }
            )
        }
        composable(
            Rutas.RESULTADOS,
            arguments = listOf(navArgument("asignacionId") { type = NavType.LongType })
        ) { entrada ->
            ResultadosScreen(
                asignacionId = entrada.arguments?.getLong("asignacionId") ?: 0L,
                onVolver = { nav.popBackStack() }
            )
        }

        composable(
            Rutas.SESION,
            arguments = listOf(navArgument("asignacionId") { type = NavType.LongType })
        ) { entrada ->
            SesionEstudianteScreen(
                asignacionId = entrada.arguments?.getLong("asignacionId") ?: 0L,
                onSalir = { nav.irABienvenida() }
            )
        }
    }
}
