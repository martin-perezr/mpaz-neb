# Casos de prueba — MPAZ NEB (rama `feature/hana-codigos-api`)

**Fecha de este documento:** 2026-11-22 (actualizado cierre EP4)  
**Convención:** *Ejecutado* = corrido en este entorno con salida verificable. *Reportado previamente* = descrito en conversación de desarrollo, sin captura en el repositorio. *Pendiente* = falta evidencia en repo o ejecución en este turno.

---

## 0. Matriz indicadores IE 3.1.1 – IE 3.3.2 (pauta EP4)

| Indicador | Requisito | Implementación | Componente / archivo | Prueba | Resultado | Evidencia | Estado |
|-----------|-----------|----------------|----------------------|--------|-----------|-----------|--------|
| **IE 3.1.1** | App completa, pantallas y navegación sin errores | Login por rol, catálogo, asignar, código, sesión estudiante, editor | `ui/screens/*`, `AppNav` | `assembleDebug` ejecutado; 65 JVM + 7 instrumentadas | Compilación y suites **OK** en este PC | Informes `app/build/reports/tests/` | **Parcial** — falta checklist manual formal con capturas |
| **IE 3.1.2** | Microservicio Spring Boot + BD + endpoints | Actividades (Matías), asignaciones/códigos (Hanael), salud (Martín) | `backend-moviles` controllers | `.\mvnw.cmd test` (9 tests) | **0 fallos** | `target/site/jacoco/index.html` | **Cumplido** (servidor; no incluye captura Postman en repo) |
| **IE 3.1.3** | Integración móvil CRUD con microservicio | `ActividadRepository`: GET sync, POST/PUT guardar, DELETE borrador, asignaciones/códigos | `ActividadRepositoryCrudRemotoTest`, `ActividadControllerRestTest` | JVM + manual | **CRUD actividad automatizado** (sin servidor real en JVM) | Postman / 2 emuladores **pendientes** captura | **Cumplido** técnico / evidencia manual pendiente |
| **IE 3.1.4** | API externa Retrofit integrada en UI | Open Library en editor Lectura | `OpenLibraryClient`, `BuscarLibrosSection` | `OpenLibraryRepositoryTest`, `BuscarLibrosComposeInstrumentedTest` | **OK** | Tests §2 | **Cumplido** (sin captura pantalla en repo) |
| **IE 3.2.1** | ≥ 80 % **código lógico** | ViewModels, Repositories, `domain`, Retrofit clients | `jacocoDebugLogicalUnitTestReport` | 65 tests JVM | **44,00 %** (653/1484 líneas) | `app/build/reports/jacoco/jacocoDebugLogicalUnitTestReport/` | **No cumplido** |
| **IE 3.3.1** | APK release firmado + `.jks` | `signingConfigs` + `keystore.properties.example` | `app/build.gradle.kts`, `keystore.properties.example` | `assembleRelease` | APK **unsigned** generado sin keystore | `app/build/outputs/apk/release/app-release-unsigned.apk` | **Pendiente** — falta `.jks` y `keystore.properties` local |
| **IE 3.3.2** | GitHub + Trello + commits por integrante | Ramas, Trello en README | Git history / Trello URL | Revisión `git shortlog` | Commits Matías/Martín en historial; trabajo Hana **sin commitear** aún | Capturas Trello **pendientes** | **Parcial** — push y evidencias AVA |

### Reglas del informe «código lógico» (IE 3.2.1)

Tarea Gradle: `:app:jacocoDebugLogicalUnitTestReport` (`app/build.gradle.kts`, lista `jacocoLogicalIncludes`).

**Incluye:** `data/repository/**`, `viewmodel/**`, `domain/**`, `RetrofitClient`, `OpenLibraryClient`.

**Excluye (informe global, no lógico):** UI Compose, theme, `data/local` Room DAO, `ui/**`, generados.

**No excluye** ViewModels/Repositories para inflar el porcentaje.

**Medición anterior:** **44,00 %** (653/1484). **Actual (2026-11-22, post-CRUD):** **47,51 %** (762/1604 líneas).

Clases con mayor hueco (revisar HTML del informe): `SesionEstudianteViewModel`, `ResultadosViewModel`, `EditorPreguntaViewModel`, `AsistenteViewModel`, `CatalogoViewModel` (flujos `init`), factories de ViewModels.

---

## 1. Matriz H1–H6 (requisitos académicos)

| Hito | Alcance | Estado | Evidencia |
|------|---------|--------|-----------|
| **H1** | Persistencia asignaciones y códigos 6 dígitos / 30 min (backend) | **Completo** | `AsignacionRepositorio`, `GeneradorCodigoTemporal`, SQLite |
| **H2** | REST `POST /api/asignaciones`, `POST /api/codigos/validar` | **Completo** | Controladores + `AsignacionServicio` |
| **H3** | Cliente Retrofit, URL configurable, asignar/validar desde Android | **Completo** | `RetrofitClient`, `AsignacionRepository`, `BuildConfig` |
| **H4** | `idRemoto` en Room, enlace tablet nueva ↔ actividad servidor | **Completo** | Migración v4, `actividadLocalParaIdRemoto` |
| **H5** | Open Library en editor Lectura (`BuscarLibrosSection`) | **Completo** | UI + `OpenLibraryRepository` |
| **H6** | Pruebas automatizadas + JaCoCo | **Parcial** | 65 JVM + 7 instrumentadas aisladas + 5 backend; **80 % lógico no alcanzado** (ver §0 y §4) |

---

## 2. Pruebas automatizadas — resultados reales (2026-11-22)

### Backend (`backend-moviles`)

| Comando | Resultado |
|---------|-----------|
| `.\mvnw.cmd test` | **9 tests, 0 fallos** (asignaciones ×4, contexto ×1, REST actividades ×4) |
| Perfil activo en tests | `test` → `jdbc:sqlite:mpazneb-test.db` |
| `mpazneb-servidor.db` tras `mvn test` | **Sin modificar** (comprobado) |

JaCoCo: `.\mvnw.cmd test` genera `target/site/jacoco/index.html`.

| Medición | Líneas |
|----------|--------|
| Global backend | **58,88 %** |
| Código H1–H2 (asignaciones/códigos) | **~81,82 %** |

### Android (`mpaz-neb`)

| Comando | Resultado |
|---------|-----------|
| `.\gradlew.bat :app:testDebugUnitTest` | **80 tests, 0 fallos** |
| `.\gradlew.bat :app:jacocoDebugUnitTestReport` | Informe JVM global |
| `.\gradlew.bat :app:jacocoDebugLogicalUnitTestReport` | Informe **código lógico** (IE 3.2.1) |
| `$env:ANDROID_SERIAL="emulator-5554"; .\gradlew.bat :app:connectedDebugAndroidTest` | **7 tests, 0 fallos** (suite aislada) |
| `.\gradlew.bat :app:jacocoDebugAndroidTestReport` | Informe instrumentado |

| Medición | Líneas | Informe |
|----------|--------|---------|
| JVM global | **10,99 %** | `app/build/reports/jacoco/jacocoDebugUnitTestReport/html/index.html` |
| Instrumentada global | **15,43 %** | `app/build/reports/jacoco/jacocoDebugAndroidTestReport/html/index.html` |

**No sumar** porcentajes JVM + instrumentada.

### Prueba de Martín (integración backend)

`SaludApiInstrumentedTest` (Martin Pérez, Retrofit `/api/salud`) se **conserva** con `@IntegracionBackendReal` y queda **excluida** del `connectedDebugAndroidTest` habitual (`notAnnotation` en `app/build.gradle.kts`).

Ejecución manual (con Spring Boot y `adb reverse tcp:8080 tcp:8080`):

```powershell
$env:ANDROID_SERIAL="emulator-5554"
.\gradlew.bat :app:connectedDebugAndroidTest `
  -Pandroid.testInstrumentationRunnerArguments.annotation=cl.duoc.mpazneb.IntegracionBackendReal `
  -Pandroid.testInstrumentationRunnerArguments.notAnnotation=
```

### Inventario suites Android (unitarias, 65)

| Clase | Tests aprox. |
|-------|----------------|
| `ValidadoresTest` | 8 |
| `AsignacionRepositoryTest` | 12 |
| `EditorActividadViewModelBuscarLibrosTest` | 9 |
| `AsignacionesYCodigosTest` | 8 |
| `AsignarViewModelTest` | 3 |
| `OpenLibraryRepositoryTest` | 3 |
| `ActividadRepositoryEnlaceTest` | 3 |
| `ActividadRepositoryAdminTest` | 3 |
| `AuthRepositoryTest` | 3 |
| `LoginViewModelTest` | 2 |
| `CatalogoViewModelTest` | 2 |
| `CodigoViewModelTest` | 2 |
| `ImagenValidatorTest` | 5 |
| `ActividadDtoGsonTest` | 1 |
| `ExampleUnitTest` | 1 |

### Instrumentadas aisladas (7)

| Clase | Tests |
|-------|-------|
| `RoomAsignacionActividadInstrumentedTest` | 2 |
| `BuscarLibrosComposeInstrumentedTest` | 4 |
| `ExampleInstrumentedTest` | 1 |

Room usa `Room.inMemoryDatabaseBuilder` — **no** escribe `mpazneb.db` del emulador.

---

## 3. Códigos HTTP (backend)

Implementación en `AsignacionServicio` / controladores (Matías conserva CRUD actividades):

| Código | Uso en asignaciones/códigos |
|--------|----------------------------|
| **200** | Validación de código exitosa |
| **201** | Asignación creada |
| **400** | Datos inválidos, actividad no disponible, código mal formado |
| **404** | Actividad o código inexistente |
| **409** | Sesión duplicada / código ya usado |
| **410** | Código vencido |

**Automatizado hoy:** reglas de negocio vía `AsignacionServicioTest` (perfil `test`).  
**No automatizado en CI:** `@WebMvcTest` / Postman que afirmen el código HTTP en la capa REST (recomendado como evidencia manual o prueba futura).

---

## 4. Cobertura — límites y objetivo 80 %

| Ámbito | Medición (líneas) | ¿≥ 80 %? | Comentario |
|--------|-------------------|----------|------------|
| Backend global | 58,88 % | No | Incluye CRUD actividades de Matías sin tests propios |
| Backend H1–H2 | ~81,82 % | **Sí** (alcance asignaciones) | `AsignacionServicioTest` |
| Android JVM global | 10,99 % (642/5844) | No | UI, Room, Compose |
| Android **código lógico** (IE 3.2.1) | **44,00 % (653/1484)** | **No** | Tarea `jacocoDebugLogicalUnitTestReport` |
| Android instrumentada global | 15,43 % (última corrida documentada) | No | Re-ejecutar tras cambios si se exige HTML actualizado |

Para el docente: el indicador **IE 3.2.1** debe leerse sobre el informe **lógico**, no sobre el global JVM.

---

## 5. Open Library (H5)

| Verificación | Estado |
|--------------|--------|
| `OpenLibraryRepositoryTest` (JVM, mock) | Ejecutado, aprobado |
| `EditorActividadViewModelBuscarLibrosTest` | Ejecutado, aprobado (incl. regresión error tras búsqueda exitosa) |
| `BuscarLibrosComposeInstrumentedTest` | Ejecutado en emulador, aprobado |
| Llamada HTTP real a openlibrary.org en CI | **No** (por diseño; mocks/fakes) |
| Tocar libro → rellenar texto lectura | **No implementado** en ViewModel (solo lista resultados) |

---

## 6. Pruebas manuales (Postman, dos emuladores)

| Evidencia | Estado en repositorio |
|-----------|------------------------|
| Postman: 200/201/400/404/409/410 en API asignaciones/códigos | **Pendiente de captura** (endpoints implementados; sin colección exportada en repo) |
| Dos emuladores: asignar en uno, validar en otro (H3–H4) | **Reportado previamente** en desarrollo; **sin screenshots en repo** |
| Flujo completo UI (tabla §7 legacy) | Mayoría **pendiente** de marcar ejecutado con fecha y observación |

---

## 7. Cómo ejecutar (reproducible)

### Backend

```powershell
cd backend-moviles
# Confirmar: src/test/resources/application-test.properties → mpazneb-test.db
# NO borrar mpazneb-servidor.db
.\mvnw.cmd test
# Opcional: .\mvnw.cmd spring-boot:run   (usa mpazneb-servidor.db)
```

### Android

```powershell
cd mpaz-neb
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest :app:jacocoDebugUnitTestReport
# Emulador único recomendado:
$env:ANDROID_SERIAL="emulator-5554"
.\gradlew.bat :app:connectedDebugAndroidTest :app:jacocoDebugAndroidTestReport
```

Con backend local en dispositivo/emulador:

```powershell
adb reverse tcp:8080 tcp:8080
```

Debug usa `http://127.0.0.1:8080/` (`app/build.gradle.kts`).

---

## 8. Casos funcionales heredados (app local / UI)

La tabla siguiente conserva casos del MVP local. Actualizar la columna *Observado* al hacer cada prueba manual.

| Caso | Estado documental |
|------|-------------------|
| Compilar `assembleDebug` | Ejecutado en desarrollo |
| Validadores y lógica de códigos (JVM) | Ejecutado — ver §2 |
| Inicio, login, roles, asignar, código, persistencia | Pendiente evidencia formal |
| Exportar CSV, voz, layouts | Pendiente evidencia formal |

---

## 9. Evidencias que debes aportar tú (entrega académica)

1. Capturas Postman (o export JSON) con **200, 201, 400, 404, 409, 410** en rutas de asignación/código.  
2. Capturas **dos emuladores**: asignación + validación cruzada con `idRemoto`.  
3. Captura pantalla editor **Lectura** con búsqueda Open Library (éxito y sin resultados).  
4. Captura del informe **código lógico** (`jacocoDebugLogicalUnitTestReport/html/index.html`) con **44,00 %** o el valor vigente tras más tests.  
5. **APK firmado** + captura de `apksigner verify` + archivo `.jks` para AVA (no contraseña en GitHub).  
6. Capturas **Trello** (tablero, tarjetas por integrante).  
7. Opcional: ejecutar `SaludApiInstrumentedTest` de Martín (comando §2).

---

## 10. Git y secretos

- `local.properties` y `*.db` **no** deben commitearse (backend: `*.db` en `.gitignore`).  
- No incluir `mpazneb-servidor.db` ni `mpazneb-test.db` en commits.