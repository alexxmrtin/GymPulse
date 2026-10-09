package com.gympulse.backend.controllers;

import com.gympulse.backend.dtos.CrearSerieDTO;
import com.gympulse.backend.dtos.SerieDTO;
import com.gympulse.backend.helpers.UsuarioAutenticado;
import com.gympulse.backend.services.SerieService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/*
POST   /serie                           -> crear una serie en un entrenamiento propio
GET    /serie?entrenamientoId=1         -> series de un entrenamiento propio
GET    /serie?ejercicioId=1             -> series propias de un ejercicio, para ver la progresion
DELETE /serie/{id}                      -> eliminar una serie propia
*/

@RestController
@RequestMapping("/serie")
public class SerieController {

    private final SerieService serieService;

    public SerieController(SerieService serieService) {
        this.serieService = serieService;
    }

    @PostMapping
    public ResponseEntity<SerieDTO> crear(@Valid @RequestBody CrearSerieDTO datos,
            @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(serieService.crear(datos, UsuarioAutenticado.id(token)));
    }

    @GetMapping
    public ResponseEntity<List<SerieDTO>> buscar(
            @RequestParam(required = false) Long entrenamientoId,
            @RequestParam(required = false) Long ejercicioId,
            @AuthenticationPrincipal Jwt token) {

        Long usuarioId = UsuarioAutenticado.id(token);

        if (entrenamientoId != null) {
            return ResponseEntity.ok(serieService.buscarPorEntrenamiento(entrenamientoId, usuarioId));
        }

        if (ejercicioId != null) {
            return ResponseEntity.ok(serieService.buscarPorEjercicio(ejercicioId, usuarioId));
        }

        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        serieService.eliminar(id, UsuarioAutenticado.id(token));
        return ResponseEntity.noContent().build();
    }
}
