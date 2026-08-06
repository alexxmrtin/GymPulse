# GymPulse — Stack

App de gimnasio con **un backend compartido** y **clientes nativos móviles** que consumen la misma API REST.

```
GymPulse/
├── BACKEND/            ← Spring Boot 4 + Java 21 (API REST/JSON)
└── FRONTEND/
    ├── Android/        ← cliente Android (Kotlin + Jetpack Compose)
    └── IOS/            ← pendiente: requiere macOS + Xcode
```

**Objetivo de aprendizaje:** desarrollo móvil nativo. El backend se hace en Java por ser lo
mayoritario en ofertas de Spring Boot; el frontend en Kotlin porque es el lenguaje preferente
de Google para Android desde 2019.

---

## Backend

| | |
|---|---|
| **Framework** | Spring Boot 4.1.0 |
| **Lenguaje** | Java 21 (LTS) |
| **JDK** | Temurin 21.0.9 (gestionado con SDKMAN) |
| **Build** | Maven 3.9.16 (instalado con SDKMAN, sin wrapper) |
| **Dependencias** | Web MVC, Data JPA, Validation, Security, Actuator |
| **Base de datos** | H2 *(provisional — sin servidor, embebida en memoria)* → PostgreSQL en producción |
| **Logging** | SLF4J + Logback *(incluido en Spring Boot)* — logs en `BACKEND/logs/gympulse.log` |
| **Ruta** | `BACKEND/` |

Expone una API REST/JSON agnóstica al cliente: Android e iOS consumirían los mismos endpoints.

---

## Frontend Android

| | |
|---|---|
| **UI** | Jetpack Compose |
| **Lenguaje** | Kotlin |
| **IDE** | Android Studio Quail 1 (2026.1.1 P2), instalado en `/opt/android-studio` |
| **Build** | Gradle + Android Gradle Plugin |
| **SDK** | Android SDK — *pendiente de instalar* (lo descarga Studio al primer arranque) |
| **Cliente HTTP** | Retrofit + kotlinx.serialization (por confirmar) |
| **Ruta** | `FRONTEND/Android/` |

---

## Frontend iOS

**Bloqueado.** Compilar y firmar una app iOS requiere macOS con Xcode; no es posible desde
Fedora, ni con Swift ni con Kotlin Multiplatform. La carpeta queda como marcador hasta
disponer de un Mac.

---

## Cómo arrancar cada parte

### Backend

```bash
cd BACKEND
mvn spring-boot:run
```

> La BD es H2 embebida: no requiere servidor ni configuración extra.

La API queda disponible en `http://localhost:8080`.

Otros comandos:

```bash
mvn test          # tests
mvn clean package # genera el jar en target/
```

### Frontend Android

Desde el icono del escritorio, o por terminal:

```bash
/opt/android-studio/bin/studio.sh
```

Primer arranque: descarga el Android SDK y pide aceptar licencias.
Después, abrir `FRONTEND/Android/` como proyecto y ejecutar en emulador o dispositivo.
