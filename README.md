# GymPulse

Aplicación móvil de registro y seguimiento de entrenamientos de gimnasio. Arquitectura cliente-servidor con un backend REST compartido y clientes nativos móviles.

---

## Estructura del proyecto

```
GymPulse/
├── BACKEND/        API REST — Spring Boot 4 + Java 21
└── FRONTEND/
    ├── Android/    Cliente Android — Kotlin + Jetpack Compose
    └── IOS/        Futuro
```

---

## Backend

### Tecnologías

| | |
|---|---|
| Framework | Spring Boot 4.1.0 |
| Lenguaje | Java 21 (LTS) |
| JDK | Temurin 21.0.9 (SDKMAN) |
| Build | Maven 3.9.16 (SDKMAN) |
| Base de datos | H2 embebida *(provisional)* → PostgreSQL en producción |
| Logging | SLF4J + Logback |

### Estructura de paquetes

```
com.gympulse.backend/
├── controllers/    Endpoints REST — reciben peticiones HTTP y devuelven respuestas
├── services/       Lógica de negocio — validaciones y reglas de la app
├── repositories/   Acceso a datos — interfaces JPA generadas por Spring
├── models/         Entidades JPA — mapean a tablas de la base de datos
├── core/           Configuración global (CORS...)
├── middlewares/    Filtros e interceptores (pendiente: JWT)
└── helpers/        Utilidades generales
```

### Modelo de datos

```
usuarios
  └──< entrenamientos
             └──< series >── ejercicios
```

| Entidad | Tabla | Descripción |
|---|---|---|
| Usuario | `usuarios` | Cuenta, credenciales y datos físicos |
| Ejercicio | `ejercicios` | Catálogo de ejercicios disponibles |
| Entrenamiento | `entrenamientos` | Sesión de entreno de un usuario en una fecha |
| Serie | `series` | Ejercicio + repeticiones + peso dentro de un entrenamiento |

### Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/usuario` | Registrar usuario |
| GET | `/usuario/{id}` | Obtener usuario por id |
| PUT | `/usuario/{id}` | Actualizar usuario |
| DELETE | `/usuario/{id}` | Eliminar usuario |
| POST | `/ejercicio` | Crear ejercicio |
| GET | `/ejercicio` | Listar todos / filtrar por `?nombre=` o `?grupoMuscular=` |
| GET | `/ejercicio/{id}` | Obtener ejercicio por id |
| DELETE | `/ejercicio/{id}` | Eliminar ejercicio |
| POST | `/entrenamiento` | Crear entrenamiento |
| GET | `/entrenamiento/{id}` | Obtener entrenamiento por id |
| GET | `/entrenamiento?usuarioId=` | Entrenamientos de un usuario (orden descendente) |
| DELETE | `/entrenamiento/{id}` | Eliminar entrenamiento |
| POST | `/serie` | Crear serie |
| GET | `/serie?entrenamientoId=` | Series de un entrenamiento |
| GET | `/serie?ejercicioId=` | Series de un ejercicio |
| DELETE | `/serie/{id}` | Eliminar serie |

### Arrancar el backend

```bash
cd BACKEND
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

> La BD es H2 embebida — no requiere servidor ni configuración extra. Los logs se escriben en `BACKEND/logs/gympulse.log`.

---

## Frontend Android

### Tecnologías

| | |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose |
| IDE | Android Studio Quail 1 (2026.1.1) |
| Build | Gradle + AGP 9.2.1 |
| Compose BOM | 2026.02.01 |

### Pantallas implementadas

- **Login / Registro**
- **Perfil** — datos del usuario, calendario de entrenos, acceso a estadísticas e historial
- **Entrenamiento** — iniciar entrenamiento vacío, gestión de rutinas, catálogo de ejercicios
- **Ajustes** — perfil, cuenta, notificaciones, idioma, tema, unidades

### Arrancar el frontend

```bash
/opt/android-studio/bin/studio.sh
```

Abrir `FRONTEND/Android/` como proyecto y ejecutar en emulador o dispositivo físico.

---

## Pendiente

- [ ] Manejo de errores global (backend)
- [ ] Autenticación OAUTH 2.0 + JWT
- [ ] Migración a PostgreSQL
- [ ] Desarrollo de frontend al completo
- [ ] Conectar frontend Android con la API
- [ ] Frontend iOS (requiere macOS)
