package com.gympulse.backend.controllers;

import com.gympulse.backend.models.Serie;
import com.gympulse.backend.services.SerieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/*
POST   /serie                           -> crear una serie
GET    /serie?entrenamientoId=1         -> obtener todas las series de un entrenamiento
GET    /serie?ejercicioId=1             -> obtener todas las series de un ejercicio
DELETE /serie/{id}                      -> eliminar una serie
*/

@RestController
@RequestMapping("/serie")
public class SerieController {

    private final SerieService serieService;

    public SerieController(SerieService serieService) {
        this.serieService = serieService;
    }

    @PostMapping
    public ResponseEntity<Serie> crear(@RequestBody Serie serie) {
        Serie nueva = serieService.crear(serie);
        return ResponseEntity.ok(nueva);
    }

    @GetMapping
    public ResponseEntity<List<Serie>> buscar(
            @RequestParam(required = false) Long entrenamientoId,
            @RequestParam(required = false) Long ejercicioId) {

        if (entrenamientoId != null) {
            return ResponseEntity.ok(serieService.buscarPorEntrenamiento(entrenamientoId));
        }

        if (ejercicioId != null) {
            return ResponseEntity.ok(serieService.buscarPorEjercicio(ejercicioId));
        }

        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        serieService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
