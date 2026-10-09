# Manejo de errores — Cómo funciona

## Estructura

```
ManejoErrores/
├── ManejadorErrores.java       ← captura las excepciones y decide el código HTTP
├── funcionamiento.md           ← este archivo
├── RespuestaNoAutenticado.java     ← errores de token: ocurren en un filtro, antes del controlador
├── RespuestaAccesoDenegado.java    ← permisos denegados en la propia cadena de seguridad
└── exceptions/
    ├── RecursoNoEncontradoException.java   ← lanzar cuando algo no existe    → 404
    ├── ConflictoException.java             ← lanzar cuando hay duplicado     → 409
    ├── CredencialesInvalidasException.java ← lanzar en un login fallido      → 401
    └── AccesoProhibidoException.java       ← lanzar si el recurso es de otro → 403
```

## Dos etapas distintas

`ManejadorErrores` solo captura lo que se lanza DESDE un controlador o servicio. Los fallos de
autenticacion ocurren antes, en los filtros de Spring Security, cuando todavia no hay controlador
al que aconsejar. Por eso `RespuestaNoAutenticado` y `RespuestaAccesoDenegado` escriben el JSON a
mano: para que el cliente reciba siempre la misma estructura venga el error de donde venga.

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
  ConflictoException              → 409 Conflict
  DataIntegrityViolationException → 409 Conflict  (la lanza la propia BD)
  RecursoNoEncontradoException    → 404 Not Found
  CredencialesInvalidasException  → 401 Unauthorized
  AccesoProhibidoException        → 403 Forbidden
  MethodArgumentNotValidException → 400 Bad Request (falla @Valid en un DTO; lista todos los campos)
  HttpMessageNotReadableException → 400 Bad Request (JSON mal formado)
  IllegalArgumentException        → 400 Bad Request
  MultipartException              → 400 Bad Request (subida de foto mal enviada)
  MaxUploadSizeExceededException  → 413 Content Too Large
  DemasiadosIntentosException     → 429 Too Many Requests (con Retry-After)
  RuntimeException                → 500 Internal Server Error
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
