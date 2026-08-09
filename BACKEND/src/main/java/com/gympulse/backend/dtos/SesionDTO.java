package com.gympulse.backend.dtos;

public record SesionDTO(
        String token,
        String tipo,
        long expiraEnSegundos,
        UsuarioDTO usuario) {
}
