package com.gympulse.backend.dtos;

import java.time.Instant;
import java.time.LocalDate;

public record UsuarioDTO(
        Long id,
        String username,
        String email,
        Double peso,
        Double altura,
        LocalDate fechaNacimiento,
        Instant fotoActualizadaEn) {
}
