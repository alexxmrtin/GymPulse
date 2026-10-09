# GymPulse

Aplicación móvil para registrar y seguir entrenamientos de gimnasio: series, pesos, progresión por
ejercicio y constancia semanal. Arquitectura cliente-servidor con una **API REST en Spring Boot**
y un **cliente nativo Android en Kotlin + Jetpack Compose**.

**Versión 1.0**

---

## Índice

- [Qué hace](#qué-hace)
- [Arquitectura](#arquitectura)
- [Puesta en marcha](#puesta-en-marcha)
- [Backend](#backend)
- [App Android](#app-android)
- [Cómo funciona el login con Google](#cómo-funciona-el-login-con-google)
- [Seguridad](#seguridad)
- [Hoja de ruta](#hoja-de-ruta)

---

## Qué hace

- **Cuentas:** registro con verificación del email por correo, login con contraseña o con Google,
  sesión que se renueva sola y foto de perfil.
- **Entrenamientos:** empezar un entreno, ir añadiendo series (ejercicio, repeticiones y peso) con
  cronómetro, y consultar el historial con el detalle de cada sesión.
- **Ejercicios:** catálogo de 36 ejercicios predefinidos por grupo muscular, más los ejercicios
  personalizados de cada usuario, con búsqueda y filtros.
- **Progreso:** perfil con calendario de entrenos y racha semanal, y estadísticas con los entrenos
  por semana y la progresión de peso de cada ejercicio.
- **Ajustes:** datos personales, unidades (kg/lb), notificaciones, borrar la cuenta y cerrar sesión.

---

## Arquitectura

```
┌──────────────────────┐   HTTPS + JSON    ┌───────────────────────┐   JDBC    ┌──────────────┐
│ App Android          │ ────────────────> │ API REST              │ ────────> │ PostgreSQL   │
│ Kotlin + Compose     │   JWT (Bearer)    │ Spring Boot 4 · Java  │           │ 17           │
└──────────┬───────────┘                   └───────────┬───────────┘           └──────────────┘
           │ ID token (login con Google)               │ correos de verificación
           ▼                                           ▼
     Google (OpenID Connect)                     Servidor SMTP
```

```
GymPulse/
├── BACKEND/            API REST — Spring Boot 4 + Java 21 + PostgreSQL
│   ├── funcionamientoBackend.md   el backend explicado a fondo
│   └── BaseDeDatos.md             el esquema de la base de datos
└── FRONTEND/
    ├── Android/        Cliente Android — Kotlin + Jetpack Compose
    └── IOS/            Pendiente (requiere macOS)
```

---

## Puesta en marcha

**Requisitos:** JDK 21, Maven, Docker o Podman y Android Studio con un emulador que tenga Google Play.

**1. Backend.** La primera vez, crea el archivo de secretos (no está en git; `BACKEND/.env.example`
tiene la plantilla):

```bash
cd BACKEND
echo "JWT_SECRETO=$(openssl rand -base64 48)" > .env
echo "POSTGRES_USUARIO=gympulse" >> .env
echo "POSTGRES_CONTRASENA=$(openssl rand -hex 16)" >> .env
```

Y cada vez:

```bash
docker compose up -d      # PostgreSQL 17 en un contenedor (con Podman: podman compose up -d)
mvn spring-boot:run       # API en http://localhost:8080
```

**2. App.** Abre `FRONTEND/Android/` en Android Studio, haz **Sync Project with Gradle Files** si
han cambiado dependencias, elige el emulador y pulsa **▶ Run 'app'**.

**3. Primera cuenta.** Regístrate desde la app. En local no hay servidor de correo: el enlace de
verificación aparece en el log del backend
(`[CORREO sin SMTP] ... enlace: http://localhost:8080/usuario/verificar?token=...`). Ábrelo en el
navegador y ya puedes entrar.

> Los tests (`mvn test` en el backend, `./gradlew :app:testDebugUnitTest` en la app) se mantienen
> solo en local: sus carpetas están ignoradas en git.

---

## Backend

> **[BACKEND/funcionamientoBackend.md](BACKEND/funcionamientoBackend.md)** explica el backend
> entero: el recorrido de una petición, qué hace cada clase y por qué.

### Tecnologías

| | |
|---|---|
| Framework | Spring Boot 4.1.0 |
| Lenguaje | Java 21 (Temurin 21.0.9, vía SDKMAN) |
| Build | Maven 3.9.16 |
| Base de datos | PostgreSQL 17 (Docker en local) |
| ORM | Hibernate (Spring Data JPA), que crea y actualiza las tablas |
| Seguridad | Spring Security + OAuth2 Resource Server (JWT HS256), BCrypt |
| Validación | Bean Validation (`spring-boot-starter-validation`) |
| Correo | Spring Mail (SMTP), envío asíncrono |
| Logging | SLF4J + Logback |

### Estructura

```
com.gympulse.backend/
├── controllers/    Endpoints REST: reciben peticiones HTTP y devuelven respuestas
├── services/       Lógica de negocio, tokens, correo y límite de intentos de login
├── repositories/   Acceso a datos: interfaces de Spring Data JPA
├── models/         Entidades JPA
├── dtos/           Records que entran y salen por la API, con sus reglas de validación
├── core/           Configuración (seguridad, JWT, CORS, asincronía), manejo de errores y
│                   catálogo de ejercicios predefinidos que se carga al arrancar
├── middlewares/    Vacío: el filtro del JWT lo pone Spring Security desde core/SeguridadConfig
└── helpers/        Mapeador entidad↔DTO, id del token, tokens aleatorios, privacidad, tipo de imagen
```

Decisiones de diseño:

- **Las entidades no salen nunca por la API:** se convierten a DTO en la capa de servicio. Así no
  se filtra el hash de la contraseña, se evita el ciclo `Usuario → entrenamientos → usuario` y las
  colecciones perezosas se leen dentro de la transacción.
- **La entrada se valida en los DTOs** (`@NotBlank`, `@Email`, `@Size`, `@Positive`…) con `@Valid`
  en los controladores: si algo falla, 400 con todos los campos que no cumplen, antes de llegar al
  servicio.
- **Las tablas las crea Hibernate** a partir de las entidades (`ddl-auto=update`): al arrancar
  añade lo que falte sin borrar datos. Solo sabe añadir; si hay que renombrar o quitar una columna,
  se hace a mano en la base de datos.

### Modelo de datos

```
usuarios ──── fotos_perfil              (1 a 1, opcional)
  ├──< tokens_refresco                  (una fila por sesión abierta)
  ├──< ejercicios                       (los personalizados; los predefinidos no tienen dueño)
  └──< entrenamientos
             └──< series >── ejercicios
```

| Tabla | Qué guarda |
|---|---|
| `usuarios` | Cuenta, credenciales, datos físicos, verificación del email e id de Google |
| `ejercicios` | Catálogo: predefinidos (sin dueño) y personalizados de cada usuario |
| `entrenamientos` | Sesión de entreno de un usuario en una fecha |
| `series` | Ejercicio + repeticiones + peso dentro de un entrenamiento |
| `tokens_refresco` | Sesiones abiertas: hash SHA-256 del token de refresco, caducidad y revocación |
| `fotos_perfil` | Imagen de perfil (bytes + tipo), aparte para no cargarla con cada usuario |

Columnas y restricciones en **[BACKEND/BaseDeDatos.md](BACKEND/BaseDeDatos.md)**.

### Endpoints

Salvo los de **cuenta y sesión**, todos exigen `Authorization: Bearer <token>`, y solo operan sobre
los datos del propio usuario.

**Cuenta y sesión** (públicos)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/usuario` | Registrarse. Responde siempre **202** con el mismo mensaje; el resto va por correo |
| GET | `/usuario/verificar?token=` | Enlace del correo de verificación; devuelve una página HTML |
| POST | `/usuario/verificacion/reenviar` | Volver a mandar el correo (202 siempre, como mucho uno por minuto) |
| POST | `/usuario/login` | Email + contraseña → JWT de acceso + token de refresco |
| POST | `/usuario/login/google` | ID token de Google → crea o vincula la cuenta y devuelve la misma sesión |
| POST | `/usuario/refresco` | Cambiar el token de refresco por una sesión nueva; el usado deja de valer |
| POST | `/usuario/logout` | Revocar el token de refresco (204) |

**Perfil**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/usuario/{id}` | Mis datos |
| PUT | `/usuario/{id}` | Actualizar peso, altura y fecha de nacimiento |
| DELETE | `/usuario/{id}` | Borrar la cuenta con todos sus datos |
| PUT | `/usuario/{id}/foto` | Subir o sustituir la foto (`multipart/form-data`, campo `foto`; JPEG, PNG o WebP, máx. 5 MB) |
| GET | `/usuario/{id}/foto` | Descargar la foto (404 si no hay) |
| DELETE | `/usuario/{id}/foto` | Quitar la foto |

**Ejercicios**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/ejercicio` | Predefinidos + mis personalizados; filtros `?nombre=` o `?grupoMuscular=` |
| GET | `/ejercicio/{id}` | Un ejercicio predefinido o propio |
| POST | `/ejercicio` | Crear un ejercicio personalizado (409 si ya hay uno visible con ese nombre) |
| DELETE | `/ejercicio/{id}` | Borrar un ejercicio propio (403 si es predefinido, 409 si tiene series) |

**Entrenamientos y series**

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/entrenamiento` | Mis entrenamientos, del más reciente al más antiguo, con sus series |
| GET | `/entrenamiento/{id}` | Un entrenamiento propio |
| POST | `/entrenamiento` | Crear un entrenamiento (el dueño sale del token) |
| DELETE | `/entrenamiento/{id}` | Borrar un entrenamiento propio con sus series |
| POST | `/serie` | Añadir una serie a un entrenamiento propio, con un ejercicio visible |
| GET | `/serie?entrenamientoId=` | Series de un entrenamiento propio |
| GET | `/serie?ejercicioId=` | Mis series de un ejercicio (progresión) |
| DELETE | `/serie/{id}` | Borrar una serie propia |

### Autenticación y sesión

**Registro.** La cuenta nace **sin verificar**: llega un correo con un enlace que caduca en 24 h, y
hasta abrirlo el login responde 403. El registro responde igual exista o no el email (si ya
existía, el correo avisa al dueño).

**Login.** Devuelve dos tokens:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEnSegundos": 900,
  "tokenRefresco": "q3Zc1...",
  "tokenRefrescoExpiraEnSegundos": 2592000,
  "usuario": { "id": 1, "username": "alex", "...": "..." }
}
```

- **`token`** es el JWT de acceso, firmado con HS256. Dura **15 minutos** y va en cada petición.
  Lleva el emisor, el id del usuario (`sub`), el `username` y las fechas de emisión y caducidad.
- **`tokenRefresco`** dura **30 días** y solo sirve para pedir una sesión nueva. Es una cadena
  aleatoria (no un JWT) y en la base de datos solo se guarda su hash. Cada vez que se usa **rota**:
  el viejo deja de valer y llega uno nuevo. Si alguien presenta uno ya usado, se entiende que lo
  han robado y se revocan todas las sesiones del usuario.

La app renueva el token de acceso sola cuando recibe un 401, sin que el usuario lo note, y solo
vuelve al login si el refresco también falla. También se puede entrar con Google
([cómo funciona](#cómo-funciona-el-login-con-google)).

**Cada petición.** Spring valida la firma y la caducidad del JWT en un filtro, antes de llegar al
controlador. El id del usuario sale del token, nunca de un parámetro, así que nadie puede pedir
datos ajenos cambiando un id en la URL. Un recurso de otro usuario responde **404 y no 403**, para
no confirmar siquiera que existe.

La clave de firma sale de `JWT_SECRETO` y las duraciones de `gympulse.jwt.duracion-minutos` y
`gympulse.jwt.refresco-dias`.

### Códigos de error

Todos los errores devuelven el mismo JSON (`timestamp`, `codigo`, `mensaje`):

| Código | Cuándo |
|---|---|
| 400 | Datos inválidos (se listan todos los campos que fallan), JSON mal formado, o imagen que no es JPEG, PNG ni WebP |
| 401 | Credenciales incorrectas, o token ausente, caducado o manipulado |
| 403 | Email sin verificar al hacer login, operar sobre la cuenta de otro o borrar un ejercicio predefinido |
| 404 | El recurso no existe, o es de otro usuario |
| 409 | Email, username o nombre de ejercicio ya existentes, o borrar algo que aún está referenciado |
| 413 | La imagen supera los 5 MB |
| 429 | Demasiados logins fallidos; `Retry-After` dice cuántos segundos esperar |
| 500 | Cualquier otro error |

### Base de datos

- **Dónde vive:** en el volumen de Docker `gympulse-datos`, que sobrevive a reiniciar o borrar el
  contenedor (`docker compose down`). Para empezar de cero: `docker compose down -v`.
- **Cómo consultarla:** `docker exec -it gympulse-postgres psql -U gympulse gympulse`, o un cliente
  gráfico (DBeaver, IntelliJ…) contra `localhost:5432` con el usuario y la contraseña del `.env`.
- **Logs** del backend: `BACKEND/logs/gympulse.log`.

### Producción

```bash
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

El perfil `prod` (`application-prod.properties`) apaga `show-sql`, baja los logs a INFO, redirige
todo a HTTPS (detrás de un proxy que termine TLS respeta `X-Forwarded-Proto`), limita CORS y manda
los correos de verdad por SMTP. Necesita estas variables de entorno, y sin ellas no arranca:

| Variable | Para qué |
|---|---|
| `JWT_SECRETO` | Clave de firma de los JWT (mínimo 32 caracteres) |
| `DATABASE_URL`, `DATABASE_USUARIO`, `DATABASE_CONTRASENA` | PostgreSQL de producción (`jdbc:postgresql://host:5432/base`) |
| `GYMPULSE_URL_PUBLICA` | URL pública del backend, para los enlaces de los correos |
| `GYMPULSE_CORS_ORIGENES` | Orígenes web permitidos, separados por comas |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USUARIO`, `MAIL_CONTRASENA`, `MAIL_REMITENTE` | Servidor SMTP |

---

## App Android

### Tecnologías

| | |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 (BOM 2026.02.01) |
| Navegación | Navigation Compose 2.9.8 con rutas tipadas |
| Red | Retrofit 2.11 + OkHttp 4.12 + kotlinx.serialization |
| Imágenes | Coil 3.4.0 *(fijado: las versiones nuevas exigen Kotlin 2.4 y compileSdk 37)* |
| Login con Google | Credential Manager 1.6.0 + googleid 1.1.1 *(la 1.2 exige un Kotlin más nuevo)* |
| Persistencia local | DataStore (sesión y preferencias) |
| Android | minSdk 26 · targetSdk 36 |
| IDE / build | Android Studio Quail 1 (2026.1.1) · Gradle + AGP 9.2.1 |

### Estructura

```
com.gympulse.android/
├── data/
│   ├── api/            Retrofit: interfaces de la API, clientes HTTP, interceptor y renovador del token, errores
│   ├── modelo/         data class calcadas de los DTO del backend
│   ├── repositorios/   Puerta de entrada a la API para las pantallas; devuelven Result<T>
│   ├── sesion/         Tokens guardados en DataStore y estado de la sesión
│   ├── preferencias/   Unidades (kg/lb) y notificaciones, solo en el móvil
│   └── perfil/         Subida de la foto de perfil (se reduce a 512 px antes de enviarla)
└── ui/
    ├── navegacion/     Rutas y grafo de navegación; la sesión decide si se ve el login
    ├── theme/          Paleta, tipografía y formas
    ├── comun/          Estado de carga, fábrica de ViewModels, formato y cálculos
    ├── componentes/    Piezas reutilizables (barras, tarjetas, campos, gráficas…)
    └── <pantalla>/     Una carpeta por pantalla, con su Pantalla y su ViewModel
```

- **MVVM:** cada pantalla tiene un ViewModel que expone su estado con `StateFlow` y habla con los
  repositorios; la pantalla es Compose y solo pinta ese estado.
- **Dependencias a mano:** se montan en `Contenedor` (`AppGymPulse.kt`), sin Hilt.
- **Sesión transparente:** un `Authenticator` de OkHttp renueva el token ante un 401 y repite la
  petición; si el backend rechaza el refresco, la app vuelve al login.

### Pantallas

- **Login / Registro:** validación en el formulario y «Continuar con Google». Tras registrarte,
  aviso de «revisa tu correo», y si intentas entrar sin verificar, botón para reenviarlo.
- **Entrenamiento:** empezar un entreno, continuar el de hoy y ver los recientes.
  - **Entreno en curso:** cronómetro, series agrupadas por ejercicio, añadir y borrar series.
  - **Ejercicios:** predefinidos y personalizados, con búsqueda y filtros; crear y borrar los tuyos.
- **Perfil:** foto, datos físicos, racha semanal, calendario de entrenos y último entreno.
  - **Historial** y **detalle** de cada entrenamiento.
  - **Estadísticas:** entrenos por semana y progresión de peso por ejercicio.
- **Ajustes:** datos personales, cuenta (borrarla), notificaciones, unidades, idioma, tema y
  cerrar sesión.

### Conexión con el backend

El emulador ve el `localhost` del ordenador como `10.0.2.2`, que es la dirección que usa la app
(`ClienteApi.URL_BASE`). En un móvil físico hay que cambiarla por la IP del PC en la red local,
también en `res/xml/network_security_config.xml`.

---

## Cómo funciona el login con Google

**La idea.** Lo que usa la app no es OAuth 2.0 «a secas», que sirve para *autorizar* (dar permiso a
una app para usar tus datos de Google), sino **OpenID Connect**, la capa que va encima de OAuth 2.0
para *autenticar*: que Google confirme quién eres. Google hace de notario: le firma a la app un
papel, el **ID token**, que dice «este usuario es fulano@gmail.com», y el backend solo tiene que
comprobar que la firma es de Google.

**El recorrido:**

```
 App Android                     Google                          Backend
     │ 1. «Continuar con Google»    │                                 │
     │ ───────────────────────────> │ 2. ¿la app es de verdad         │
     │   (Credential Manager +      │    GymPulse? (cliente Android)  │
     │    Google Play Services)     │                                 │
     │ <─────────────────────────── │ 3. ID token firmado,            │
     │                              │    aud = cliente Web            │
     │ 4. POST /usuario/login/google { idToken } ───────────────────> │
     │                              │      5. comprueba firma, iss,   │
     │                              │         aud, exp, email         │
     │                              │      6. busca, vincula o crea   │
     │ <─────────────── 7. sesión de GymPulse (JWT + refresco) ────── │
```

1. El usuario pulsa «Continuar con Google» y elige su cuenta en un panel del sistema.
2. Google comprueba que quien pide el token es la app real (nombre de paquete + certificado).
3. Google devuelve un ID token: un JWT firmado con **su** clave privada y dirigido a nuestro
   servidor (campo `aud`).
4. La app se lo manda al backend. Es la única vez que el backend ve algo de Google.
5. El backend lo verifica **sin preguntar a Google**: comprueba la firma con las claves públicas
   que Google publica, y que el token sea de Google (`iss`), para GymPulse (`aud`), no esté
   caducado (`exp`) y tenga el email verificado.
6. Con esa identidad busca al usuario por su id de Google; si no lo encuentra, por email (y
   vincula la cuenta); y si tampoco, crea una nueva.
7. Devuelve **nuestra** sesión de siempre. A partir de aquí Google ya no interviene: su token se
   descarta y la app funciona igual que con el login normal.

**Las piezas en Google Cloud** son solo configuración; no se ejecutan durante el login:

| Pieza | Para qué |
|---|---|
| Proyecto `GymPulse` | El contenedor de todo |
| Pantalla de consentimiento | Cómo se presenta GymPulse al elegir cuenta. En modo prueba solo entran los usuarios de prueba añadidos |
| Cliente **Android** | La ficha de la app: paquete `com.gympulse.android` + huella SHA-1. Responde a «¿quién pide el token?» |
| Cliente **Web** | La ficha del backend. Su ID va en el `aud` del token y el backend lo exige (`gympulse.google.client-id`). Responde a «¿para quién es el token?» |

**A tener en cuenta:**

- Si se vincula una cuenta que nunca verificó su email, se anulan su contraseña y sus sesiones.
  Así nadie puede registrar tu email antes que tú y quedarse dentro cuando entres con Google
  (*pre-hijacking*).
- Las cuentas creadas con Google no tienen contraseña utilizable.
- La huella SHA-1 es la del certificado de depuración de cada ordenador: para desarrollar en otro
  equipo o publicar la app firmada con otro certificado hay que añadir otro cliente Android.

**Dónde está en el código:** `ui/login/InicioSesionGoogle.kt` (pedir el token a Google),
`LoginViewModel` y `UsuarioRepositorio.loginConGoogle` (mandarlo al backend),
`VerificadorTokenGoogle` (verificarlo) y `UsuarioService.loginConGoogle` (buscar, vincular o crear).

---

## Seguridad

| Medida | Cómo |
|---|---|
| Contraseñas | Hash BCrypt; nunca se guardan ni se devuelven en claro. Entre 6 y 72 caracteres, comprobado en el backend aunque la petición no venga de la app |
| Sesiones | JWT de acceso de 15 minutos + token de refresco de 30 días con rotación y detección de reutilización; tokens guardados como hash SHA-256 |
| Verificación del email | Obligatoria para entrar; enlace de un solo uso que caduca en 24 h |
| Fuerza bruta | 5 fallos de login por email o 20 por IP en 15 minutos bloquean el login 15 minutos (429) |
| Enumeración de cuentas | El registro responde siempre igual (el resto va por correo) y el login tarda lo mismo exista o no el email (compara contra un hash ficticio) |
| Aislamiento entre usuarios | El id sale siempre del token; un recurso ajeno responde 404. Los ejercicios predefinidos no se pueden borrar y los personalizados solo los ve su dueño |
| Entrada | Validación de todos los DTOs; el tipo de las imágenes se comprueba por sus bytes, no por lo que diga el cliente |
| Login con Google | Firma, emisor, audiencia, caducidad y email verificado del ID token; defensa contra *pre-hijacking* |
| Privacidad | Los emails se escriben enmascarados en los logs (`a***@gmail.com`) |
| Producción | Solo HTTPS, CORS limitado a los orígenes configurados, sin `show-sql`; secretos en variables de entorno |

---

## Hoja de ruta

**1.0:** backend con PostgreSQL, autenticación con JWT y tokens de refresco,
verificación de email, login con Google, ejercicios predefinidos y personalizados, foto de perfil,
revisión de seguridad completa y app Android conectada a la API.

**Próximas versiones:**

- [ ] Rutinas (necesitan soporte en el backend)
- [ ] Idioma inglés
- [ ] Tema claro
- [ ] Envío real de notificaciones (ahora solo se guarda la preferencia)
- [ ] App iOS (requiere macOS)
