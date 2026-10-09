package com.gympulse.backend.controllers;

import com.gympulse.backend.dtos.CrearEntrenamientoDTO;
import com.gympulse.backend.dtos.EntrenamientoDTO;
import com.gympulse.backend.helpers.UsuarioAutenticado;
import com.gympulse.backend.services.EntrenamientoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/*
POST   /entrenamiento               -> crear un entrenamiento para el usuario del token
GET    /entrenamiento               -> obtener los entrenamientos del usuario del token
GET    /entrenamiento/{id}          -> obtener un entrenamiento propio por id
DELETE /entrenamiento/{id}          -> eliminar un entrenamiento propio
*/

@RestController
@RequestMapping("/entrenamiento")
public class EntrenamientoController {

    private final EntrenamientoService entrenamientoService;

    public EntrenamientoController(EntrenamientoService entrenamientoService) {
        this.entrenamientoService = entrenamientoService;
    }

    @PostMapping
    public ResponseEntity<EntrenamientoDTO> crear(@Valid @RequestBody CrearEntrenamientoDTO datos,
            @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(entrenamientoService.crear(datos, UsuarioAutenticado.id(token)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntrenamientoDTO> buscarPorId(@PathVariable Long id,
            @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(entrenamientoService.buscarPorId(id, UsuarioAutenticado.id(token)));
    }

    @GetMapping
    public ResponseEntity<List<EntrenamientoDTO>> buscarPropios(@AuthenticationPrincipal Jwt token) {
        return ResponseEntity.ok(entrenamientoService.buscarPropios(UsuarioAutenticado.id(token)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        entrenamientoService.eliminar(id, UsuarioAutenticado.id(token));
        return ResponseEntity.noContent().build();
    }
}
