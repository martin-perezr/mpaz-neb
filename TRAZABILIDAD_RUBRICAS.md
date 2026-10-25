# Trazabilidad de rúbricas

Las ponderaciones salen del encargo de este proyecto y de las pautas de la Evaluación Parcial 2 (`370263101.pdf`) y la Evaluación Parcial 3 (`370292645.pdf`). La evidencia de emulador está pendiente: aquí solo se afirma lo compilado y lo cubierto por pruebas unitarias.

## Evaluación parcial 2

| Indicador | Peso | Dónde está | Evidencia | Hueco |
| --- | --- | --- | --- | --- |
| Diseño visual y navegación | 15% | `Theme.kt`, `NavGraph.kt`, pantallas de admin, asistente y estudiante, `TamanoVentana.kt` | Compila. El layout de lectura en dos paneles ya existía y se conservó | Falta recorrerlo en tablet horizontal, tablet vertical y teléfono |
| Formularios validados | 15% | Login, código, actividad, pregunta, asignación y observación | Tests de validadores. La UI usa `CampoValidado` con `isError`, ícono y mensaje | Falta ver los formularios nuevos en el emulador |
| Validaciones desacopladas | 10% | `domain/validation` | Los tests llaman a los objetos sin Compose | Ninguno en la lógica. Falta mostrar el cambio en vivo |
| Animaciones | 10% | Transiciones del `NavHost`, sacudida del código y del login, `BarraAvance`, aparición del código y de los avisos | Están en el código. `duracionAnimacion` apaga las nuevas si el sistema tiene la escala en 0 | Falta verlas en ejecución |
| Modularidad y persistencia | 15% | MVVM, Room, DAO, repositorios y semilla | Compila. La semilla corre en `onCreate` | Falta cerrar y abrir la app para ver los datos |
| GitHub y planificación | 20% | Historial de `develop` ya publicado. Rama local `hana` | No se inventaron commits viejos. `hana` no se subió por la regla de la semana 11 | Publicar solo si el docente lo permite. Trello no está en este repositorio |
| Dos recursos nativos | 15% | `Voz.kt` y `ExportadorCsv.kt`. `Vibrador.kt` queda como apoyo | Compila. No piden micrófono ni almacenamiento amplio | Falta ejecutarlos y confirmar con el docente que esta pareja es la aceptada |

## Evaluación parcial 3

| Indicador | Peso | Cómo demostrarlo |
| --- | --- | --- |
| Explicar y modificar validaciones | 20% | Abrir `ActividadValidator` o `CodigoValidator` y agregar una condición. Guía en `GUIA_PARA_ESTUDIANTES.md` |
| Diseño y navegación | 7% | Recorrer bienvenida, catálogo, asistente y sesión. Mostrar `NavGraph.kt` |
| Incorporar un componente o clase | 22% | Crear un Composable pequeño al lado de `ChipEstado` o una función en un validador |
| Estado y estructuras de control | 7% | Mostrar el `when` de `clasificarCodigo` y el `StateFlow` del catálogo |
| Modificar persistencia | 18% | Mostrar `cambiarEstado` o `insertar` y explicar la semilla |
| Arquitectura y herramientas | 7% | Explicar pantalla → ViewModel → Repository → DAO |
| Integración del recurso nativo | 7% | Explicar `rememberVoz` y `CreateDocument` |
| Demostración del recurso | 12% | Pulsar Escuchar y exportar un CSV |

## Reglas del caso

| Regla | Dónde se cumple |
| --- | --- |
| Solo administración modifica actividades | `ActividadRepository.exigirAdmin` |
| Solo administración activa o desactiva | `cambiarEstado` |
| La asistente asigna actividades disponibles | `AsignacionRepository.crear` |
| El estudiante entra solo con su código | `clasificarCodigo` y la sesión usa el id de esa asignación |
| El curso y la dificultad los elige una persona | Filtros de la asistente. No hay un nivel automático |
| Los intentos respetan el máximo | `SesionEstudianteViewModel`, campo `intentosPermitidos` |
| Cada respuesta se guarda | `registrarRespuesta` inserta en Room |
| El resultado es descriptivo y no es nota | `ResultadoDescriptivo` |
| No hay datos personales reales | Alias de animales en `Semilla` |
| Una actividad desactivada no se asigna | Consulta `disponibles` y revisión en `crear` |
| Lo finalizado conserva historial | `regenerarCodigo` rechaza el estado `FINALIZADA` |

## Publicación

Clases del segundo semestre 2026: desde el 10 de agosto. El 22 de octubre corresponde a la semana 11. La pauta de la parcial 2 dice que un cambio en el repositorio después de la entrega y antes de la evaluación queda en 1.0, en la nota grupal y en la individual.

Por eso esta rama no se envió a GitHub. El trabajo está en la rama local `hana`.
