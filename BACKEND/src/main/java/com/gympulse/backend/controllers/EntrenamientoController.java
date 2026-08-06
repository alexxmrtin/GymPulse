package com.gympulse.backend.controllers;

import com.gympulse.backend.models.Entrenamiento;
import com.gympulse.backend.services.EntrenamientoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/*
POST   /entrenamiento               -> crear un entrenamiento
GET    /entrenamiento/{id}          -> obtener un entrenamiento por id
GET    /entrenamiento?usuarioId=1   -> obtener todos los entrenamientos de un usuario
DELETE /entrenamiento/{id}          -> eliminar un entrenamiento
*/

@RestController
@RequestMapping("/entrenamiento")
public class EntrenamientoController {

    private final EntrenamientoService entrenamientoService;

    public EntrenamientoController(EntrenamientoService entrenamientoService) {
        this.entrenamientoService = entrenamientoService;
    }

    @PostMapping
    public ResponseEntity<Entrenamiento> crear(@RequestBody Entrenamiento entrenamiento) {
        Entrenamiento nuevo = entrenamientoService.crear(entrenamiento);
        return ResponseEntity.ok(nuevo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entrenamiento> buscarPorId(@PathVariable Long id) {
        Optional<Entrenamiento> entrenamiento = entrenamientoService.buscarPorId(id);

        if (entrenamiento.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(entrenamiento.get());
    }

    @GetMapping
    public ResponseEntity<List<Entrenamiento>> buscarPorUsuario(@RequestParam Long usuarioId) {
        List<Entrenamiento> entrenamientos = entrenamientoService.buscarPorUsuario(usuarioId);
        return ResponseEntity.ok(entrenamientos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        entrenamientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
