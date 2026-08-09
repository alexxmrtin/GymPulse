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
| Contraseñas | BCrypt (spring-security-crypto) |
| Logging | SLF4J + Logback |

### Estructura de paquetes

```
com.gympulse.backend/
├── controllers/    Endpoints REST — reciben peticiones HTTP y devuelven respuestas
├── services/       Lógica de negocio — validaciones y reglas de la app
├── repositories/   Acceso a datos — interfaces JPA generadas por Spring
├── models/         Entidades JPA — mapean a tablas de la base de datos
├── dtos/           Records que entran y salen por la API — nunca se exponen las entidades
├── core/           Configuración global (CORS, BCrypt, manejo de errores)
├── middlewares/    Filtros e interceptores (pendiente: JWT)
└── helpers/        Mapeador entidad↔DTO y validaciones
```

Las entidades JPA no salen nunca en las respuestas: se convierten a DTO en la capa de servicio.
Así se evita el ciclo `Usuario → entrenamientos → usuario`, no se filtra el hash de la contraseña
y las colecciones perezosas se leen dentro de la transacción.

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
| POST | `/usuario/login` | Comprobar email + contraseña y devolver un JWT (401 si no cuadran) |
| GET | `/usuario/{id}` | Obtener usuario por id |
| PUT | `/usuario/{id}` | Actualizar usuario |
| DELETE | `/usuario/{id}` | Eliminar usuario |
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
cp .env.example .env
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
| 400 | Falta un campo obligatorio |
| 401 | Credenciales incorrectas, o token ausente / caducado / manipulado |
| 403 | Intentas operar sobre la cuenta de otro usuario |
| 404 | El recurso no existe, o es de otro usuario |
| 409 | Email o username ya registrados, o se borra algo que aún está referenciado |
| 500 | Cualquier otro error |

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

- [x] Manejo de errores global (backend)
- [x] DTOs y contraseñas cifradas
- [x] Autenticación con JWT y API cerrada
- [ ] Refresh tokens (ahora el token dura 12 h y al caducar toca volver a hacer login)
- [ ] Validación con `spring-boot-starter-validation` en vez de comprobaciones manuales
- [ ] Login con Google (OAuth 2.0)
- [ ] Migración a PostgreSQL
- [ ] Desarrollo de frontend al completo
- [ ] Conectar frontend Android con la API
- [ ] Frontend iOS (requiere macOS)
