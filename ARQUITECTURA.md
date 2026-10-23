# Arquitectura

MPAZ NEB usa MVVM sencillo. Cada capa tiene un trabajo y la pantalla no habla con Room.

```text
Pantalla (Composable)
        |
        v
ViewModel  ---- StateFlow ---->  la pantalla se redibuja
        |
        v
Repository  (también revisa el rol)
        |
        v
DAO
        |
        v
Room (mpazneb.db)
```

## Dónde está cada cosa

| Capa | Carpeta | Qué hace |
| --- | --- | --- |
| Pantallas | `ui/screens` | Muestran datos y avisan toques |
| ViewModel | `viewmodel` | Guardan el estado y llaman al repositorio |
| Repositorio | `data/repository` | Deciden si la operación está permitida y usan el DAO |
| DAO y base | `data/local` | Consultas Room y semilla |
| Tablas | `model/Entidades.kt` | Usuarios, actividades, preguntas, asignaciones, respuestas y observaciones |
| Validación | `domain/validation` | Funciones puras, sin Compose |
| Rutas | `ui/navigation` | Qué pantalla sigue a cuál |
| Recursos del teléfono | `device` | Voz, vibración y archivo CSV |
| Armado de objetos | `AppContainer.kt` | Crea la base y los repositorios una sola vez |

No hay módulos Gradle extra ni Hilt. `MainActivity` solo abre el tema y `MpazNavHost`.

## Estado

Cada ViewModel tiene un `MutableStateFlow` privado y expone un `StateFlow`. La pantalla lo lee con `collectAsStateWithLifecycle()`.

Los estados importantes son: cargando, datos listos, mensaje de error, mensaje de éxito y “sin permiso”. Si el rol no corresponde, la pantalla vuelve al inicio.

## Navegación

```text
Bienvenida
  ├── Login
  │     ├── Catálogo (administración)
  │     │     └── Editor de actividad
  │     │           └── Editor de pregunta
  │     └── Inicio de la asistente
  │           ├── Asignar
  │           └── Resultados
  └── Código
        └── Sesión del estudiante
```

`Rutas.kt` tiene el texto de cada ruta. `NavGraph.kt` conecta la ruta con la pantalla.

## Reglas que no dependen del botón

- `ActividadRepository` solo deja crear, editar, borrar preguntas o cambiar el estado si la sesión es `Rol.ADMIN`.
- No se puede dejar una actividad disponible si no tiene preguntas.
- `AsignacionRepository.crear` solo funciona con sesión `Rol.ASISTENTE`, actividad `DISPONIBLE` y estudiante sin otra actividad abierta.
- Una actividad `FINALIZADA` no cambia de código: se conserva el historial.
- `clasificarCodigo` distingue código inexistente, ya usado, vencido y vigente. Una sesión `EN_DESARROLLO` y no vencida se puede retomar.

## Persistencia

`AppDatabase` usa el archivo `mpazneb.db`. `Semilla.poblar` corre dentro de `onCreate`, una vez. La versión de la base es 2. Si el esquema cambia, `fallbackToDestructiveMigration` la vuelve a crear con la semilla. Eso es aceptable en este MVP porque no hay datos reales.

La sesión del adulto (id, alias y rol) vive en DataStore, aparte de Room, para no pedir la clave cada vez que se abre la app. El botón **Salir** la borra.

## Resultados

`ResultadoDescriptivo` arma una frase de logro alto, parcial o en inicio y siempre dice que no es una nota. `TextoCsv` convierte esa información en texto. `ExportadorCsv` la escribe en el archivo que la persona elige. La app no pide permiso de almacenamiento.

## Conectividad

El caso menciona internet, pero no hay un backend autorizado. El MVP guarda todo en el dispositivo. Dos equipos no comparten datos hasta que exista un servidor definido por la institución.
