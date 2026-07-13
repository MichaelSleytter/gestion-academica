package com.example.gestionacademica.auth.dto;

import jakarta.validation.constraints.Size;

public record ActualizarPerfilRequest(
    @Size(min = 1, max = 100)
    String nombre,

    @Size(min = 1, max = 100)
    String apellido,

    String emailPersonal
) {}
