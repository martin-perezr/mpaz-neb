# Defensa individual de 15 minutos

No uses IA. Abre el proyecto antes de que te llamen. El emulador tablet en horizontal debe estar encendido.

## Minuto 0 a 2 — Ejecutar

1. Run en la tablet horizontal.
2. Muestra la bienvenida.
3. Di que hay tres perfiles y que los datos son ficticios.
4. Entra con `utp.demo` / `Utp2026` o con `asistente.aula2` / `Aula2026`.
5. Si el login entra solo, es porque la sesión quedó guardada. Pulsa **Salir** para mostrar el formulario.

## Minuto 2 a 4 — Arquitectura

Abre `ARQUITECTURA.md` solo si lo necesitas. En el código señala:

- `CatalogoScreen`: dibuja.
- `CatalogoViewModel`: tiene el `StateFlow`.
- `ActividadRepository`: habla con Room y revisa que el rol sea administración.
- `ActividadDao`: la consulta.

Frase útil: “La pantalla no guarda nada. Avisa al ViewModel, el ViewModel llama al repositorio y Room devuelve un flujo que actualiza la pantalla”.

## Minuto 4 a 6 — Tablet y Material 3

1. En el catálogo o en la sesión de lectura, muestra dos columnas.
2. Gira a vertical o cambia a un teléfono: pasa a una columna.
3. Abre `Theme.kt` y di que los colores están en un `lightColorScheme` de Material 3.
4. Menciona que los botones de adulto miden al menos 52 dp y los del estudiante 72 dp.

`rememberTamanoVentana` en `TamanoVentana.kt` usa el ancho: menos de 600 dp es teléfono, menos de 840 es tablet vertical, el resto es tablet horizontal.

## Minuto 6 a 8 — Navegación, formulario y validación

1. Desde el catálogo abre Nueva actividad.
2. Pulsa Guardar vacío. Muestra el error en el campo, no solo un botón apagado.
3. Abre `ActividadValidator.validarNombre` y lee las tres condiciones.
4. Si te piden modificar: agrega una condición en esa función, por ejemplo largo máximo distinto, y vuelve a guardar para ver el mensaje nuevo.
5. Muestra `NavGraph.kt`: bienvenida, login, catálogo, editor, asistente, código y sesión.

## Minuto 8 a 10 — StateFlow y Room

1. En `CatalogoViewModel`, muestra `_estado` y `estado`.
2. Di que la pantalla usa `collectAsStateWithLifecycle()`.
3. Filtra por Matemática. Explica que `visibles` recorre la lista que ya llegó de Room.
4. Abre `Semilla.kt` y muestra una actividad. Di que se inserta en `onCreate` de la base, una vez.
5. Si te piden tocar Room: en `ActividadDao` la función `cambiarEstado` es una sola línea. Puedes explicar que activar también exige preguntas en el repositorio.

## Minuto 10 a 12 — Recursos nativos y animación

1. Como estudiante, con `482196`, pulsa **Escuchar**. Abre `Voz.kt`: `hablar`, `detener` y `liberar`. No usa micrófono.
2. Como asistente, abre un resultado y pulsa **Exportar CSV**. Elige una carpeta y muestra el archivo. Si cancelas, la app avisa.
3. Abre `ExportadorCsv.escribir`. Di que usa el selector del sistema y no pide permiso de almacenamiento.
4. Menciona la vibración al acertar, en `Vibrador.kt`, y la barra de progreso que se anima en `BarraAvance`.
5. Di con claridad: “Falta confirmar con el docente que voz y archivo son los dos recursos que pide la rúbrica”.

## Minuto 12 a 15 — Modificar en vivo

Elige una sola de estas, la que pidan:

| Pedido | Dónde | Cambio corto |
| --- | --- | --- |
| Nueva validación | `ActividadValidator.validarNombre` o `ObservacionValidator.validarTexto` | Una condición nueva en el `when` |
| Nuevo Composable | `ui/components/Componentes.kt` | Copia `ChipEstado` y cambia el texto |
| Cambiar una condición | `clasificarCodigo` o `ResultadoDescriptivo.frase` | Cambia un límite o una frase |
| Operación Room | `AsignacionDao` o `ActividadDao` | Muestra `insertar` o `cambiarEstado` y explica quién la llama |
| Recurso nativo | `Voz.kt` o `ExportadorCsv.kt` | Explica inicio, uso, error y cierre |

Cierra diciendo qué no hace la app: no diagnostica, no pone notas y no guarda datos reales.

## Cuentas para no trabarte

- UTP: `utp.demo` / `Utp2026`
- Asistente: `asistente.aula2` / `Aula2026`
- Estudiante: `482196`
- Vencido: `111111`
- Ya usado: `222222`
