package com.gympulse.backend.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public record ActualizarUsuarioDTO(
        @Positive(message = "debe ser mayor que 0")
        Double peso,

        @Positive(message = "debe ser mayor que 0")
        Double altura,

        @Past(message = "debe ser una fecha pasada")
        LocalDate fechaNacimiento) {
}
