# Casos de prueba

Convención: **ejecutado** significa que se corrió en este computador. **pendiente** significa que hace falta un emulador o un teléfono. No se marca como aprobado un caso que no se ejecutó.

## Ejecutados

| Caso | Pasos | Esperado | Observado | Estado |
| --- | --- | --- | --- | --- |
| Compilar la app | `.\gradlew.bat :app:assembleDebug` | APK debug | `BUILD SUCCESSFUL`. La primera vez Gradle instaló la plataforma 37 | Ejecutado, aprobado |
| Pruebas unitarias | `.\gradlew.bat :app:testDebugUnitTest` | Sin fallos | 7 pruebas en `ValidadoresTest` y 1 en `ExampleUnitTest`, 0 fallos | Ejecutado, aprobado |
| Código de 6 números | `CodigoValidator.validar` | `482196` válido; vacío, `4821`, `4821967` y `48219A` inválidos | Así lo afirmó el test `codigoDeSeisNumeros` | Ejecutado, aprobado |
| Formato del código | `formatear(7)` | `000007` | Aprobado por el mismo test | Ejecutado, aprobado |
| Login | `LoginValidator` | Rechaza vacío, alias corto y clave sin número; acepta `utp.demo` y `Utp2026` | Aprobado por `loginRechazaCamposVaciosYClaveCorta` | Ejecutado, aprobado |
| Actividad | `ActividadValidator` | Lectura exige texto; Matemática no; intentos entre 1 y 5; curso 5 es inválido | Aprobado por `actividadExigeTextoSoloEnLectura` | Ejecutado, aprobado |
| Pregunta | `PreguntaValidator` | Dos alternativas válidas; un hueco no; Matemática pide procedimiento | Aprobado por `preguntaPideDosAlternativasYUnaCorrecta` | Ejecutado, aprobado |
| Asignación y observación | validadores | Filtro incompleto falla; observación corta falla; categoría “Diagnóstico” falla | Aprobado por `asignacionYObservacion` | Ejecutado, aprobado |
| Estado del código | `clasificarCodigo` | Null no existe; vigente es válido; en desarrollo y no vencido es válido; vencido; finalizado es ya usado | Aprobado por `codigoVigenteVencidoYUsado` | Ejecutado, aprobado |
| Resultado y CSV | `ResultadoDescriptivo` y `TextoCsv` | La frase dice que no es una nota; las comillas del texto se duplican | Aprobado por `resultadoNoEsNotaYElCsvEscapaComillas` | Ejecutado, aprobado |

## Pendientes en emulador o dispositivo

Estos casos quedaron escritos para hacerlos en Android Studio. No se observó su resultado visual.

| Caso | Pasos | Resultado esperado |
| --- | --- | --- |
| Inicio | Abrir la app | Se ve la bienvenida con equipo y estudiante |
| Administración | `utp.demo` / `Utp2026` | Entra al catálogo. Atrás no muestra el panel de la asistente |
| Asistente | `asistente.aula2` / `Aula2026` | Entra a actividades y asignaciones. No puede editar la respuesta correcta |
| Rol | Con la asistente abierta, intentar la ruta del catálogo | La lógica devuelve al inicio |
| Código válido | Estudiante, `482196` | Abre “El volantín de Tomás” |
| Código inválido | `123456` | Mensaje de que no está |
| Código vencido | `111111` | Mensaje de vencido |
| Código usado | `222222` | Mensaje de ya usado |
| Crear actividad | Administración, Nueva, completar campos, Guardar | Queda guardada. Sin preguntas no se puede activar |
| Editar | Cambiar el objetivo y guardar | Al volver, el objetivo nuevo sigue |
| Validación visual | Dejar el nombre vacío y guardar | Borde e ícono de error y un mensaje concreto |
| Asignar | Asistente, filtro completo, estudiante sin actividad abierta | Aparece un código de 6 números |
| Actividad desactivada | Intentar asignar “Sumas en el recreo” | No aparece entre las disponibles |
| Lectura | Resolver el volantín | Texto a la izquierda en tablet horizontal; pregunta a la derecha |
| Matemática | Resolver “Lápices de la sala” o “Manzanas en la mesa” | Se registra el intento |
| Correcta e incorrecta | Acertar y fallar | Mensaje verde o pista. Al agotar intentos se muestra la respuesta |
| Persistencia | Responder, cerrar la app, abrir Resultados | Las respuestas siguen |
| Exportar | Resultados, Exportar CSV, elegir Descargas, y también Cancelar | Guardado confirma. Cancelar avisa y no rompe |
| Voz | Escuchar en el código y en una pregunta | Habla o, si no hay motor, no se cae |
| Tablet horizontal, tablet vertical y teléfono | Girar o cambiar el dispositivo | Los botones se alcanzan y el texto no queda cortado |
| Salir | Pulsar Salir | Vuelve a la bienvenida y vuelve a pedir la clave |

## Cómo ejecutar lo pendiente

1. Abre el proyecto en Android Studio y sincroniza.
2. Device Manager → Create Device → elige una tablet. Repite con un teléfono.
3. Run.
4. Recorre la tabla de arriba y anota aquí el resultado observado.
5. Para borrar datos de una prueba anterior: desinstala la app o usa **Clear storage**. La semilla se crea de nuevo.
