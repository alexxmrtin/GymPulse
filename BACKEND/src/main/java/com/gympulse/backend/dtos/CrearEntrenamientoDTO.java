package com.gympulse.backend.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearEntrenamientoDTO(
        @Size(max = 60, message = "no puede tener mas de 60 caracteres")
        String nombre,

        @NotNull(message = "es obligatoria")
        LocalDate fecha) {
}
