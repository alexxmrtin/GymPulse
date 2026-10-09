package com.gympulse.backend.dtos;

public record SesionDTO(
        String token,
        String tipo,
        long expiraEnSegundos,
        String tokenRefresco,
        long tokenRefrescoExpiraEnSegundos,
        UsuarioDTO usuario) {
}
