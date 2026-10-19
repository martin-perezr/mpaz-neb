package cl.duoc.mpazneb.ui.screens.estudiante

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.device.rememberVoz
import cl.duoc.mpazneb.ui.components.BotonEscuchar
import cl.duoc.mpazneb.ui.components.TamanoVentana
import cl.duoc.mpazneb.ui.components.rememberTamanoVentana
import cl.duoc.mpazneb.ui.theme.AmarilloAnimo
import cl.duoc.mpazneb.ui.theme.AmarilloBorde
import cl.duoc.mpazneb.ui.theme.AmarilloTexto
import cl.duoc.mpazneb.ui.theme.Andika
import cl.duoc.mpazneb.ui.theme.Borde
import cl.duoc.mpazneb.ui.theme.Durazno
import cl.duoc.mpazneb.ui.theme.Grafito
import cl.duoc.mpazneb.ui.theme.GrisDeshabilitado
import cl.duoc.mpazneb.ui.theme.GrisSuave
import cl.duoc.mpazneb.ui.theme.NaranjaLapiz
import cl.duoc.mpazneb.ui.theme.TextoEstudiante
import cl.duoc.mpazneb.ui.theme.TintaTexto
import cl.duoc.mpazneb.viewmodel.CodigoViewModel
import kotlin.math.roundToInt

@Composable
fun CodigoScreen(
    onCodigoValido: (asignacionId: Long) -> Unit,
    onVolver: () -> Unit,
    vm: CodigoViewModel = viewModel(factory = CodigoViewModel.Factory)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val voz = rememberVoz()
    val tamano = rememberTamanoVentana()

    LaunchedEffect(estado.asignacionId) {
        estado.asignacionId?.let {
            onCodigoValido(it)
            vm.navegacionHecha()
        }
    }

    val sacudida = remember { Animatable(0f) }
    LaunchedEffect(estado.errores) {
        if (estado.errores > 0) for (x in listOf(-16f, 16f, -10f, 10f, 0f)) sacudida.animateTo(x, tween(50))
    }

    val textoYCasillas: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("¡Hola! Escribe tu código", style = TextoEstudiante.pregunta.copy(fontSize = 38.sp, lineHeight = 46.sp))
            Text("Son 4 números. Tu asistente te los muestra.", style = TextoEstudiante.instruccion, color = Grafito)
            BotonEscuchar(onClick = { voz.hablar("Hola. Escribe tu código. Son cuatro números. Tu asistente te los muestra.") })
            Row(
                Modifier.offset { IntOffset(sacudida.value.roundToInt(), 0) },
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                repeat(4) { i -> CasillaDigito(estado.codigo.getOrNull(i), activa = i == estado.codigo.length, conError = estado.mensaje != null) }
            }
            AnimatedVisibility(visible = estado.mensaje != null, enter = scaleIn()) {
                Row(
                    Modifier.background(AmarilloAnimo, RoundedCornerShape(20.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = AmarilloTexto, modifier = Modifier.size(28.dp))
                    Text("  ${estado.mensaje.orEmpty()}", style = TextoEstudiante.instruccion, color = AmarilloTexto)
                }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
            Text("MPAZ NEB", style = MaterialTheme.typography.titleLarge)
        }
        if (tamano == TamanoVentana.EXPANDIDA) {
            Row(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(460.dp)) { textoYCasillas() }
                Teclado(estado.completo, vm::tecla, vm::borrar, vm::entrar)
            }
        } else {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                textoYCasillas()
                Teclado(estado.completo, vm::tecla, vm::borrar, vm::entrar)
            }
        }
    }
}

@Composable
private fun CasillaDigito(digito: Char?, activa: Boolean, conError: Boolean) {
    val borde by animateColorAsState(
        when {
            conError -> AmarilloBorde
            activa -> NaranjaLapiz
            else -> Durazno
        }, label = "bordeCasilla"
    )
    Box(
        Modifier.size(width = 76.dp, height = 96.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(3.dp, borde, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(digito?.toString().orEmpty(), fontFamily = Andika, fontSize = 48.sp, color = TintaTexto)
    }
}

@Composable
private fun Teclado(completo: Boolean, onTecla: (Char) -> Unit, onBorrar: () -> Unit, onEntrar: () -> Unit) {
    val filas = listOf("123", "456", "789")
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        filas.forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                fila.forEach { d -> TeclaNumero(d.toString()) { onTecla(d) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedButton(
                onClick = onBorrar,
                modifier = Modifier.size(96.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Borde),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = GrisSuave, contentColor = Grafito)
            ) { Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Borrar", modifier = Modifier.size(34.dp)) }
            TeclaNumero("0") { onTecla('0') }
            Button(
                onClick = onEntrar,
                modifier = Modifier.size(96.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (completo) NaranjaLapiz else GrisDeshabilitado,
                    contentColor = if (completo) Color.White else Grafito
                )
            ) { Icon(Icons.Filled.Check, contentDescription = "Entrar", modifier = Modifier.size(40.dp)) }
        }
    }
}

@Composable
private fun TeclaNumero(texto: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(96.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, Durazno),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = TintaTexto)
    ) {
        Text(texto, fontFamily = Andika, fontSize = 40.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().height(52.dp))
    }
}
