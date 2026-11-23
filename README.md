# MPAZ NEB

Aplicación Android para practicar Lectura y Matemática de 1.º a 4.º básico en la Escuela Escritora Marcela Paz. Es un MVP académico: usa personas y observaciones ficticias, no pone notas y no reemplaza al profesorado.

**Repositorios del equipo**

| Componente | Carpeta / remoto |
|------------|------------------|
| Cliente Android (este repo) | `mpaz-neb` |
| Microservicio Spring Boot | `backend-moviles` (misma organización GitHub) |

Rama de integración de la evaluación parcial 4: `feature/hana-codigos-api`.

## Integrantes

- Hanael Echeverría
- Matías Soto
- Martín Pérez

## Funcionalidades principales

- **Administración** (`utp.demo`): catálogo, filtros, CRUD local de actividades y preguntas, galería, activación.
- **Asistente** (`asistente.aula2`): asignar actividad, generar código de 6 dígitos (30 min), resultados y CSV.
- **Estudiante**: ingreso por código, lectura/voz, respuestas y cierre sin nota.
- **Backend (equipo)**: CRUD de **actividades** vía Retrofit + Room (`idRemoto`), asignaciones y validación de códigos; SQLite compartido entre dispositivos con el mismo servidor.
- **API externa**: búsqueda **Open Library** en el editor de actividades de Lectura (Retrofit).

## Cuentas ficticias

| Uso | Dato |
| --- | --- |
| Docente / UTP | alias `utp.demo`, clave `Utp2026` |
| Asistente | alias `asistente.aula2`, clave `Aula2026` |

Los códigos que crea la asistente vencen a los 30 minutos en el servidor. No son claves institucionales.

## Requisitos

- **JDK 21** (backend) y **Android Studio** reciente (Android SDK 37).
- Emulador o dispositivo con **Android 9+** (`minSdk` 28).
- Para integración local: **Spring Boot** en el PC (`backend-moviles`) y, en emulador, `adb reverse tcp:8080 tcp:8080`.

## Microservicio — endpoints

Base URL típica en desarrollo: `http://127.0.0.1:8080/` (debug Android con `adb reverse`).

| Método | Ruta | Uso |
|--------|------|-----|
| GET | `/api/salud` | Comprobación de conectividad (Martín) |
| GET | `/api/actividades` | Catálogo remoto (Matías) |
| POST | `/api/actividades` | Crear actividad (Matías) |
| PUT | `/api/actividades/{id}` | Editar (Matías) |
| DELETE | `/api/actividades/{id}` | Eliminar (Matías) |
| POST | `/api/asignaciones` | Crear asignación + código (Hanael) |
| POST | `/api/codigos/validar` | Validar código de estudiante (Hanael) |

Persistencia del servidor: `mpazneb-servidor.db` (no borrar al evaluar). Tests Maven usan `mpazneb-test.db` (perfil `test`).

## API externa — Open Library

| Método | Base | Ejemplo |
|--------|------|---------|
| GET | `https://openlibrary.org` | Búsqueda vía `OpenLibraryService` (`/search.json`) |

La app conserva datos locales en Room; la búsqueda solo rellena la lista en pantalla (no sobrescribe la BD local automáticamente).

## Ejecutar el backend

```powershell
cd backend-moviles
# Verificar src/test/resources/application-test.properties → jdbc:sqlite:mpazneb-test.db
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

El servidor escucha en el puerto **8080** por defecto. `spring-boot:run` usa `mpazneb-servidor.db` sin borrar datos existentes.

JaCoCo backend: tras `mvn test`, abrir `target/site/jacoco/index.html`.

## Ejecutar la app Android

```powershell
cd mpaz-neb
.\gradlew.bat :app:assembleDebug
# Emulador + backend en el PC:
adb reverse tcp:8080 tcp:8080
```

Abrir el proyecto en Android Studio y **Run** en emulador tablet recomendado.

### URL del servidor en la app

| Variante | Configuración |
|----------|----------------|
| **Debug** | `BuildConfig.API_BASE_URL` = `http://127.0.0.1:8080/` + `adb reverse` |
| **Release** | Por defecto placeholder inválido. Para evaluación, definir antes de compilar: |

```powershell
# IP LAN del PC (dispositivo físico) o http://10.0.2.2:8080/ (emulador sin reverse)
.\gradlew.bat :app:assembleRelease -PmpazReleaseApiBaseUrl="http://10.0.2.2:8080/"
```

O variable de entorno `MPAZ_RELEASE_API_BASE_URL`.

`app/src/debug/res/xml/network_security_config.xml` permite cleartext a localhost en debug. Release permite cleartext solo a `10.0.2.2` (`app/src/main/res/xml/...`).

## Pruebas automatizadas

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:jacocoDebugUnitTestReport
# Cobertura IE 3.2.1 (solo código lógico):
.\gradlew.bat :app:jacocoDebugLogicalUnitTestReport
```

Informes:

- Global JVM: `app/build/reports/jacoco/jacocoDebugUnitTestReport/html/index.html`
- **Código lógico** (ViewModels, Repositories, `domain`, clientes Retrofit): `app/build/reports/jacoco/jacocoDebugLogicalUnitTestReport/html/index.html`

Instrumentadas (un emulador, sin backend en la suite habitual):

```powershell
$env:ANDROID_SERIAL="emulator-5554"
.\gradlew.bat :app:connectedDebugAndroidTest :app:jacocoDebugAndroidTestReport
```

Prueba de integración de Martín (`SaludApiInstrumentedTest`): ver [CASOS_DE_PRUEBA.md](CASOS_DE_PRUEBA.md).

Detalle de casos, matriz IE y evidencias: **[CASOS_DE_PRUEBA.md](CASOS_DE_PRUEBA.md)**.

## APK firmado (IE 3.3.1)

1. Copiar `keystore.properties.example` → `keystore.properties` (ignorado por Git).
2. Generar un `.jks` **solo para esta evaluación** (comando en el ejemplo). **No** subir contraseñas a GitHub.
3. Compilar:

```powershell
.\gradlew.bat :app:assembleRelease -PmpazReleaseApiBaseUrl="http://10.0.2.2:8080/"
```

4. APK firmado: `app/build/outputs/apk/release/app-release.apk` (con `keystore.properties` presente).
5. Verificar firma (Android SDK build-tools):

```powershell
apksigner verify --print-certs app\build\outputs\apk\release\app-release.apk
```

Entregar el `.jks` y el APK según la pauta AVA (el `.jks` puede ir en la entrega grupal, no en el repo público con contraseña).

**Captura del APK firmado:** pendiente hasta generar `keystore.properties` localmente.

## Flujo de demostración (con backend)

1. Levantar `backend-moviles` (`spring-boot:run`).
2. `adb reverse tcp:8080 tcp:8080` (emulador).
3. Login asistente → asignar → anotar código.
4. Segundo emulador o sesión estudiante → validar código.
5. Editor UTP → módulo Lectura → **Buscar libros** (Open Library).

## CRUD de actividades (IE 3.1.3)

Flujo en `ActividadRepository` (administración UTP):

| Operación | REST | Room |
|-----------|------|------|
| **Leer / sincronizar** | `GET /api/actividades` al abrir catálogo | Fusiona por `idRemoto` sin duplicar |
| **Crear / editar** | `POST` o `PUT` según `idRemoto` | Solo persiste si el servidor responde OK |
| **Cambiar estado** | `PUT` con estado actualizado | Actualiza metadata local |
| **Eliminar borrador** | `DELETE /api/actividades/{id}` (reglas Matías) | `eliminarBorrador(idLocal)` borra local tras 204 |

Las **preguntas** siguen solo en Room (el backend no las almacena).

## Resultados de pruebas (última verificación local)

| Suite | Resultado |
|-------|-----------|
| Android JVM | **80 tests**, 0 fallos |
| Android instrumentadas (suite aislada) | **7 tests** (sin re-ejecutar en este cierre) |
| Backend `mvn test` | **9 tests**, 0 fallos |
| Cobertura lógica Android | **47,51 %** (762/1604 líneas) — ver `jacocoDebugLogicalUnitTestReport` |

**APK firmado:** pendiente (configuración lista en `keystore.properties.example`; no incluido en esta entrega).

## Limitaciones conocidas

- Preguntas y alternativas **no** se sincronizan entre dispositivos (solo metadata de actividad en el servidor).
- Cobertura lógica aún **por debajo del 80 %** exigido en IE 3.2.1; ver clases pendientes en CASOS_DE_PRUEBA.
- Release sin URL configurada no puede hablar con el backend.

## Organización

- Arquitectura: [ARQUITECTURA.md](ARQUITECTURA.md)
- Planificación Trello: https://trello.com/b/0EkPsNiH/mpaz-neb-dsy1105 (aportar capturas en la entrega AVA)
- Evidencias y matriz de indicadores IE: [CASOS_DE_PRUEBA.md](CASOS_DE_PRUEBA.md)