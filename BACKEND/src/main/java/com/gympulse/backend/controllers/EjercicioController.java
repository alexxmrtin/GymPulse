package com.gympulse.backend.controllers;

import com.gympulse.backend.models.Ejercicio;
import com.gympulse.backend.services.EjercicioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

/*
POST   /ejercicio                        -> crear un ejercicio
GET    /ejercicio                        -> obtener todos los ejercicios
GET    /ejercicio/{id}                   -> obtener un ejercicio por id
GET    /ejercicio?nombre=press           -> buscar ejercicios por nombre
GET    /ejercicio?grupoMuscular=pecho    -> filtrar por grupo muscular
DELETE /ejercicio/{id}                   -> eliminar un ejercicio
*/

@RestController
@RequestMapping("/ejercicio")
public class EjercicioController {

    private final EjercicioService ejercicioService;

    public EjercicioController(EjercicioService ejercicioService) {
        this.ejercicioService = ejercicioService;
    }

    @PostMapping
    public ResponseEntity<Ejercicio> crear(@RequestBody Ejercicio ejercicio) {
        Ejercicio nuevo = ejercicioService.crear(ejercicio);
        return ResponseEntity.ok(nuevo);
    }

    @GetMapping
    public ResponseEntity<List<Ejercicio>> buscar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String grupoMuscular) {

        if (nombre != null) {
            return ResponseEntity.ok(ejercicioService.buscarPorNombre(nombre));
        }

        if (grupoMuscular != null) {
            return ResponseEntity.ok(ejercicioService.buscarPorGrupoMuscular(grupoMuscular));
        }

        return ResponseEntity.ok(ejercicioService.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ejercicio> buscarPorId(@PathVariable Long id) {
        Optional<Ejercicio> ejercicio = ejercicioService.buscarPorId(id);

        if (ejercicio.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ejercicio.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ejercicioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
