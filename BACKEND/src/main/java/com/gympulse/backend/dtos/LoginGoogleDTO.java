package com.gympulse.backend.dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginGoogleDTO(
        @NotBlank(message = "es obligatorio")
        String idToken) {
}
