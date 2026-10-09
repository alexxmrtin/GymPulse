package com.gympulse.backend.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioDTO(
        @NotBlank(message = "es obligatorio")
        @Size(max = 30, message = "no puede tener mas de 30 caracteres")
        String username,

        @NotBlank(message = "es obligatorio")
        @Email(message = "no es un email valido")
        String email,

        @NotBlank(message = "es obligatoria")
        @Size(min = 6, max = 72, message = "debe tener entre 6 y 72 caracteres")
        String password,

        @Positive(message = "debe ser mayor que 0")
        Double peso,

        @Positive(message = "debe ser mayor que 0")
        Double altura,

        @Past(message = "debe ser una fecha pasada")
        LocalDate fechaNacimiento) {
}
