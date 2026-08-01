# GymPulse — Estructura de la base de datos

## Diagrama de relaciones

```
usuarios
  │
  └──< entrenamientos
             │
             └──< series >── ejercicios
```

Un usuario tiene muchos entrenamientos.
Un entrenamiento tiene muchas series.
Cada serie apunta a un ejercicio y guarda el peso y las repeticiones de esa serie concreta.

---

## Tablas

### `usuarios`

| Columna           | Tipo        | Restricciones     |
|-------------------|-------------|-------------------|
| id                | BIGINT      | PK, autoincremento|
| username          | VARCHAR     | NOT NULL, UNIQUE  |
| email             | VARCHAR     | NOT NULL, UNIQUE  |
| password          | VARCHAR     | NOT NULL          |
| peso              | DOUBLE      |                   |
| altura            | DOUBLE      |                   |
| fecha_nacimiento  | DATE        |                   |

---

### `ejercicios`

| Columna         | Tipo    | Restricciones      |
|-----------------|---------|--------------------|
| id              | BIGINT  | PK, autoincremento |
| nombre          | VARCHAR | NOT NULL           |
| descripcion     | VARCHAR |                    |
| grupo_muscular  | VARCHAR |                    |

---

### `entrenamientos`

| Columna     | Tipo    | Restricciones          |
|-------------|---------|------------------------|
| id          | BIGINT  | PK, autoincremento     |
| usuario_id  | BIGINT  | FK → usuarios.id       |
| nombre      | VARCHAR |                        |
| fecha       | DATE    | NOT NULL               |

---

### `series`

| Columna          | Tipo    | Restricciones              |
|------------------|---------|----------------------------|
| id               | BIGINT  | PK, autoincremento         |
| entrenamiento_id | BIGINT  | FK → entrenamientos.id     |
| ejercicio_id     | BIGINT  | FK → ejercicios.id         |
| repeticiones     | INTEGER | NOT NULL                   |
| peso             | DOUBLE  | NOT NULL                   |

---

## Notas

- Las tablas las genera Hibernate automáticamente al arrancar la app (`ddl-auto=update`).
- La BD actual es H2 (embebida, provisional). La migración a PostgreSQL solo requiere cambiar el driver y la URL en `application.properties`.
- No borrar un ejercicio borra sus series históricas: `Ejercicio` no tiene `cascade = ALL` intencionalmente.
