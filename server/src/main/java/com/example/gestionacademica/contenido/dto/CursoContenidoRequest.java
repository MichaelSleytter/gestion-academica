package com.example.gestionacademica.contenido.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CursoContenidoRequest(
        @NotNull Integer idSeccion,
        @NotBlank String nombreOriginal,
        @NotBlank String key,
        @NotBlank String url,
        String mimeType,
        @PositiveOrZero Long sizeBytes,
        @NotNull @Min(1) @Max(18) Integer semana
) {
}
