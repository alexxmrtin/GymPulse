# Manejo de errores — Cómo funciona

## Estructura

```
ManejoErrores/
├── ManejadorErrores.java       ← captura las excepciones y decide el código HTTP
├── funcionamiento.md           ← este archivo
└── exceptions/
    ├── RecursoNoEncontradoException.java   ← lanzar cuando algo no existe → 404
    └── ConflictoException.java             ← lanzar cuando hay duplicado  → 409
```

## Por qué archivos separados

Java obliga a que cada clase pública esté en su propio archivo con el mismo nombre.
No se pueden meter varias clases públicas en un mismo `.java`.

- Las excepciones **definen** el tipo de error
- `ManejadorErrores` **captura** ese error y decide la respuesta HTTP

Son responsabilidades distintas aunque estén relacionadas.

## Flujo completo

```
Servicio detecta un problema
        ↓
Lanza una excepción concreta:
  throw new ConflictoException("El email ya esta registrado");
  throw new RecursoNoEncontradoException("Usuario no encontrado");
        ↓
Spring intercepta la excepción antes de que salga como respuesta
        ↓
ManejadorErrores busca el @ExceptionHandler del tipo que coincida:
  ConflictoException           → 409 Conflict
  RecursoNoEncontradoException → 404 Not Found
  IllegalArgumentException     → 400 Bad Request
  RuntimeException             → 500 Internal Server Error
        ↓
El cliente recibe un JSON con esta estructura:
  {
    "timestamp": "2026-08-07T14:32:10",
    "codigo": 404,
    "mensaje": "Usuario no encontrado"
  }
```

## Cómo añadir un error nuevo

1. Crear la clase en `exceptions/` extendiendo `RuntimeException`
2. Añadir su `@ExceptionHandler` en `ManejadorErrores`
3. Lanzarla desde el servicio con `throw new TuExcepcion("mensaje")`
