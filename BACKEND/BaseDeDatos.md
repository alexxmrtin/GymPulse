# GymPulse — Estructura de la base de datos

PostgreSQL 17. Las tablas las crea **Hibernate** a partir de las entidades de `models/` al arrancar
el backend (`ddl-auto=update`): añade lo que falte sin borrar datos.

## Diagrama de relaciones

```
usuarios ──── fotos_perfil            (1 a 1, opcional)
  ├──< tokens_refresco                (una fila por sesión abierta)
  ├──< ejercicios                     (los personalizados; los predefinidos tienen usuario_id vacío)
  └──< entrenamientos
             └──< series >── ejercicios
```

---

## Tablas

### `usuarios`

| Columna | Tipo | Restricciones |
|---|---|---|
| id | BIGINT | PK, identidad |
| username | VARCHAR(255) | NOT NULL, UNIQUE |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL, hash BCrypt (aleatorio si la cuenta se creó con Google) |
| peso | DOUBLE PRECISION | |
| altura | DOUBLE PRECISION | en cm |
| fecha_nacimiento | DATE | |
| foto_actualizada_en | TIMESTAMPTZ | vacío si no hay foto |
| email_verificado_en | TIMESTAMPTZ | vacío = cuenta sin verificar (no puede entrar) |
| google_id | VARCHAR(255) | UNIQUE, el `sub` del ID token de Google |
| verificacion_hash | VARCHAR(64) | UNIQUE, SHA-256 del token del enlace de verificación |
| verificacion_expira_en | TIMESTAMPTZ | |
| verificacion_enviada_en | TIMESTAMPTZ | para limitar los reenvíos a uno por minuto |

### `ejercicios`

| Columna | Tipo | Restricciones |
|---|---|---|
| id | BIGINT | PK, identidad |
| nombre | VARCHAR(255) | NOT NULL |
| descripcion | VARCHAR(255) | |
| grupo_muscular | VARCHAR(255) | |
| usuario_id | BIGINT | FK → usuarios.id. Vacío = predefinido |

### `entrenamientos`

| Columna | Tipo | Restricciones |
|---|---|---|
| id | BIGINT | PK, identidad |
| usuario_id | BIGINT | NOT NULL, FK → usuarios.id |
| nombre | VARCHAR(255) | |
| fecha | DATE | NOT NULL |

### `series`

| Columna | Tipo | Restricciones |
|---|---|---|
| id | BIGINT | PK, identidad |
| entrenamiento_id | BIGINT | NOT NULL, FK → entrenamientos.id |
| ejercicio_id | BIGINT | NOT NULL, FK → ejercicios.id |
| repeticiones | INTEGER | NOT NULL |
| peso | DOUBLE PRECISION | NOT NULL, en kg |

### `fotos_perfil`

| Columna | Tipo | Restricciones |
|---|---|---|
| usuario_id | BIGINT | PK, FK → usuarios.id |
| datos | BYTEA | NOT NULL, la imagen (máx. 5 MB) |
| tipo | VARCHAR(255) | NOT NULL, `image/jpeg`, `image/png` o `image/webp` |

### `tokens_refresco`

| Columna | Tipo | Restricciones |
|---|---|---|
| id | BIGINT | PK, identidad |
| usuario_id | BIGINT | NOT NULL, FK → usuarios.id |
| hash | VARCHAR(64) | NOT NULL, UNIQUE, SHA-256 del token |
| creado_en | TIMESTAMPTZ | NOT NULL |
| expira_en | TIMESTAMPTZ | NOT NULL |
| revocado_en | TIMESTAMPTZ | vacío = sesión activa |

---

## Notas

- La foto va en una tabla aparte para no cargar sus bytes cada vez que se lee un `Usuario`.
- Ni las contraseñas ni los tokens se guardan en claro: contraseñas con BCrypt, tokens de refresco
  y de verificación con SHA-256.
- Borrar un ejercicio no borra sus series (no hay `ON DELETE CASCADE`): el backend lo impide con
  un 409 si tiene series. Al borrar una cuenta, el backend borra en orden sus sesiones, su foto,
  sus entrenamientos con sus series y sus ejercicios propios.
