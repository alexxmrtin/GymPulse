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
| Base de datos | H2 embebida en archivo *(provisional)* → PostgreSQL en producción |
| Seguridad | Spring Security + OAuth2 Resource Server (JWT HS256) |
| Contraseñas | BCrypt |
| Logging | SLF4J + Logback |

> **[BACKEND/funcionamientoBackend.md](BACKEND/funcionamientoBackend.md)** explica el backend entero: el
> recorrido de una petición, qué hace cada clase y cómo funciona la seguridad.

### Estructura de paquetes

```
com.gympulse.backend/
├── controllers/    Endpoints REST — reciben peticiones HTTP y devuelven respuestas
├── services/       Lógica de negocio — validaciones y reglas de la app
├── repositories/   Acceso a datos — interfaces JPA generadas por Spring
├── models/         Entidades JPA — mapean a tablas de la base de datos
├── dtos/           Records que entran y salen por la API — nunca se exponen las entidades
├── core/           Configuración global (seguridad y JWT, CORS, BCrypt, manejo de errores)
├── middlewares/    Vacío: el filtro del JWT lo pone Spring Security desde core/SeguridadConfig
└── helpers/        Mapeador entidad↔DTO, validaciones, id del token y detección del tipo de imagen
```

Las entidades JPA no salen nunca en las respuestas: se convierten a DTO en la capa de servicio.
Así se evita el ciclo `Usuario → entrenamientos → usuario`, no se filtra el hash de la contraseña
y las colecciones perezosas se leen dentro de la transacción.

### Modelo de datos

```
usuarios ──── fotos_perfil        (1 a 1, opcional)
  └──< entrenamientos
             └──< series >── ejercicios
```

| Entidad | Tabla | Descripción |
|---|---|---|
| Usuario | `usuarios` | Cuenta, credenciales, datos físicos y `fotoActualizadaEn` (cuándo cambió la foto) |
| Ejercicio | `ejercicios` | Catálogo de ejercicios disponibles |
| Entrenamiento | `entrenamientos` | Sesión de entreno de un usuario en una fecha |
| Serie | `series` | Ejercicio + repeticiones + peso dentro de un entrenamiento |
| FotoPerfil | `fotos_perfil` | Imagen de perfil de un usuario (bytes + tipo), en tabla aparte para no cargarla con cada `Usuario` |

### Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/usuario` | Registrar usuario |
| POST | `/usuario/login` | Comprobar email + contraseña y devolver un JWT (401 si no cuadran) |
| GET | `/usuario/{id}` | Obtener usuario por id |
| PUT | `/usuario/{id}` | Actualizar usuario |
| DELETE | `/usuario/{id}` | Eliminar usuario |
| PUT | `/usuario/{id}/foto` | Subir o sustituir la foto de perfil (`multipart/form-data`, campo `foto`; JPEG, PNG o WebP, máx. 5 MB) |
| GET | `/usuario/{id}/foto` | Descargar la foto de perfil (404 si no tiene) |
| DELETE | `/usuario/{id}/foto` | Quitar la foto de perfil |
| POST | `/ejercicio` | Crear ejercicio |
| GET | `/ejercicio` | Listar todos / filtrar por `?nombre=` o `?grupoMuscular=` |
| GET | `/ejercicio/{id}` | Obtener ejercicio por id |
| DELETE | `/ejercicio/{id}` | Eliminar ejercicio |
| POST | `/entrenamiento` | Crear entrenamiento (el dueño sale del token) |
| GET | `/entrenamiento` | Mis entrenamientos (orden descendente) |
| GET | `/entrenamiento/{id}` | Obtener un entrenamiento propio |
| DELETE | `/entrenamiento/{id}` | Eliminar un entrenamiento propio |
| POST | `/serie` | Crear serie en un entrenamiento propio |
| GET | `/serie?entrenamientoId=` | Series de un entrenamiento propio |
| GET | `/serie?ejercicioId=` | Mis series de un ejercicio (progresión) |
| DELETE | `/serie/{id}` | Eliminar una serie propia |

Salvo `POST /usuario` y `POST /usuario/login`, **todos exigen** la cabecera
`Authorization: Bearer <token>`. Los endpoints de usuario solo operan sobre la propia cuenta.

### Arrancar el backend

La primera vez hay que crear el archivo de secretos, que no está en git:

```bash
cd BACKEND
echo "JWT_SECRETO=$(openssl rand -base64 48)" > .env
```

Y ya:

```bash
mvn spring-boot:run
```

> Si falta el `.env` la app no arranca y avisa con `Could not resolve placeholder 'JWT_SECRETO'`.
> En producción no se usa `.env`: la variable se define en el entorno del servidor.

La API queda disponible en `http://localhost:8080`.

> La BD es H2 embebida — no requiere servidor ni configuración extra. Los datos viven en
> `BACKEND/data/gympulse.mv.db` y se pueden inspeccionar en `http://localhost:8080/h2-console`
> (JDBC URL `jdbc:h2:file:./data/gympulse`, usuario `sa`, sin contraseña).
> Los logs se escriben en `BACKEND/logs/gympulse.log`.

Los tests se lanzan con `mvn test` y usan una BD en memoria, así que no tocan la tuya.

### Autenticación

`POST /usuario/login` devuelve un JWT firmado con HS256:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEnSegundos": 43200,
  "usuario": { "id": 1, "username": "alex", "...": "..." }
}
```

Dentro del token van el emisor, el id del usuario (`sub`), el `username` y las fechas de emisión
y caducidad. La clave de firma sale de `JWT_SECRETO` (archivo `BACKEND/.env` en local, variable
de entorno en producción) y la duración de `gympulse.jwt.duracion-minutos`.

A partir de ahí, cada petición manda el token:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Spring valida la firma y la caducidad en un filtro, antes de llegar al controlador, y el id del
usuario se saca del token (nunca de un parámetro), así que nadie puede pedir datos ajenos
cambiando un id en la URL. Un entrenamiento o una serie de otro usuario responden **404 y no
403**, para no confirmar siquiera que ese id existe.

### Códigos de error

Todos los errores devuelven el mismo JSON (`timestamp`, `codigo`, `mensaje`):

| Código | Cuándo |
|---|---|
| 400 | Falta un campo obligatorio, o la imagen subida no es JPEG, PNG ni WebP |
| 401 | Credenciales incorrectas, o token ausente / caducado / manipulado |
| 403 | Intentas operar sobre la cuenta de otro usuario |
| 404 | El recurso no existe, o es de otro usuario |
| 409 | Email o username ya registrados, o se borra algo que aún está referenciado |
| 413 | La imagen subida supera los 5 MB |
| 500 | Cualquier otro error |

---

## Frontend Android

### Tecnologías

| | |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 (BOM 2026.02.01) |
| Navegación | Navigation Compose 2.9.8 con rutas tipadas |
| Red | Retrofit 2.11 + OkHttp 4.12 + kotlinx.serialization |
| Imágenes | Coil 3.4.0 *(fijado: las versiones nuevas exigen Kotlin 2.4 y compileSdk 37)* |
| Persistencia local | DataStore (token de sesión y preferencias) |
| Android | minSdk 26 · targetSdk 36 |
| IDE | Android Studio Quail 1 (2026.1.1) |
| Build | Gradle + AGP 9.2.1 |

### Estructura de paquetes

```
com.gympulse.android/
├── data/
│   ├── api/            Retrofit: interfaz de la API, cliente HTTP, interceptor del token y errores
│   ├── modelo/         data class calcadas de los DTO del backend
│   ├── repositorios/   Puerta de entrada a la API para las pantallas; devuelven Result<T>
│   ├── sesion/         Token guardado en DataStore y estado de la sesión
│   ├── preferencias/   Unidades (kg/lb) y notificaciones, solo en el móvil
│   └── perfil/         Subida de la foto de perfil (se reduce a 512 px antes de enviarla)
└── ui/
    ├── navegacion/     Rutas y grafo de navegación; la sesión decide si se ve el login
    ├── theme/          Paleta, tipografía y formas
    ├── comun/          Estado de carga, fábrica de ViewModels, formato y cálculos
    ├── componentes/    Piezas reutilizables (barras, tarjetas, campos, gráficas…)
    └── <pantalla>/     Una carpeta por pantalla, con su Pantalla y su ViewModel
```

Cada pantalla tiene un ViewModel que expone su estado con `StateFlow` y habla con los
repositorios. Las dependencias se montan a mano en `Contenedor` (`AppGymPulse.kt`), sin Hilt.

### Pantallas

- **Login / Registro** — validación en el formulario; al registrarte entras directamente
- **Entrenamiento** — empezar un entreno, continuar el de hoy y ver los recientes
  - **Entreno en curso** — cronómetro, series agrupadas por ejercicio, añadir y borrar series
  - **Ejercicios** — catálogo con búsqueda y filtro por grupo muscular; crear y borrar
- **Perfil** — foto, datos físicos, racha semanal, calendario de entrenos y último entreno
  - **Historial** y **detalle** de cada entrenamiento
  - **Estadísticas** — entrenos por semana y progresión de peso por ejercicio
- **Ajustes** — datos personales, cuenta (borrarla), notificaciones, unidades, idioma, tema y
  cerrar sesión

### Arrancar el frontend

1. Arranca el backend (ver arriba). La app no funciona sin él.
2. Abre `FRONTEND/Android/` en Android Studio (`/opt/android-studio/bin/studio.sh`).
3. Si has cambiado dependencias, haz **Sync Project with Gradle Files**.
4. Elige el emulador y pulsa **▶ Run 'app'**.

> El emulador ve el `localhost` del ordenador como `10.0.2.2`, que es la dirección que usa la app
> (`ClienteApi.URL_BASE`). En un móvil físico hay que cambiarla por la IP del PC en la red local,
> también en `res/xml/network_security_config.xml`.

Los tests de la lógica (cálculos, estadísticas y formato) se lanzan con
`./gradlew :app:testDebugUnitTest` desde `FRONTEND/Android/`.

---

## Pendiente

- [x] Manejo de errores global (backend)
- [x] DTOs y contraseñas cifradas
- [x] Autenticación con JWT y API cerrada
- [ ] Refresh tokens (ahora el token dura 12 h y al caducar toca volver a hacer login)
- [ ] Validación con `spring-boot-starter-validation` en vez de comprobaciones manuales
- [ ] Login con Google (OAuth 2.0)
- [ ] Migración a PostgreSQL
- [x] Desarrollo de frontend al completo
- [x] Conectar frontend Android con la API
- [x] Foto de perfil guardada en el backend
- [ ] Rutinas (necesitan soporte en el backend)
- [ ] Idioma inglés, tema claro y envío real de notificaciones (ahora solo se guarda la preferencia)

