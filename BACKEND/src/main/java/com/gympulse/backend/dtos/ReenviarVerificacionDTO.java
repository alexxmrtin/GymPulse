package com.gympulse.backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReenviarVerificacionDTO(
        @NotBlank(message = "es obligatorio")
        @Email(message = "no es un email valido")
        String email) {
}
