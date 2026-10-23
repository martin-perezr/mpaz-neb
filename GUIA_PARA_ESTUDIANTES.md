# Guía para estudiantes

Lee los archivos en este orden. En cada uno responde: qué es, para qué sirve, dónde está, cómo funciona, con qué se relaciona, qué puedes cambiar, cómo cambiarlo y cómo probarlo.

## 1. `MainActivity.kt`

1. Es la actividad única de Android.
2. Sirve para abrir la interfaz.
3. Está en `app/src/main/java/cl/duoc/mpazneb/MainActivity.kt`.
4. En `onCreate` aplica el tema y llama a `MpazNavHost`.
5. Se relaciona con `Theme.kt` y con la navegación.
6. Puedes cambiar el color de fondo de la superficie.
7. Cambia `MaterialTheme.colorScheme.background` solo si quieres ver el efecto; el color real del tema está en `Theme.kt`.
8. Ejecuta la app y comprueba que la bienvenida sigue en vertical y en horizontal.

## 2. `Rutas.kt` y `NavGraph.kt`

1. `Rutas` es la lista de direcciones. `NavGraph` decide qué pantalla se muestra.
2. Sirven para ir y volver sin perder el rol.
3. Están en `ui/navigation`.
4. Por ejemplo, `editorActividad(4)` produce el texto `admin/actividad/4`. Navigation entrega ese 4 como `Long`.
5. Se relacionan con todas las pantallas.
6. Puedes agregar una ruta nueva.
7. Crea la constante, la función que arma el texto y un `composable` en `NavGraph`.
8. Navega desde un botón y usa atrás del sistema para volver.

## 3. `LoginValidator.kt` y `LoginScreen.kt`

1. El validador es un objeto con funciones. La pantalla es la interfaz.
2. El validador dice si el alias y la clave tienen forma válida. No revisa si existen: eso lo hace `AuthRepository`.
3. Están en `domain/validation` y `ui/screens/auth`.
4. `validarAlias` revisa vacío, espacios y largo. La pantalla muestra `errorAlias` en rojo con un ícono.
5. `LoginViewModel` llama al validador y, si pasa, llama a `auth.ingresar`.
6. Durante la defensa puedes agregar una regla en `validarAlias` o `validarClave`.
7. Ejemplo: dentro de `validarAlias`, antes del `else`, agrega `alias.contains("@") -> "El alias no usa correo"`.
8. Escribe `utp@demo` en el login. Debe aparecer el mensaje y el botón no debe entrar. `utp.demo` con `Utp2026` sí entra.

## 4. `CodigoValidator.kt` y `CodigoScreen.kt`

1. El validador conoce el largo del código. La pantalla dibuja una casilla por número.
2. Evita entrar con un código vacío, corto o con letras.
3. `LARGO` está en `domain/validation/CodigoValidator.kt`. La pantalla usa ese mismo número en `repeat`.
4. `formatear(7)` devuelve `000007`. `validar` devuelve un mensaje o `null`.
5. `CodigoViewModel` primero valida y después llama a `asignaciones.validarCodigo`.
6. Puedes cambiar `LARGO`.
7. Si lo cambias, también debes ajustar la semilla y `formatear`, porque hoy rellena hasta 6.
8. Prueba `482196` (entra), `111111` (venció) y `222222` (ya se usó).

## 5. `ActividadValidator.kt`

1. Revisa el formulario de crear o editar una actividad.
2. Separa las reglas de la pantalla `EditorActividadScreen`.
3. Está en `domain/validation/ActividadValidator.kt`.
4. `validar` junta nombre, módulo, curso, dificultad, contenido, objetivo, instrucciones, intentos y, solo en Lectura, el texto.
5. `EditorActividadViewModel` llama a `validar` cuando se pulsa Guardar. Si falla, no llama a Room.
6. La función más fácil de modificar es `validarIntentos`.
7. Cambia `INTENTOS_MAX` de 5 a 3. El mensaje usa esa constante, así que se actualiza solo.
8. En el editor escribe `4` en intentos. Debe marcar error. Escribe `2` y guarda.

## 6. `PreguntaValidator.kt`

1. Revisa enunciado, tipo, alternativas, respuesta correcta, retroalimentación y pista.
2. Impide guardar una pregunta incompleta.
3. Está junto al validador de actividad.
4. `leerAlternativas` exige al menos dos textos, sin huecos y sin repetir.
5. El ViewModel convierte los textos válidos en filas `Alternativa`.
6. Puedes cambiar `MIN_ALTERNATIVAS`.
7. Súbelo a 3 y vuelve a correr `ValidadoresTest`: el caso de dos alternativas debe fallar y tendrás que actualizar el test.
8. En la app, deja una sola alternativa y pulsa Guardar. Debe explicar el problema.

## 7. `CatalogoViewModel.kt` y `CatalogoScreen.kt`

1. El ViewModel guarda la lista y los filtros. La pantalla dibuja tarjetas.
2. Permiten a administración encontrar y activar actividades.
3. Están en `viewmodel` y `ui/screens/admin`.
4. Room entrega todas las actividades. `visibles` las filtra en memoria por módulo, curso y dificultad.
5. Activar llama a `actividades.cambiarEstado`. El repositorio rechaza la acción si no hay sesión de administración o si no hay preguntas.
6. Puedes cambiar el texto de un filtro o agregar otro.
7. En `visibles`, agrega una condición, por ejemplo mostrar solo las que tienen preguntas.
8. Entra con `utp.demo`, filtra Matemática y 1.º, y comprueba que aparece “Manzanas en la mesa”.

## 8. `AsignacionRepository.kt`

1. Es la clase que crea asignaciones, códigos, respuestas y observaciones.
2. Concentra las reglas de la asistente y del estudiante.
3. Está en `data/repository/AsignacionRepository.kt`.
4. `crear` mira el rol, el estado de la actividad y si el estudiante ya tiene una sesión abierta. Luego genera un código libre de 6 dígitos que vence en `DURACION_CODIGO_MS`.
5. Lo usan `AsignarViewModel`, `CodigoViewModel` y `SesionEstudianteViewModel`.
6. Puedes cambiar la duración del código.
7. `DURACION_CODIGO_MS` está en el `companion object`. 30 minutos son `30 * 60 * 1000L`. Para una prueba puedes poner `2 * 60 * 1000L`.
8. Asigna una actividad, espera a que venza y comprueba que el estudiante ve “Ese código ya venció”.

## 9. `AppDatabase.kt`, `Semilla.kt` y los DAO

1. `AppDatabase` es la base. `Semilla` son los datos ficticios. Los DAO son las consultas.
2. Hacen que la información siga ahí después de cerrar la app.
3. Están en `data/local`.
4. Al crear el archivo `mpazneb.db`, Room llama a `Semilla.poblar`.
5. Los repositorios son los únicos que deberían usar los DAO.
6. Puedes agregar un estudiante ficticio en la lista `alumnos` de `Semilla`.
7. Agrega una línea `"Zorzal-02" to 3` dentro de la lista y sube `version` de la base en 1 para que se recree.
8. Entra como asistente, elige 3.º y comprueba que el alias nuevo aparece al asignar. Si no cambia nada, desinstala la app: la base vieja sigue en el teléfono.

## 10. `ResultadosViewModel.kt`, `TextoCsv.kt` y `ExportadorCsv.kt`

1. El ViewModel junta el detalle, las respuestas y las observaciones. `TextoCsv` arma el texto. `ExportadorCsv` lo escribe.
2. Muestran un resultado descriptivo y permiten guardarlo.
3. Están en `viewmodel`, `domain` y `device`.
4. La pantalla pide un archivo con `CreateDocument`. Si la persona cancela, se avisa. Si elige un lugar, `escribir` usa el `ContentResolver`.
5. Se relacionan con Room a través de `AsignacionRepository`.
6. Puedes cambiar la frase del resultado en `ResultadoDescriptivo.frase`.
7. Cambia la palabra “Logro parcial” por “Va avanzando”.
8. Termina una actividad, abre Resultados y lee la frase. Exporta y abre el CSV: debe usar punto y coma y mostrar el alias ficticio.

## 11. `Voz.kt`

1. Es una clase que envuelve `TextToSpeech`.
2. Lee instrucciones y preguntas sin usar el micrófono.
3. Está en `device/Voz.kt`.
4. `rememberVoz()` crea la voz al entrar y llama a `liberar()` al salir.
5. La usan el código y la sesión del estudiante mediante `BotonEscuchar`.
6. Puedes cambiar la velocidad.
7. En `onInit`, `setSpeechRate(0.9f)` se puede dejar en `0.8f` para leer más lento.
8. Pulsa Escuchar en el código o en una pregunta. Si el emulador no tiene voz en español, el botón no rompe la app: `hablar` no hace nada hasta que el motor esté listo.

## Qué no hay que hacer en la defensa

- No pidas a la pantalla que consulte un DAO.
- No guardes un RUT ni una nota.
- No borres una asignación finalizada para “limpiar”: el historial se conserva.
- No uses la IA durante los 15 minutos. Los cambios de arriba se pueden hacer a mano.
