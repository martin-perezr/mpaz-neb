# Auditoría del repositorio MPAZ NEB

Fecha de la revisión: 22 de octubre de 2026.  
Repositorio: https://github.com/martin-perezr/mpaz-neb  
Base revisada: `origin/develop` en el commit `a4b499e`.  
Rama de trabajo creada en este equipo: `hana` (solo local).

## 1. Estado inicial

El repositorio ya tenía una aplicación Android nativa, no un proyecto vacío. Al clonarlo, la rama activa era `develop`, el árbol estaba limpio y no existía la rama `hana` ni en local ni en GitHub.

Lo que ya funcionaba:

- Inicio con dos caminos: equipo de la escuela y estudiante.
- Login de adultos con validación por campo, bloqueo tras 3 intentos y sesión en DataStore.
- Ingreso del estudiante con teclado numérico.
- Sesión de práctica con lectura o matemática, confirmación, retroalimentación, reintento y cierre.
- Room con usuarios, estudiantes, actividades, preguntas, alternativas, asignaciones, respuestas y observaciones.
- Semilla ficticia que se inserta una sola vez, al crear la base.
- Tema Material 3, tipografías Nunito y Andika, y tamaños de ventana para teléfono, tablet vertical y tablet horizontal.
- Síntesis de voz y vibración.

Lo que estaba pendiente:

- Catálogo, editor de actividad y editor de pregunta mostraban el texto “En construcción”.
- Inicio de la asistente, asignar y resultados también estaban en construcción.
- El código temporal tenía 4 dígitos. La pauta del caso y este encargo piden 6.
- No había exportación de resultados.
- No había pruebas de las validaciones reales. Solo existía el ejemplo `2 + 2`.

## 2. Tecnologías existentes

- Kotlin, Jetpack Compose y Material 3.
- Un solo módulo Gradle: `app`.
- Room, DataStore, Navigation Compose y Coil.
- Inyección manual en `AppContainer`. No se agregó Hilt.
- `minSdk` 28, `compileSdk` y `targetSdk` 37.
- Android Gradle Plugin 9.4.1 y Kotlin 2.2.10.

## 3. Funcionalidades que ya estaban implementadas

| Pieza | Estado al empezar | Decisión |
| --- | --- | --- |
| Bienvenida, login y sesión del estudiante | Funcionaban | Conservar |
| Entidades, DAO y semilla | Funcionaban | Conservar y ampliar la semilla |
| Repositorios | Consultaban Room, pero no frenaban un rol incorrecto | Mejorar |
| Validadores de login y código | Funcionaban, el código era de 4 dígitos | Corregir el largo |
| Voz y vibración | Funcionaban | Conservar |
| Copia de imágenes de galería | Existía la clase, sin pantalla | Conservar sin usarla todavía |
| Pantallas de administración y asistente | Solo el nombre de la ruta | Crear |

## 4. Funcionalidades que faltaban

- Consultar, filtrar, crear, editar, activar y desactivar actividades.
- Crear y editar preguntas con alternativas y respuesta correcta.
- Que la asistente filtre por módulo, curso y dificultad.
- Asignar una actividad disponible a un estudiante ficticio.
- Generar y validar un código de seis dígitos con vencimiento.
- Consultar respuestas, intentos y un resultado descriptivo.
- Registrar observaciones ficticias.
- Exportar ese resultado a CSV con el selector de archivos de Android.

## 5. Errores encontrados

- Las rutas de administración y asistente no tenían pantallas.
- `CodigoValidator.LARGO` valía 4 y la semilla usaba `4821`.
- Un código de una actividad ya comenzada se trataba como usado, así que no se podía retomar.
- Las reglas “solo administración modifica” y “solo la asistente asigna” no estaban en el repositorio: dependían de que el botón existiera.
- En este computador no estaba instalada la plataforma Android 37. Gradle la instaló durante la compilación.

## 6. Decisiones técnicas

- Se terminó el proyecto existente. No se creó otra aplicación.
- Se mantuvo MVVM con Repository, Room y StateFlow.
- Los formularios nuevos llaman a objetos `Validator`. La pantalla solo muestra el mensaje.
- Los cambios de datos pasan por el repositorio y ese repositorio revisa el rol.
- La aplicación sigue siendo local. No hay servidor institucional. Dos tablets no se sincronizan solas.
- Los códigos nuevos vencen a los 30 minutos. El código de demostración `482196` dura un año para poder probarlo en la defensa.
- Los dos recursos nativos que se muestran en la defensa son la síntesis de voz y la exportación con el selector de archivos. La vibración se conserva como refuerzo. Falta que el docente confirme si acepta esa pareja de recursos.
- La versión de la base pasó de 1 a 2 para que una instalación anterior cargue la semilla nueva. Esa recreación borra los datos de prueba anteriores.

## 7. Archivos que se modificaron o crearon

Se modificaron, entre otros, `Semilla.kt`, `AppDatabase.kt`, `ActividadRepository.kt`, `AsignacionRepository.kt`, `CodigoValidator.kt`, `NavGraph.kt`, `CodigoScreen.kt` y `AppContainer.kt`.

Se crearon las pantallas de `ui/screens/admin` y `ui/screens/asistente`, sus ViewModel, los validadores de actividad, pregunta, asignación y observación, `ExportadorCsv.kt` y esta documentación.

## 8. Plan que se ejecutó

1. Crear `hana` desde `origin/develop` sin borrar trabajo.
2. Completar reglas, códigos de seis dígitos y semilla.
3. Construir administración, asistente, asignación, resultados y exportación.
4. Compilar y ejecutar pruebas unitarias.
5. Documentar cómo estudiarlo y cómo defenderlo.

## 9. Riesgos

- No se pudo abrir el emulador desde aquí. La interfaz se compiló, pero no se recorrió con el dedo.
- Publicar en GitHub durante la semana 11 puede caer en la regla de nota 1.0 si la entrega ya se hizo. Por eso `hana` no se subió.
- Si el docente no acepta voz y selector de archivos como los dos recursos nativos, hay que confirmarlo antes de la defensa. La vibración es un tercer recurso ya existente.
- La semilla no es un currículo completo. Alcanza para demostrar cursos y dificultades, no para reemplazar las clases.

## 10. Compilación verificada

En Windows, con el SDK local:

```text
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
BUILD SUCCESSFUL
```

La primera corrida instaló Android SDK Platform 37 y compiló. Después de los ajustes finales se volvió a ejecutar:

```text
.\gradlew.bat :app:compileDebugKotlin :app:testDebugUnitTest
```

Terminó con código 0. `ValidadoresTest` quedó con 7 pruebas, 0 fallos. `ExampleUnitTest` también pasó. No se ejecutaron pruebas en emulador.
