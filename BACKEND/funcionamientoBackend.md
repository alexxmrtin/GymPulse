# GymPulse Backend — Cómo funciona, de arriba abajo

Este documento explica el backend completo: qué hace cada carpeta, qué hace cada clase, y sobre
todo **por dónde pasa una petición desde que entra hasta que sale**. Si solo vas a leer una
sección, que sea [El recorrido de una petición](#el-recorrido-de-una-petición).

---

## 1. Qué es

Una API REST/JSON que guarda entrenamientos de gimnasio. No tiene interfaz: solo recibe
peticiones HTTP y devuelve JSON. Los clientes (la app Android, y algún día iOS o web) consumen
todos los mismos endpoints.

| | |
|---|---|
| Framework | Spring Boot 4.1.0 |
| Lenguaje | Java 21 |
| Build | Maven (sin wrapper; `mvn` del sistema, vía SDKMAN) |
| Base de datos | PostgreSQL 17 (Docker en local) |
| ORM | Hibernate, a través de Spring Data JPA |
| Seguridad | Spring Security + JWT firmado con HS256 |
| JSON | Jackson 3 (ojo: paquete `tools.jackson`, no `com.fasterxml`) |
| Logs | SLF4J + Logback → `logs/gympulse.log` |

### Arrancar

```bash
cd BACKEND
echo "JWT_SECRETO=$(openssl rand -base64 48)" > .env          # solo la primera vez
echo "POSTGRES_USUARIO=gympulse" >> .env
echo "POSTGRES_CONTRASENA=$(openssl rand -hex 16)" >> .env
docker compose up -d                                          # PostgreSQL (o podman compose)
mvn spring-boot:run
```

Queda escuchando en `http://localhost:8080`. La base de datos se consulta con
`docker exec -it gympulse-postgres psql -U gympulse gympulse` o con un cliente gráfico contra
`localhost:5432`.

Otros comandos: `mvn test`, `mvn clean package` (genera el jar en `target/`).

---

## 2. Mapa de carpetas

```
com.gympulse.backend/
├── GymPulseBackendApplication.java   Enciende Spring Boot. Nunca se toca.
│
├── controllers/    Reciben HTTP. Sin lógica: traducen URL → llamada a un servicio.
├── services/       Las reglas del negocio. Deciden SI se hace algo y cómo.
├── repositories/   Interfaces que Spring convierte en consultas SQL.
├── models/         Entidades JPA. Son las tablas de la base de datos.
├── dtos/           Records con la forma del JSON que entra y sale.
├── helpers/        Utilidades sin estado: mapeo, id del token, tipo de imagen.
├── core/           Configuración: seguridad, JWT, CORS y manejo de errores.
└── middlewares/    Vacía (queda del esqueleto inicial).
```

La regla que ordena todo esto: **cada capa solo habla con la de debajo**.

```
Controlador  →  Servicio  →  Repositorio  →  Base de datos
```

Un controlador nunca toca un repositorio. Un repositorio no sabe que existe HTTP. Y las
entidades de `models/` **nunca salen** por la API: se convierten a DTO antes.

---

## 3. Qué pasa al arrancar

1. `main()` llama a `SpringApplication.run(...)`.
2. `@SpringBootApplication` escanea este paquete y los de debajo buscando clases anotadas
   (`@RestController`, `@Service`, `@Component`, `@Configuration`) y las instancia. Por eso en
   este proyecto casi no verás `new`: los objetos los crea y los reparte Spring.
3. Lee `application.properties`, y con él importa el `.env`. Si falta `JWT_SECRETO`,
   **la aplicación no arranca** (ver [Configuración](#8-configuración)).
4. Hibernate lee las clases de `models/` y crea o actualiza las tablas en PostgreSQL
   (`ddl-auto=update`): añade las tablas y columnas que falten, sin borrar datos.
5. Se monta la cadena de filtros de seguridad definida en `core/SeguridadConfig.java`.
6. Arranca el Tomcat embebido en el puerto 8080.

---

## El recorrido de una petición

Esta es la parte importante. Dos ejemplos cubren todo lo demás.

### A) Login — `POST /usuario/login` (público)

```
{"email":"alex@mail.com","password":"secreta123"}
   │
   ▼
FILTROS DE SEGURIDAD ....................... core/SeguridadConfig.java
   └─ esta ruta es permitAll() → pasa sin token
   │
   ▼
UsuarioController.login .................... controllers/UsuarioController.java
   ├─ @RequestBody convierte el JSON en un LoginDTO
   └─ @Valid comprueba sus anotaciones (@NotBlank)  → 400 si faltan
   │
   ▼
UsuarioService.autenticar .................. services/UsuarioService.java
   ├─ LimiteIntentosService.comprobar(email, ip)     → 429 si está bloqueado
   ├─ usuarioRepository.findByEmail(...)             → 401 si no existe
   ├─ passwordEncoder.matches(password, hash)        → 401 si no cuadra
   │
   ▼
TokenService.generar ....................... services/TokenService.java
   ├─ construye los claims: iss, sub (id), iat, exp, username
   └─ jwtEncoder.encode(...)  ← firma con la clave de core/JwtConfig.java
   │
   ▼
SesionDTO {token, tipo, expiraEnSegundos, usuario}
   │
   ▼
Jackson lo convierte en JSON → 200 OK
```

### B) Una petición protegida — `GET /entrenamiento`

```
GET /entrenamiento
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
   │
   ▼
CADENA DE FILTROS .......................... core/SeguridadConfig.java
   ├─ CORS
   ├─ lee la cabecera Authorization
   │    └─ jwtDecoder valida firma y caducidad ..... core/JwtConfig.java
   │         └─ si falla → 401 ................. ManejoErrores/RespuestaNoAutenticado.java
   ├─ ¿esta ruta exige autenticación? sí. ¿hay usuario? sí → sigue
   │    └─ si no lo hubiera → 401 (mismo JSON)
   │
   ▼
EntrenamientoController.buscarPropios ...... controllers/EntrenamientoController.java
   └─ @AuthenticationPrincipal Jwt token
        └─ UsuarioAutenticado.id(token) → Long ..... helpers/UsuarioAutenticado.java
   │
   ▼
EntrenamientoService.buscarPropios(usuarioId)  services/EntrenamientoService.java
   └─ @Transactional(readOnly = true)  ← abre la sesión con la BD
   │
   ▼
EntrenamientoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)
   └─ Hibernate genera el SELECT → devuelve entidades Entrenamiento
   │
   ▼
Mapeador.aDTO(entrenamiento) ............... helpers/Mapeador.java
   └─ recorre las series DENTRO de la transacción (son LAZY)
   │
   ▼
List<EntrenamientoDTO> → Jackson → JSON → 200 OK
```

Si en cualquier punto a partir del controlador se lanza una excepción, la recoge
`ManejoErrores/ManejadorErrores.java` y la convierte en el JSON de error correspondiente.

**La idea clave de la seguridad:** el id del usuario sale del token, nunca de la URL ni del
cuerpo. Por eso `GET /entrenamiento` no lleva `?usuarioId=`: nadie puede pedir los datos de otro
cambiando un número.

---

## 4. El modelo de datos

```
usuarios
  └──< entrenamientos
             └──< series >── ejercicios
```

Un usuario tiene muchos entrenamientos. Un entrenamiento tiene muchas series. Cada serie apunta
a un ejercicio y guarda las repeticiones y el peso de esa serie concreta.

| Entidad | Tabla | Campos |
|---|---|---|
| `Usuario` | `usuarios` | id, username (único), email (único), password (hash), peso, altura, fechaNacimiento |
| `Ejercicio` | `ejercicios` | id, nombre, descripcion, grupoMuscular |
| `Entrenamiento` | `entrenamientos` | id, usuario_id (FK), nombre, fecha |
| `Serie` | `series` | id, entrenamiento_id (FK), ejercicio_id (FK), repeticiones, peso |

El detalle de columnas y restricciones está en [BaseDeDatos.md](BaseDeDatos.md).

### Las relaciones y sus consecuencias

**`@ManyToOne` carga el objeto de golpe (EAGER por defecto).** Cuando tienes una `Serie`, su
`Ejercicio` y su `Entrenamiento` ya vienen con ella. Por eso `Mapeador` puede llamar a
`serie.getEjercicio().getNombre()` sin problemas.

**`@OneToMany` es perezoso (LAZY por defecto).** `entrenamiento.getSeries()` no es una lista: es
un proxy que va a la BD cuando alguien lo recorre. Si lo recorres fuera de la transacción, la
sesión ya está cerrada y salta `LazyInitializationException`. **Ese es el motivo de los
`@Transactional` en los servicios**: el mapeo a DTO tiene que ocurrir con la sesión abierta.

**Las cascadas deciden qué se lleva por delante un borrado:**

| Relación | Cascada | Efecto |
|---|---|---|
| `Usuario` → entrenamientos | `ALL` | Borrar un usuario borra sus entrenamientos |
| `Entrenamiento` → series | `ALL` | Borrar un entrenamiento borra sus series |
| `Ejercicio` → series | **ninguna** | Borrar un ejercicio en uso falla → 409 |

Lo de `Ejercicio` es intencionado: borrar "Press banca" del catálogo no debe borrar el historial
de quien lo haya hecho.

### El ciclo que obliga a usar DTOs

`Usuario` tiene una lista de `Entrenamiento`, y cada `Entrenamiento` tiene un `Usuario`. Si
devolvieras la entidad directamente, Jackson entraría en bucle infinito: usuario → entrenamientos
→ usuario → … hasta el `StackOverflowError`. Los DTOs cortan ese ciclo.

---

## 5. Repositorios

Una interfaz que extiende `JpaRepository<Entidad, TipoDelId>` y no implementa nada. Spring genera
la implementación al arrancar. De serie ya tienes `save`, `findById`, `findAll`, `deleteById`,
`existsById`, `count`.

Para lo demás basta con **declarar un método con el nombre adecuado**: Spring lee el nombre y
escribe el SQL.

| Repositorio | Método propio | SQL que genera |
|---|---|---|
| `UsuarioRepository` | `findByEmail` | `WHERE email = ?` |
| | `findByUsername` | `WHERE username = ?` |
| | `existsByEmail` | `SELECT COUNT(*) WHERE email = ?` |
| `EjercicioRepository` | `findByGrupoMuscular` | `WHERE grupo_muscular = ?` |
| | `findByNombreContainingIgnoreCase` | `WHERE UPPER(nombre) LIKE %?%` |
| `EntrenamientoRepository` | `findByUsuarioIdOrderByFechaDesc` | `WHERE usuario_id = ? ORDER BY fecha DESC` |
| `SerieRepository` | `findByEntrenamientoId` | `WHERE entrenamiento_id = ?` |
| | `findByEjercicioIdAndEntrenamientoUsuarioId` | Une con entrenamientos: `WHERE ejercicio_id = ? AND e.usuario_id = ?` |

Ese último enseña bien el truco: `EntrenamientoUsuarioId` significa "atraviesa la relación
`entrenamiento`, luego su `usuario`, y compara su `id`". Así se filtran las series de un ejercicio
para quedarse solo con las del usuario que pregunta.

---

## 6. DTOs y Mapeador

Un **DTO** (Data Transfer Object) es un objeto cuya única función es viajar por la API. No tiene
lógica ni sabe de la base de datos: define **la forma del JSON**.

Existen por tres motivos: cortan el ciclo infinito, evitan el problema de las colecciones LAZY, y
—lo más importante— controlan qué entra y qué sale.

Están escritos como `record`, que en Java 21 te da constructor, getters (`dto.id()`, sin `get`),
`equals` y `toString` en una línea, con los campos inmutables.

### Catálogo

| DTO | Dirección | Para qué |
|---|---|---|
| `RegistroUsuarioDTO` | entra | Alta: username, email, password, peso, altura, fechaNacimiento |
| `LoginDTO` | entra | email + password |
| `ActualizarUsuarioDTO` | entra | Solo peso, altura y fechaNacimiento |
| `UsuarioDTO` | sale | Los datos del usuario **sin la contraseña** |
| `SesionDTO` | sale | token, tipo, expiraEnSegundos y el `UsuarioDTO` |
| `CrearEjercicioDTO` / `EjercicioDTO` | entra / sale | Catálogo de ejercicios |
| `CrearEntrenamientoDTO` | entra | nombre y fecha. **No lleva usuarioId**: sale del token |
| `EntrenamientoDTO` | sale | id, usuarioId, nombre, fecha y la lista de series |
| `CrearSerieDTO` | entra | entrenamientoId, ejercicioId, repeticiones, peso |
| `SerieDTO` | sale | Añade `ejercicioNombre` ya resuelto |

Fíjate en dos decisiones:

- **Hay un DTO distinto para entrar y para salir.** `RegistroUsuarioDTO` no tiene `id`, así que
  aunque un cliente lo mande se ignora; y `ActualizarUsuarioDTO` no tiene `email`, así que un PUT
  no puede cambiarlo. Sin DTOs, con la entidad como `@RequestBody`, alguien podría mandar
  `{"id": 3}` y como `save()` con id hace UPDATE, estaría editando la cuenta de otro.
- **`SerieDTO` aplana la relación.** En la BD la serie solo guarda `ejercicio_id`; el DTO añade el
  nombre para que la app pinte la lista sin una petición extra por cada serie.

### `helpers/Mapeador.java`

El único sitio donde se decide qué campos de la BD acaban en el JSON. Solo traduce en un sentido,
**entidad → DTO**. La vuelta (DTO → entidad) vive en los servicios, porque hace falta ir a la BD
a resolver los ids sueltos y comprobar que existen, y eso requiere repositorios.

Todos los métodos se llaman `aDTO`: es sobrecarga, el compilador elige según el tipo. No hay
versión para listas porque `List<Usuario>` y `List<Serie>` son el mismo tipo en tiempo de
ejecución (*type erasure*) y chocarían; por eso los servicios hacen
`.stream().map(Mapeador::aDTO).toList()`.

### Validación de la entrada (`spring-boot-starter-validation`)

Las reglas de los datos que entran van **anotadas en los propios DTOs**, con Bean Validation:

| DTO | Reglas |
|---|---|
| `RegistroUsuarioDTO` | `username` obligatorio (máx. 30), `email` con formato válido, `password` de 6 a 72 caracteres, peso y altura `@Positive`, fecha de nacimiento `@Past` |
| `ActualizarUsuarioDTO` | Peso y altura `@Positive`, fecha de nacimiento `@Past` |
| `LoginDTO` | Email y contraseña `@NotBlank` |
| `CrearEjercicioDTO` | Nombre obligatorio (máx. 60), descripción máx. 255 |
| `CrearEntrenamientoDTO` | Fecha `@NotNull`, nombre máx. 60 |
| `CrearSerieDTO` | Ids obligatorios, repeticiones `@Min(1)`, peso `@PositiveOrZero` |

El controlador pone `@Valid` delante de `@RequestBody`. Spring valida **antes de entrar en el
método**, y si algo falla lanza `MethodArgumentNotValidException`. `ManejadorErrores` la convierte
en un 400 con **todos** los errores juntos: `"email: no es un email valido; password: debe tener
entre 6 y 72 caracteres"`. Así los servicios solo contienen reglas de negocio (duplicados,
propiedad…).

La contraseña tiene máximo 72 porque BCrypt ignora lo que pase de 72 bytes. La app comprueba lo
mismo para avisar antes, pero la validación que cuenta es la del backend: la API la puede llamar
cualquiera, no solo la app.

---

## 7. Servicios: dónde viven las reglas

### `UsuarioService`

| Método | Qué comprueba |
|---|---|
| `registrar` | Campos obligatorios → 400. Email ya usado → 409. Username ya usado → 409. **Cifra la contraseña con BCrypt** antes de guardar |
| `autenticar` | Pasa por el límite de intentos, busca por email y compara con `passwordEncoder.matches`. Cualquier fallo → 401 con el mismo mensaje (no distingue "no existe" de "contraseña mala"). Si el email no existe, **compara igualmente contra un hash ficticio** generado al arrancar: sin eso, esa respuesta sería unos 80 ms más rápida y midiendo tiempos se sabría qué emails están registrados. Si cuadra, emite el JWT y el token de refresco |
| `buscarPorId` / `actualizar` / `eliminar` | `exigirQueSeaElMismo(id, idDelToken)` → **403 si intentas tocar la cuenta de otro** |

Sobre BCrypt: la contraseña **no se cifra, se hashea**. No hay forma de recuperarla. Al hacer
login no se descifra nada: se vuelve a hashear lo que llega y se compara. BCrypt además incorpora
una sal aleatoria en cada hash, así que dos usuarios con la misma contraseña tienen hashes
distintos.

### `TokenService`

Construye y firma el JWT. Los *claims* que mete:

| Claim | Valor | Estándar |
|---|---|---|
| `iss` | `"gympulse"` | sí, el emisor |
| `sub` | El id del usuario, como texto | sí, el sujeto |
| `iat` | Cuándo se emitió | sí |
| `exp` | Cuándo caduca | sí |
| `username` | El nombre de usuario | no, propio |

**Nunca metas nada secreto en un JWT**: no está cifrado, solo firmado, y cualquiera puede leer su
contenido con un Base64.

### `VerificadorTokenGoogle` y el login con Google

La app obtiene de Google un **ID token** (un JWT firmado por Google con RS256) y lo manda a
`POST /usuario/login/google`. `VerificadorTokenGoogle` lo comprueba con un `NimbusJwtDecoder`
apuntado al JWK Set de Google (`https://www.googleapis.com/oauth2/v3/certs`, que Nimbus cachea y
renueva cuando Google rota claves) y estos validadores: caducidad, `iss` de Google, `aud` igual a
`gympulse.google.client-id` (sin esto valdría un token emitido para otra app) y `email_verified`.
No es un bean `JwtDecoder` para no chocar con el que valida nuestros propios tokens.

`UsuarioService.loginConGoogle` sigue tres caminos: usuario con ese `google_id` → entra; usuario con
ese email → se vincula (si no había verificado el email, se anula su contraseña y se revocan sus
sesiones, contra el *pre-hijacking*); ninguno → se crea ya verificado, con un `username` sacado del
nombre de Google y una contraseña aleatoria que nadie conoce (la columna `password` es
`NOT NULL`; se decidió así cuando el esquema aún lo generaba Hibernate). Devuelve la misma `SesionDTO` que el login normal.

Tests: `VerificadorTokenGoogleTest` firma tokens con una clave RSA propia para probar cada
rechazo (otra app, otro emisor, otra clave, caducado, email sin verificar) y `LoginGoogleTests`
simula el verificador para probar crear, volver a entrar y vincular.

### `VerificacionEmailService` y `CorreoService`

El registro deja la cuenta **sin verificar** (`emailVerificadoEn` vacío) y genera un token
aleatorio del que solo se guarda el hash en `usuarios.verificacion_hash`, con caducidad de 24 h.
El enlace `GET /usuario/verificar?token=...` marca el email como verificado y borra el token, así
que solo sirve una vez. Mientras no se verifique, el login responde 403 (solo después de comprobar
la contraseña, para no dar esa información a quien no la sabe).

Para que el registro no delate qué emails existen, **responde siempre 202 con el mismo mensaje**:
si el email ya estaba, no crea nada y manda al dueño un correo de aviso, y además hace el hash de
BCrypt igualmente para que tarde lo mismo. Reenviar el correo también responde siempre igual y
tiene una espera de un minuto entre envíos.

`CorreoService` envía con `@Async` (otro hilo, gracias a `@EnableAsync` en `core/AsincroniaConfig`),
así que el correo nunca retrasa la respuesta. Si no hay `spring.mail.host` configurado, no existe
`JavaMailSender` y el correo se **escribe en el log** con el enlace: es lo que pasa en local. Con
`gympulse.registro.verificar-email=false` (lo usan los tests) las cuentas nacen verificadas.

### `LimiteIntentosService`

Frena la fuerza bruta en el login. Lleva en memoria (`ConcurrentHashMap`) dos contadores de
fallos: uno por email, en minúsculas, y otro por IP. Al llegar al máximo dentro de la ventana,
esa clave queda bloqueada y `UsuarioService.autenticar` lanza `DemasiadosIntentosException` (429
con `Retry-After`) **antes** de comprobar la contraseña. Un login correcto borra el contador del
email. Los emails que no existen cuentan igual, para no delatar cuáles están registrados.
Recibe un `Clock` para poder probar la caducidad sin esperar (`LimiteIntentosServiceTest`).

### `TokenRefrescoService`

Emite, rota y revoca los tokens de refresco (tabla `tokens_refresco`):

- **Emitir:** genera 32 bytes aleatorios con `SecureRandom`, los devuelve en Base64 y guarda solo
  su **hash SHA-256**. Si alguien se lleva la BD, no puede usar los tokens. De paso borra los ya
  caducados de ese usuario.
- **Rotar:** busca el token por su hash. Si está revocado, es una **reutilización**: alguien está
  usando un token que ya se cambió, así que se revocan todas las sesiones del usuario y se responde
  401. Si no, lo marca como revocado y emite uno nuevo. Usa `noRollbackFor` para que la revocación
  masiva se guarde aunque la petición acabe en error.
- **Revocar:** el logout. No falla si el token no existe.
- **Borrar todos:** al eliminar la cuenta, antes de borrar el usuario, por la clave foránea.

### `EjercicioService`

El catálogo de ejercicios es **compartido**: cualquier usuario autenticado puede listarlo, crear
y borrar. No hay dueño. `eliminar` comprueba que exista (404) y, si el ejercicio tiene series
asociadas, la BD rechaza el borrado y sale un 409.

### `EntrenamientoService`

El corazón de la separación por usuario. `obtenerPropio(id, usuarioId)` es el método por el que
pasan todos los accesos a un entrenamiento concreto, y **es público a propósito** porque
`SerieService` también lo usa.

Si el entrenamiento existe pero es de otro usuario, devuelve **404 y no 403**. Es deliberado: un
403 confirmaría que ese id existe, y eso ya es información sobre datos ajenos.

`crear` recibe el `usuarioId` del token, no del cuerpo de la petición.

### `SerieService`

No habla con `EntrenamientoRepository` directamente: llama a `entrenamientoService.obtenerPropio`
para que la comprobación de propiedad esté escrita **una sola vez**. Todas sus operaciones
(crear, listar por entrenamiento, borrar) pasan por ahí. `buscarPorEjercicio` es la excepción:
usa la consulta filtrada del repositorio, que ya limita por usuario.

---

## 8. Los controladores y los endpoints

Un controlador solo hace tres cosas: recibir, delegar y devolver. Si ves lógica en uno, está en
el sitio equivocado.

- `@RestController` — todo lo que devuelvan los métodos se convierte en JSON.
- `@RequestMapping("/usuario")` — prefijo común de la clase.
- `@RequestBody` — convierte el JSON de entrada en un DTO.
- `@PathVariable` — extrae el `{id}` de la URL.
- `@RequestParam` — lee `?nombre=press`.
- `@AuthenticationPrincipal Jwt token` — inyecta el token **ya validado**.
- `ResponseEntity` — permite elegir el código HTTP.

### Tabla completa

| Método | Ruta | Token | Cuerpo | Devuelve |
|---|---|---|---|---|
| POST | `/usuario` | **no** | `RegistroUsuarioDTO` | 202 + `MensajeDTO`, siempre el mismo |
| GET | `/usuario/verificar?token=` | **no** | — | Página HTML: verificado (200) o enlace no válido (400) |
| POST | `/usuario/verificacion/reenviar` | **no** | `ReenviarVerificacionDTO` | 202 + `MensajeDTO`, siempre el mismo |
| POST | `/usuario/login` | **no** | `LoginDTO` | `SesionDTO` (acceso + refresco) |
| POST | `/usuario/login/google` | **no** | `LoginGoogleDTO` | `SesionDTO`; crea o vincula la cuenta |
| POST | `/usuario/refresco` | **no** | `RefrescoDTO` | `SesionDTO` nueva; el token usado deja de valer |
| POST | `/usuario/logout` | **no** | `RefrescoDTO` | 204 |
| GET | `/usuario/{id}` | sí, propio | — | `UsuarioDTO` |
| PUT | `/usuario/{id}` | sí, propio | `ActualizarUsuarioDTO` | `UsuarioDTO` |
| DELETE | `/usuario/{id}` | sí, propio | — | 204 |
| PUT | `/usuario/{id}/foto` | sí, propio | multipart, campo `foto` | `UsuarioDTO` |
| GET | `/usuario/{id}/foto` | sí, propio | — | La imagen, o 404 |
| DELETE | `/usuario/{id}/foto` | sí, propio | — | 204 |
| POST | `/ejercicio` | sí | `CrearEjercicioDTO` | `EjercicioDTO` personalizado, a nombre del usuario |
| GET | `/ejercicio` | sí | — | Predefinidos + propios. Acepta `?nombre=` o `?grupoMuscular=` |
| GET | `/ejercicio/{id}` | sí, predefinido o propio | — | `EjercicioDTO` (con `predefinido`) |
| DELETE | `/ejercicio/{id}` | sí, propio | — | 204; 403 si es predefinido, 409 si tiene series |

Los ejercicios con `usuario_id` vacío son los **predefinidos**: los crea `core/CatalogoPredefinido`
al arrancar, solo si no existen, y nadie puede borrarlos. Los de otros usuarios responden 404,
también al intentar usarlos en una serie.
| POST | `/entrenamiento` | sí | `CrearEntrenamientoDTO` | `EntrenamientoDTO` |
| GET | `/entrenamiento` | sí | — | Mis entrenamientos, por fecha descendente |
| GET | `/entrenamiento/{id}` | sí, propio | — | `EntrenamientoDTO` con sus series |
| DELETE | `/entrenamiento/{id}` | sí, propio | — | 204 |
| POST | `/serie` | sí, entreno propio | `CrearSerieDTO` | `SerieDTO` |
| GET | `/serie?entrenamientoId=` | sí, propio | — | Lista de `SerieDTO` |
| GET | `/serie?ejercicioId=` | sí | — | **Mis** series de ese ejercicio |
| DELETE | `/serie/{id}` | sí, propia | — | 204 |

`GET /serie` sin ningún parámetro devuelve 400.

Nota: los POST devuelven **200**, no 201. Es una decisión pendiente de revisar.

---

## 9. Seguridad

### La cadena de filtros — `core/SeguridadConfig.java`

En cuanto Spring Security está en el classpath, se coloca una cadena de filtros **delante** del
DispatcherServlet. Toda petición los atraviesa antes de que exista tu código. Eso explica por qué
hay cosas que no puede resolver `ManejadorErrores`.

```java
.csrf(csrf -> csrf.disable())
```
CSRF protege de que otra web haga peticiones en tu nombre **usando la cookie que el navegador
manda sola**. Esta API no usa cookies; el token lo pone el cliente a mano. En una API REST se
desactiva; en una web con formularios y sesión, nunca.

```java
.cors(Customizer.withDefaults())
```
Hace que Security respete la configuración de `CorsConfig`. Sin esto, el `OPTIONS` de preflight
—que el navegador manda **sin credenciales**— chocaría contra los filtros antes de llegar a MVC.

```java
.sessionManagement(... STATELESS)
```
Prohíbe crear sesiones en el servidor. Ésta es la diferencia de fondo entre sesión y token: con
sesión el servidor **recuerda** quién eres; con JWT no recuerda nada y el token **contiene** quién
eres, firmado. Por eso puedes reiniciar el backend sin desloguear a nadie.

```java
.httpBasic(disable).formLogin(disable)
```
Quita la ventanita del navegador y la página de login que Spring monta sola. No las queremos:
autenticamos con `POST /usuario/login`.

```java
.authorizeHttpRequests(...)
    .requestMatchers(POST, "/usuario", "/usuario/login", "/usuario/login/google",
                     "/usuario/refresco", "/usuario/logout", "/usuario/verificacion/reenviar").permitAll()
    .requestMatchers(GET, "/usuario/verificar").permitAll()
    .anyRequest().authenticated()
```
Aquí se decide qué está abierto. **El orden importa**: gana la primera regla que encaje.

```java
.oauth2ResourceServer(servidor -> servidor.jwt(...))
```
Esta línea mete el filtro que lee `Authorization: Bearer ...`, llama al `JwtDecoder` y deja al
usuario identificado. **No hay ningún filtro escrito a mano en este proyecto, y no hace falta
añadirlo**: la mayoría de tutoriales escriben un `OncePerRequestFilter` de 60 líneas que aquí ya
viene resuelto.

### Las claves — `core/JwtConfig.java`

Tres beans:

- `claveJwt` — convierte el texto de `JWT_SECRETO` en una `SecretKey` de tipo `HmacSHA256`. Exige
  32 caracteres mínimo porque HS256 necesita 256 bits.
- `jwtEncoder` — firma tokens.
- `jwtDecoder` — los verifica, **con la misma clave**. En eso consiste que sea simétrica.

### Anatomía de un token

Tres trozos separados por puntos, en Base64:

```
{"alg":"HS256"}                                                  ← cabecera
{"iss":"gympulse","sub":"1","exp":1786333992,"iat":1786290792,   ← contenido
 "username":"alex"}
hRWJDBDBilkuTlvHQJ5E...                                          ← firma
```

La firma es `HMAC-SHA256(cabecera + contenido, secreto)`. Si alguien edita el contenido para poner
`"sub":"2"` y hacerse pasar por otro, la firma deja de cuadrar y el token se rechaza. Como el
secreto no sale nunca del servidor, no se puede fabricar una firma válida.

Para validar un token el servidor **no consulta la base de datos**: solo recalcula la firma y mira
la fecha de caducidad.

### Las dos capas de protección

Es importante ver que son cosas distintas:

1. **Autenticación** (la cadena de filtros): ¿tienes un token válido? Si no → 401.
2. **Propiedad** (los servicios): ¿este dato es tuyo? Si no → 403 o 404.

La primera sola no bastaría: sin la segunda, cualquier usuario registrado podría leer los datos de
todos los demás simplemente cambiando un id.

---

## 10. Manejo de errores

Todos los errores de la API tienen la misma forma, venga de donde venga:

```json
{ "timestamp": "2026-08-09T18:07:10.254", "codigo": 404, "mensaje": "Entrenamiento no encontrado" }
```

Pero se generan en **dos etapas distintas**, y por eso hay tres clases:

| Clase | Cuándo actúa |
|---|---|
| `ManejadorErrores` | Excepciones lanzadas desde un controlador o servicio |
| `RespuestaNoAutenticado` | Fallos de token, en la cadena de filtros (401) |
| `RespuestaAccesoDenegado` | Permisos denegados en la cadena de filtros (403) |

Las dos últimas escriben el JSON a mano porque cuando saltan **todavía no hay controlador** al que
`@RestControllerAdvice` pueda aconsejar.

### Tabla de códigos

| Código | Excepción | Cuándo |
|---|---|---|
| 400 | `MethodArgumentNotValidException` | Algún campo no cumple las anotaciones del DTO; lista todos |
| 400 | `HttpMessageNotReadableException` | El cuerpo no es un JSON válido o un campo tiene un tipo incorrecto |
| 400 | `IllegalArgumentException` | La imagen subida no es JPEG, PNG ni WebP |
| 401 | `CredencialesInvalidasException` | Email o contraseña incorrectos |
| 401 | (filtro) | Token ausente, caducado o manipulado |
| 403 | `AccesoProhibidoException` | Intentas operar sobre la cuenta de otro |
| 404 | `RecursoNoEncontradoException` | No existe, **o es de otro usuario** |
| 409 | `ConflictoException` | Email o username duplicados |
| 409 | `DataIntegrityViolationException` | Borrar algo que aún está referenciado |
| 413 | `MaxUploadSizeExceededException` | La foto supera los 5 MB |
| 429 | `DemasiadosIntentosException` | Login bloqueado por demasiados fallos; añade `Retry-After` |
| 500 | `RuntimeException` | Cualquier otra cosa |

Para añadir un error nuevo: crea la clase en `exceptions/` extendiendo `RuntimeException`, añade
su `@ExceptionHandler` en `ManejadorErrores`, y lánzala desde el servicio.

Detalle: los `@ExceptionHandler` se eligen del más específico al más general, por eso el de
`RuntimeException` no se come a los demás.

---

## 11. Configuración

### `application.properties`

| Propiedad | Qué hace |
|---|---|
| `spring.config.import=optional:file:.env[.properties]` | Carga el `.env` como propiedades. `optional:` = si no existe, arranca igual (en producción las variables vienen del entorno). La sintaxis `[.properties]` le dice el formato, porque el archivo no tiene extensión |
| `spring.datasource.url=jdbc:postgresql://localhost:5432/gympulse` | PostgreSQL local del `docker-compose.yml`; usuario y contraseña salen del `.env` (`POSTGRES_USUARIO`, `POSTGRES_CONTRASENA`) |
| `spring.jpa.hibernate.ddl-auto=update` | Hibernate crea y actualiza las tablas según las entidades, sin borrar datos. Solo añade: renombrar o quitar columnas hay que hacerlo a mano |
| `gympulse.jwt.secreto=${JWT_SECRETO}` | **Sin valor por defecto a propósito**: si falta, la app no arranca |
| `gympulse.jwt.duracion-minutos=15` | Vida del token de acceso |
| `gympulse.jwt.refresco-dias=30` | Vida del token de refresco |
| `spring.servlet.multipart.max-file-size=5MB` | Tamaño máximo de la foto de perfil |
| `gympulse.login.max-fallos-email=5` / `max-fallos-ip=20` | Fallos de login que provocan el bloqueo, por email y por IP |
| `gympulse.login.ventana-minutos=15` / `bloqueo-minutos=15` | En cuánto tiempo se cuentan los fallos y cuánto dura el bloqueo |
| `gympulse.google.client-id=...` | ID del cliente Web de Google: el `aud` que deben llevar los ID tokens |
| `gympulse.registro.verificar-email=true` | Exigir verificar el email antes de poder entrar |
| `gympulse.registro.verificacion-horas=24` | Caducidad del enlace de verificación |
| `gympulse.url-publica=http://localhost:8080` | Base de los enlaces que van en los correos |

### Perfil de producción (`application-prod.properties`)

Se activa con `SPRING_PROFILES_ACTIVE=prod` y se suma a la configuración normal. Apaga `show-sql`, baja los logs a INFO, activa
`gympulse.https-obligatorio` (`http.redirectToHttps`) con `server.forward-headers-strategy` para
funcionar detrás de un proxy que termine TLS, limita CORS a `GYMPULSE_CORS_ORIGENES` y toma la base de datos (`DATABASE_URL`,
`DATABASE_USUARIO`, `DATABASE_CONTRASENA`) y el SMTP de variables de entorno. Si falta alguna, la app no arranca. `PerfilProduccionTests`
comprueba la redirección a HTTPS, que sin token responde 401 y el CORS.
| `spring.jpa.show-sql=true` | Imprime el SQL que genera Hibernate |
| `logging.level.com.gympulse=DEBUG` | Detalle en nuestro código, INFO en el resto |

### Secretos

`BACKEND/.env` está **ignorado por git** y contiene `JWT_SECRETO`. `.env.example` sí está en git,
con el nombre de la variable y vacío el valor.

Que no haya valor por defecto es deliberado: con un fallback commiteado, podrías desplegar sin
definir la variable y la app funcionaría con una clave que está publicada en el repositorio —
cualquiera podría firmarse un token como el usuario que quisiera. Fallar al arrancar es mejor que
funcionar con seguridad falsa.

Generar uno nuevo: `openssl rand -base64 48`. Si lo cambias, todos los tokens emitidos dejan de
valer al instante.

### Tests

`src/test/resources/application.properties` **sustituye entera** a la de `main` cuando corres
`mvn test`, porque se llaman igual. Usa **H2 en memoria en modo PostgreSQL** y un secreto ficticio:
los tests no necesitan Docker ni el `.env`. Las tablas las crea Hibernate al empezar y las borra
al terminar (`ddl-auto=create-drop`), así que cada ejecución arranca limpia.

---

## 12. Probarlo a mano

```bash
# 1. Registro
curl -s -X POST localhost:8080/usuario -H 'Content-Type: application/json' \
  -d '{"username":"alex","email":"alex@mail.com","password":"secreta123","peso":78.5}'

# 2. Login: guarda el token
TOKEN=$(curl -s -X POST localhost:8080/usuario/login -H 'Content-Type: application/json' \
  -d '{"email":"alex@mail.com","password":"secreta123"}' \
  | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

# 3. A partir de aquí, todo lleva el token
curl -s -X POST localhost:8080/ejercicio -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"nombre":"Press banca","grupoMuscular":"pecho"}'

curl -s -X POST localhost:8080/entrenamiento -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"nombre":"Pecho","fecha":"2026-08-09"}'

curl -s -X POST localhost:8080/serie -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"entrenamientoId":1,"ejercicioId":1,"repeticiones":10,"peso":60.0}'

curl -s localhost:8080/entrenamiento -H "Authorization: Bearer $TOKEN"

# Sin token → 401
curl -s localhost:8080/entrenamiento
```

Para ver el contenido de un token:
`echo $TOKEN | cut -d. -f2 | base64 -d`

---

## 13. Trampas conocidas de este proyecto

- **Spring Boot 4 usa Jackson 3.** El paquete es `tools.jackson.databind`, no
  `com.fasterxml.jackson.databind`. Casi todo el código que encuentres por internet será de
  Jackson 2 y no compilará.
- **`ddl-auto=update` solo añade.** Nunca quita un `NOT NULL`, ni renombra, ni borra columnas: si
  cambias una entidad de esa forma, la tabla se queda como estaba y hay que retocarla a mano.
- **`byte[]` grandes y PostgreSQL.** Si a un `byte[]` se le pone un `length` muy grande, Hibernate
  lo trata como `BLOB`, que en PostgreSQL es un `oid` (los bytes van fuera de la tabla y no se
  borran solos). Por eso `FotoPerfil.datos` lleva `@JdbcTypeCode(SqlTypes.VARBINARY)` → `bytea`.
- **`mvn spring-boot:run` no recompila.** DevTools solo reinicia cuando algo actualiza
  `target/classes`; si editas y no compilas, sigues ejecutando el código viejo.
- **Al tocar el `pom.xml`, recarga el proyecto en VS Code** (`Java: Reload Projects`). Si no, la
  extensión marca en rojo imports que compilan perfectamente. Cuando dudes, manda `mvn compile`.
- **En el log aparece `Using generated security password: ...`** Es el usuario en memoria que
  Spring crea por defecto. No sirve para entrar por ningún sitio, porque basic y form login están
  desactivados.

---

## 14. Estado y pendientes

Hecho: CRUD completo de las cuatro entidades, DTOs, contraseñas hasheadas, manejo de errores
uniforme, autenticación con JWT y API cerrada con separación por usuario, tokens de refresco con
rotación y foto de perfil.

Pendiente:

- **Devolver 201 en los POST** en lugar de 200.
- **Más tests.** Hay tests de integración de la foto de perfil (`FotoPerfilTests`) y de la sesión
  (`SesionTests`); faltan los de ejercicios, entrenamientos y series.
- Limpieza menor: `EntrenamientoRepository.findByUsuarioId` y `SerieRepository.findByEjercicioId`
  solo aparecen en su declaración, no los llama nadie. La carpeta `middlewares/` únicamente tiene
  un `.gitkeep`.
