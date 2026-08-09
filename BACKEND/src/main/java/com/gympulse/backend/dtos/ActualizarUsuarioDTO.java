package com.gympulse.backend.dtos;

import java.time.LocalDate;

public record ActualizarUsuarioDTO(
        Double peso,
        Double altura,
        LocalDate fechaNacimiento) {
}
