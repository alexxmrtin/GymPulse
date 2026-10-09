package com.gympulse.backend.controllers;

import com.gympulse.backend.dtos.CrearEjercicioDTO;
import com.gympulse.backend.dtos.EjercicioDTO;
import com.gympulse.backend.services.EjercicioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import com.gympulse.backend.helpers.UsuarioAutenticado;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/*
Cada usuario ve los ejercicios predefinidos mas los suyos; los de otros usuarios no existen para el.

POST   /ejercicio                        -> crear un ejercicio personalizado
GET    /ejercicio                        -> predefinidos + propios
GET    /ejercicio/{id}                   -> obtener un ejercicio visible por id
GET    /ejercicio?nombre=press           -> buscar por nombre
GET    /ejercicio?grupoMuscular=pecho    -> filtrar por grupo muscular
DELETE /ejercicio/{id}                   -> eliminar un ejercicio propio (los predefinidos no)
*/

@RestController
@RequestMapping("/ejercicio")
public class EjercicioController {

    private final EjercicioService ejercicioService;

    public EjercicioController(EjercicioService ejercicioService) {
        this.ejercicioService = ejercicioService;
    }

    @PostMapping
    public ResponseEntity<EjercicioDTO> crear(@Valid @RequestBody CrearEjercicioDTO datos, @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(ejercicioService.crear(datos, UsuarioAutenticado.id(token)));
    }

    @GetMapping
    public ResponseEntity<List<EjercicioDTO>> buscar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String grupoMuscular,
            @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(ejercicioService.buscar(UsuarioAutenticado.id(token), nombre, grupoMuscular));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EjercicioDTO> buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(ejercicioService.buscarPorId(id, UsuarioAutenticado.id(token)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        ejercicioService.eliminar(id, UsuarioAutenticado.id(token));
        return ResponseEntity.noContent().build();
    }
}
