package com.gympulse.backend.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "es obligatorio")
        String email,

        @NotBlank(message = "es obligatoria")
        String password) {
}
