package com.gympulse.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearEjercicioDTO(
        @NotBlank(message = "es obligatorio")
        @Size(max = 60, message = "no puede tener mas de 60 caracteres")
        String nombre,

        @Size(max = 255, message = "no puede tener mas de 255 caracteres")
        String descripcion,

        @Size(max = 30, message = "no puede tener mas de 30 caracteres")
        String grupoMuscular) {
}
