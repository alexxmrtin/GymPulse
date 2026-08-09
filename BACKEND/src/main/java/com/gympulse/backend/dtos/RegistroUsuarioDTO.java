package com.gympulse.backend.dtos;

import java.time.LocalDate;

public record RegistroUsuarioDTO(
        String username,
        String email,
        String password,
        Double peso,
        Double altura,
        LocalDate fechaNacimiento) {
}
