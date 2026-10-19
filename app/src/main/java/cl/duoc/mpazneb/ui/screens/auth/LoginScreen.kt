package cl.duoc.mpazneb.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.model.Rol
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.CampoValidado
import cl.duoc.mpazneb.ui.theme.Grafito
import cl.duoc.mpazneb.ui.theme.GrisSuave
import cl.duoc.mpazneb.ui.theme.RojoErrorFondo
import cl.duoc.mpazneb.ui.theme.RojoErrorTexto
import cl.duoc.mpazneb.viewmodel.LoginViewModel
import kotlin.math.roundToInt

@Composable
fun LoginScreen(
    onIngresado: (Rol) -> Unit,
    onVolver: () -> Unit,
    vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()

    // Navega cuando el ViewModel indica el destino (evento de una sola vez).
    LaunchedEffect(estado.destino) {
        estado.destino?.let {
            onIngresado(it)
            vm.navegacionHecha()
        }
    }

    // Animación: la tarjeta tiembla cuando el login falla.
    val sacudida = remember { Animatable(0f) }
    LaunchedEffect(estado.intentosFallidos) {
        if (estado.intentosFallidos > 0) {
            for (x in listOf(-14f, 14f, -9f, 9f, -4f, 0f)) sacudida.animateTo(x, tween(45))
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
            Text("MPAZ NEB", style = MaterialTheme.typography.titleLarge)
        }
        Box(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(Modifier.widthIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ingreso del equipo", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Usa tu cuenta de prueba. Tu perfil (Docente/UTP o Asistente) define qué puedes hacer.",
                    style = MaterialTheme.typography.bodyLarge, color = Grafito
                )
                Card(
                    modifier = Modifier.offset { IntOffset(sacudida.value.roundToInt(), 0) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoValidado(
                            valor = estado.alias,
                            onCambio = vm::cambiarAlias,
                            etiqueta = "Alias",
                            error = estado.errorAlias,
                            iconoInicio = Icons.Filled.Person,
                            ayuda = "Entre 4 y 20 caracteres, sin espacios",
                            mostrarValido = true,
                            teclado = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        CampoValidado(
                            valor = estado.clave,
                            onCambio = vm::cambiarClave,
                            etiqueta = "Contraseña",
                            error = estado.errorClave,
                            iconoInicio = Icons.Filled.Lock,
                            mostrarValido = true,
                            teclado = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            transformacion = if (estado.mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                            accionFinal = {
                                IconButton(onClick = vm::alternarClave) {
                                    Icon(
                                        if (estado.mostrarClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (estado.mostrarClave) "Ocultar contraseña" else "Mostrar contraseña"
                                    )
                                }
                            }
                        )
                        AnimatedVisibility(
                            visible = estado.mensaje != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                Modifier.fillMaxWidth().background(RojoErrorFondo, RoundedCornerShape(12.dp)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = RojoErrorTexto)
                                Text("  ${estado.mensaje.orEmpty()}", color = RojoErrorTexto, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                        if (estado.cargando) {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(32.dp))
                            }
                        } else {
                            BotonPrincipal(
                                texto = "Ingresar",
                                onClick = vm::ingresar,
                                enabled = estado.puedeEnviar || !estado.intentoEnviar,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Text(
                            "3 intentos fallidos bloquean el ingreso por 1 minuto.",
                            style = MaterialTheme.typography.bodySmall, color = Grafito,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
                Column(
                    Modifier.fillMaxWidth().background(GrisSuave, RoundedCornerShape(14.dp)).padding(16.dp)
                ) {
                    Text("Cuentas ficticias de prueba", style = MaterialTheme.typography.titleSmall)
                    Text("utp.demo · Utp2026 (Docente / UTP)", style = MaterialTheme.typography.bodyMedium, color = Grafito)
                    Text("asistente.aula2 · Aula2026 (Asistente)", style = MaterialTheme.typography.bodyMedium, color = Grafito)
                }
            }
        }
    }
}
