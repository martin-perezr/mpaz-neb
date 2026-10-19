package cl.duoc.mpazneb.ui.screens.estudiante

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.device.Vibrador
import cl.duoc.mpazneb.device.Voz
import cl.duoc.mpazneb.device.rememberVoz
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.ui.components.BotonEscuchar
import cl.duoc.mpazneb.ui.components.BotonEstudiante
import cl.duoc.mpazneb.ui.components.Cargando
import cl.duoc.mpazneb.ui.components.ChipEstado
import cl.duoc.mpazneb.ui.components.TamanoVentana
import cl.duoc.mpazneb.ui.components.rememberTamanoVentana
import cl.duoc.mpazneb.ui.theme.AmarilloAnimo
import cl.duoc.mpazneb.ui.theme.AmarilloBorde
import cl.duoc.mpazneb.ui.theme.AmarilloTexto
import cl.duoc.mpazneb.ui.theme.AzulHoja
import cl.duoc.mpazneb.ui.theme.AzulNoche
import cl.duoc.mpazneb.ui.theme.AzulTinta
import cl.duoc.mpazneb.ui.theme.Borde
import cl.duoc.mpazneb.ui.theme.ColoresModulo
import cl.duoc.mpazneb.ui.theme.Grafito
import cl.duoc.mpazneb.ui.theme.Menta
import cl.duoc.mpazneb.ui.theme.TextoEstudiante
import cl.duoc.mpazneb.ui.theme.VerdeLogro
import cl.duoc.mpazneb.ui.theme.VerdeLogroFondo
import cl.duoc.mpazneb.ui.theme.VerdeLogroTexto
import cl.duoc.mpazneb.ui.theme.coloresDe
import cl.duoc.mpazneb.viewmodel.Efecto
import cl.duoc.mpazneb.viewmodel.Fase
import cl.duoc.mpazneb.viewmodel.SesionEstudianteViewModel
import cl.duoc.mpazneb.viewmodel.SesionUiState
import coil3.compose.AsyncImage
import java.io.File
import kotlin.math.roundToInt

@Composable
fun SesionEstudianteScreen(
    asignacionId: Long,
    onSalir: () -> Unit,
    vm: SesionEstudianteViewModel = viewModel(factory = SesionEstudianteViewModel.factory(asignacionId))
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val voz = rememberVoz()
    val context = LocalContext.current
    val vibrador = remember { Vibrador(context) }
    var pedirSalida by remember { mutableStateOf(false) }

    // El botón "atrás" del sistema no saca al niño de la actividad: pregunta primero.
    BackHandler(enabled = estado.fase != Fase.FIN) { pedirSalida = true }

    // Efectos de una sola vez → vibración.
    LaunchedEffect(Unit) {
        vm.efectos.collect { efecto ->
            when (efecto) {
                Efecto.ENVIO, Efecto.CASI -> vibrador.pulsoCorto()
                Efecto.ACIERTO -> vibrador.exito()
            }
        }
    }

    val actividad = estado.actividad
    if (estado.fase == Fase.CARGANDO) {
        Cargando()
        return
    }
    if (estado.fase == Fase.ERROR || actividad == null) {
        Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No pudimos abrir la actividad. Pídele ayuda a tu asistente.", style = TextoEstudiante.instruccion, textAlign = TextAlign.Center)
            TextButton(onClick = onSalir) { Text("Volver") }
        }
        return
    }
    val colores = coloresDe(actividad.modulo)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            EncabezadoEstudiante(
                titulo = if (estado.fase == Fase.INICIO || estado.fase == Fase.FIN) actividad.nombre
                else "Pregunta ${estado.indice + 1} de ${estado.total}",
                subtitulo = if (estado.fase == Fase.INICIO) actividad.modulo.etiqueta else actividad.nombre,
                colores = colores,
                progreso = estado.progreso,
                estrellas = estado.estrellas,
                onSalirLargo = { pedirSalida = true }
            )
            // Animación entre pantallas: inicio → cada pregunta → fin, deslizando hacia la izquierda.
            val clave = when (estado.fase) {
                Fase.INICIO -> -1
                Fase.FIN -> Int.MAX_VALUE
                else -> estado.indice
            }
            AnimatedContent(
                targetState = clave,
                transitionSpec = { (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut()) },
                label = "pantallaSesion",
                modifier = Modifier.weight(1f)
            ) { k ->
                when (k) {
                    -1 -> ContenidoInicio(estado, colores, voz, vm::comenzar)
                    Int.MAX_VALUE -> ContenidoFin(estado, onSalir)
                    else -> ContenidoPregunta(estado, colores, voz, vm::seleccionar, vm::pedirConfirmacion)
                }
            }
        }

        // Paneles de retroalimentación que suben desde abajo.
        PanelFeedback(estado, vm::siguiente, vm::reintentar, Modifier.align(Alignment.BottomCenter))
    }

    if (estado.fase == Fase.CONFIRMAR) {
        val elegida = estado.alternativas.firstOrNull { it.id == estado.seleccion }?.texto.orEmpty()
        AlertDialog(
            onDismissRequest = vm::revisar,
            title = { Text("¿Enviamos esta respuesta?", style = TextoEstudiante.pregunta) },
            text = {
                Box(Modifier.fillMaxWidth().background(colores.fondo, RoundedCornerShape(18.dp)).padding(18.dp), contentAlignment = Alignment.Center) {
                    Text(elegida, style = TextoEstudiante.alternativa, color = colores.texto)
                }
            },
            confirmButton = { BotonEstudiante("Sí, enviar", vm::confirmar, colores.fuerte) },
            dismissButton = {
                OutlinedButton(onClick = vm::revisar, modifier = Modifier.height(72.dp), shape = RoundedCornerShape(24.dp)) {
                    Text("Revisar", style = TextoEstudiante.boton)
                }
            }
        )
    }

    if (pedirSalida) {
        AlertDialog(
            onDismissRequest = { pedirSalida = false },
            title = { Text("¿Salir de la actividad?") },
            text = { Text("Solo la asistente puede salir. Lo respondido hasta ahora queda guardado.") },
            confirmButton = { TextButton(onClick = { pedirSalida = false; onSalir() }) { Text("Salir") } },
            dismissButton = { TextButton(onClick = { pedirSalida = false }) { Text("Seguir") } }
        )
    }
}

@Composable
private fun ContenidoInicio(estado: SesionUiState, colores: ColoresModulo, voz: Voz, onComenzar: () -> Unit) {
    val a = estado.actividad ?: return
    val ancha = rememberTamanoVentana() == TamanoVentana.EXPANDIDA
    val instrucciones: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                a.instrucciones, style = TextoEstudiante.instruccion, color = colores.texto,
                modifier = Modifier.fillMaxWidth().background(colores.fondo, RoundedCornerShape(24.dp)).padding(20.dp)
            )
            BotonEscuchar(onClick = { voz.hablar(a.instrucciones) }, texto = "Escuchar instrucciones")
            a.imagenRuta?.let {
                AsyncImage(
                    model = File(it), contentDescription = "Ilustración", contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp))
                )
            }
        }
    }
    Column(Modifier.fillMaxSize()) {
        if (a.modulo == Modulo.LECTURA && a.textoCuerpo != null) {
            if (ancha) {
                Row(Modifier.weight(1f).padding(24.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    Column(Modifier.width(340.dp).verticalScroll(rememberScrollState())) { instrucciones() }
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        Text(a.textoTitulo.orEmpty(), style = TextoEstudiante.titulo, modifier = Modifier.padding(bottom = 12.dp))
                        CuadernoLineas(a.textoCuerpo, Modifier.fillMaxWidth())
                    }
                }
            } else {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    instrucciones()
                    Text(a.textoTitulo.orEmpty(), style = TextoEstudiante.titulo)
                    CuadernoLineas(a.textoCuerpo, Modifier.fillMaxWidth())
                }
            }
        } else {
            Box(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(Modifier.widthIn(max = 640.dp)) { instrucciones() }
            }
        }
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (a.modulo == Modulo.LECTURA && a.textoCuerpo != null) {
                BotonEscuchar(onClick = { voz.hablar(a.textoCuerpo) }, texto = "Escuchar el texto")
            }
            BotonEstudiante(
                if (a.modulo == Modulo.LECTURA) "Ya leí, vamos" else "Comenzar",
                onComenzar, colores.fuerte, icono = androidx.compose.material.icons.Icons.AutoMirrored.Filled.Send
            )
        }
    }
}

@Composable
private fun ContenidoPregunta(
    estado: SesionUiState,
    colores: ColoresModulo,
    voz: Voz,
    onSeleccionar: (Long) -> Unit,
    onEnviar: () -> Unit
) {
    val pregunta = estado.preguntaActual?.pregunta ?: return
    val actividad = estado.actividad ?: return
    val tamano = rememberTamanoVentana()
    var verTexto by remember { mutableStateOf(tamano != TamanoVentana.COMPACTA) }
    val texto = actividad.textoCuerpo

    val panelTexto: @Composable (Modifier) -> Unit = { mod ->
        if (actividad.modulo == Modulo.LECTURA && texto != null) {
            Column(mod, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { verTexto = !verTexto }) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = colores.texto)
                    Text(if (verTexto) "  Ocultar el texto" else "  Volver a leer el texto", style = MaterialTheme.typography.titleMedium, color = colores.texto)
                    Icon(if (verTexto) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = colores.texto)
                }
                AnimatedVisibility(visible = verTexto) {
                    CuadernoLineas(texto, Modifier.fillMaxWidth(), tamanoLetra = 20, interlineado = 32)
                }
            }
        }
    }

    val zonaPregunta: @Composable (Modifier) -> Unit = { mod ->
        Column(mod, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipEstado(pregunta.tipo.etiqueta, colores.fondo, colores.texto)
                    Text(pregunta.enunciado, style = TextoEstudiante.pregunta)
                }
                BotonEscuchar(onClick = {
                    voz.hablar(pregunta.enunciado + ". " + estado.alternativas.joinToString(". ") { it.texto })
                }, texto = "")
            }
            if (actividad.modulo == Modulo.MATEMATICA && pregunta.numeroA != null && pregunta.numeroB != null) {
                BloquesDecenas(
                    pregunta.numeroA, pregunta.numeroB, pregunta.operacion ?: "+",
                    Modifier.background(Menta, RoundedCornerShape(20.dp)).padding(4.dp)
                )
            }
            estado.alternativas.forEachIndexed { i, alt ->
                val estadoAlt = when {
                    (estado.fase == Fase.ACIERTO || estado.fase == Fase.REVELAR) && alt.esCorrecta -> EstadoAlternativa.CORRECTA
                    estado.fase == Fase.CASI && alt.id == estado.seleccion -> EstadoAlternativa.CASI
                    alt.id in estado.descartadas -> EstadoAlternativa.DESCARTADA
                    alt.id == estado.seleccion -> EstadoAlternativa.SELECCIONADA
                    else -> EstadoAlternativa.NORMAL
                }
                AlternativaBoton('A' + i, alt.texto, estadoAlt, colores, onClick = { onSeleccionar(alt.id) })
            }
            // Espacio para que el panel de feedback no tape la última alternativa.
            Box(Modifier.height(200.dp))
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (tamano == TamanoVentana.EXPANDIDA) {
            Row(Modifier.weight(1f).padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                panelTexto(Modifier.width(380.dp).verticalScroll(rememberScrollState()))
                zonaPregunta(Modifier.weight(1f).verticalScroll(rememberScrollState()))
            }
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                panelTexto(Modifier.fillMaxWidth())
                zonaPregunta(Modifier.fillMaxWidth())
            }
        }
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Intento ${estado.intento} de ${estado.intentosPermitidos}",
                style = MaterialTheme.typography.titleMedium, color = Grafito, modifier = Modifier.weight(1f)
            )
            BotonEstudiante(
                "Enviar", onEnviar, colores.fuerte,
                enabled = estado.seleccion != null && estado.fase == Fase.PREGUNTA,
                icono = Icons.AutoMirrored.Filled.Send
            )
        }
    }
}

@Composable
private fun PanelFeedback(estado: SesionUiState, onSiguiente: () -> Unit, onReintentar: () -> Unit, modifier: Modifier) {
    val pregunta = estado.preguntaActual?.pregunta
    val visible = estado.fase == Fase.ACIERTO || estado.fase == Fase.CASI || estado.fase == Fase.REVELAR
    AnimatedVisibility(
        visible = visible && pregunta != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        val textoBoton = if (estado.esUltima) "Terminar" else "Siguiente"
        when (estado.fase) {
            Fase.ACIERTO -> Panel(VerdeLogroFondo, VerdeLogro) {
                Row { EstrellaAnimada(0); EstrellaAnimada(150); EstrellaAnimada(300) }
                Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    Text("¡Muy bien!", style = TextoEstudiante.pregunta, color = VerdeLogroTexto)
                    Text(pregunta?.retroalimentacion.orEmpty(), style = TextoEstudiante.instruccion, color = VerdeLogroTexto)
                }
                BotonEstudiante(textoBoton, onSiguiente, VerdeLogro)
            }
            Fase.CASI -> {
                // Animación: el ícono se sacude suavemente para invitar a intentar de nuevo.
                val sacudida = remember { Animatable(0f) }
                LaunchedEffect(Unit) { for (x in listOf(-12f, 12f, -8f, 8f, 0f)) sacudida.animateTo(x, tween(60)) }
                Panel(AmarilloAnimo, AmarilloBorde) {
                    Box(
                        Modifier.offset { IntOffset(sacudida.value.roundToInt(), 0) }.size(72.dp).background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.Refresh, contentDescription = null, tint = AmarilloTexto, modifier = Modifier.size(40.dp)) }
                    Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                        Text("¡Casi! Tú puedes", style = TextoEstudiante.pregunta, color = AmarilloTexto)
                        Text(pregunta?.pista.orEmpty(), style = TextoEstudiante.instruccion, color = AmarilloTexto)
                    }
                    BotonEstudiante("Intentar otra vez", onReintentar, AmarilloTexto)
                }
            }
            else -> Panel(AzulHoja, AzulTinta) {
                Column(Modifier.weight(1f).padding(end = 16.dp)) {
                    Text("Esta vez no resultó, ¡y está bien!", style = TextoEstudiante.pregunta, color = AzulNoche)
                    Text(
                        "La respuesta era: ${estado.correcta?.texto.orEmpty()}. " + (pregunta?.procedimiento?.let { "($it) " } ?: "") + "¡Sigamos!",
                        style = TextoEstudiante.instruccion, color = AzulNoche
                    )
                }
                BotonEstudiante(textoBoton, onSiguiente, AzulTinta)
            }
        }
    }
}

@Composable
private fun Panel(fondo: Color, borde: Color, contenido: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(fondo)) {
        Box(Modifier.fillMaxWidth().height(4.dp).background(borde))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = contenido
        )
    }
}

@Composable
private fun ContenidoFin(estado: SesionUiState, onSalir: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            repeat(estado.total) { i -> EstrellaAnimada(150L * i, if (i == estado.total / 2) 120 else 96, ganada = i < estado.estrellas) }
        }
        Text("¡Terminaste!", style = TextoEstudiante.pregunta.copy(fontSize = 52.sp, lineHeight = 60.sp))
        Text(
            "Respondiste las ${estado.total} preguntas y ganaste ${estado.estrellas} estrella(s).\n¡Cada vez lo haces mejor!",
            style = TextoEstudiante.instruccion.copy(fontSize = 26.sp, lineHeight = 36.sp), textAlign = TextAlign.Center, color = Grafito
        )
        Row(
            Modifier.background(AzulHoja, RoundedCornerShape(20.dp)).padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Groups, contentDescription = null, tint = AzulNoche, modifier = Modifier.size(30.dp))
            Text("  Ahora entrega la tablet a tu asistente.", style = TextoEstudiante.instruccion, color = AzulNoche)
        }
        OutlinedButton(onClick = onSalir, shape = RoundedCornerShape(999.dp), modifier = Modifier.height(56.dp)) {
            Text("Asistente: terminar sesión", style = MaterialTheme.typography.labelLarge)
        }
        Box(Modifier.size(1.dp).background(Borde))
    }
}
