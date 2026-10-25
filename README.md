# MPAZ NEB

Aplicación Android para practicar Lectura y Matemática de 1.º a 4.º básico en la Escuela Escritora Marcela Paz. Es un MVP académico: usa personas y observaciones ficticias, no pone notas y no reemplaza al profesorado.

## Integrantes 

- Hanael Echeverría
- Matías Soto
- Martín Pérez

## Tecnologías

- Kotlin y Jetpack Compose
- Material Design 3
- MVVM, Repository y StateFlow
- Room
- Navegación de Compose
- DataStore para recordar la sesión del adulto
- Recursos nativos: galería con permiso de lectura de imágenes, síntesis de voz, vibración y exportación CSV con el selector de archivos.

## Qué puede hacer

- Administración (`utp.demo`): ver, filtrar, crear, editar, activar y desactivar actividades y sus preguntas, y agregarles una ilustración desde la galería..
- Asistente (`asistente.aula2`): elegir módulo, curso y dificultad, asignar una actividad disponible, generar un código de 6 números, revisar resultados y anotar una observación ficticia.
- Estudiante: entrar con el código, leer o escuchar, responder, confirmar, reintentar y ver un cierre sin nota.

## Cuentas y códigos ficticios

| Uso | Dato |
| --- | --- |
| Docente / UTP | alias `utp.demo`, clave `Utp2026` |
| Asistente | alias `asistente.aula2`, clave `Aula2026` |
| Código vigente | `482196`, estudiante Pudú-12, lectura “El volantín de Tomás” |
| Código vencido | `111111` |
| Código ya usado | `222222` |

Los códigos que crea la asistente vencen a los 30 minutos. `482196` dura más solo para poder demostrarlo. No es una clave institucional.

## Requisitos

- Android Studio actual
- Android SDK 37
- Un emulador o una tablet/teléfono con Android 9 o superior (`minSdk` 28)

## Abrir el proyecto

1. Android Studio → **File → Open**.
2. Elige la carpeta del proyecto, la que contiene `settings.gradle.kts`.
3. Espera a que termine **Sync**.
4. Si pide la plataforma 37, acepta la instalación.
5. Crea un dispositivo tablet, por ejemplo Pixel Tablet, en horizontal.
6. Pulsa **Run**.

También se puede compilar en la terminal, dentro de la carpeta del proyecto:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

## Cómo probar el recorrido

1. Entra como `utp.demo` y revisa el catálogo. Crea una actividad, guarda, agrega una pregunta y actívala.
2. Sal y entra como `asistente.aula2`. Elige módulo, curso y dificultad. Asigna la actividad a un estudiante que no tenga otra abierta.
3. Anota el código de 6 números. Sal de la sesión.
4. Entra como estudiante, escribe el código y responde.
5. Vuelve como asistente, abre Resultados y exporta el CSV.
6. Cierra la app y ábrela de nuevo. Las asignaciones siguen en la base.

Pudú-12 ya tiene el código `482196` abierto. Para asignarle otra actividad hay que terminar esa o elegir otro estudiante, por ejemplo Chinchilla-05 en 1.º básico.

## Galería y permisos

Entra como utp.demo, abre una actividad y pulsa Elegir de la galería.
La primera vez la app pide permiso para ver tus fotos. Hasta Android 12 es READ_EXTERNAL_STORAGE; desde Android 13, READ_MEDIA_IMAGES, y desde Android 14 se puede compartir solo algunas fotos.
Elige una foto JPG, PNG o WEBP de hasta 5 MB. La app la copia a su carpeta privada y la muestra con borde redondeado.
Guarda la actividad. La ilustración aparece en el catálogo y en la lectura del estudiante.
Si rechazas el permiso, la app explica cómo activarlo en Ajustes y no abre la galería.

## Limitaciones

- No hay servidor. Lo que se guarda en una tablet no aparece en otra.
- No hay RUT, diagnósticos, datos PIE ni calificaciones oficiales.
- No usa cámara, micrófono, GPS, biometría ni contactos.
- La semilla cubre una secuencia corta de Lectura y otra de Matemática, no todas las unidades del año.
- La aceptación de como los dos recursos nativos de la rúbrica debe confirmarla el docente. La vibración también está integrada.

## Rama Git

El trabajo nuevo está en `hana`. `develop` y `main` no se modificaron. La rama no se publicó: en la semana 11 del segundo semestre 2026 la pauta sanciona con nota 1.0 los cambios remotos hechos después de la entrega y antes de la defensa. Si el docente autoriza la publicación:

```powershell
git checkout hana
git push -u origin hana
```

No hagas push a `develop` ni a `main`.
