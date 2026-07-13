package com.example.gestionacademica.auth.dto;

import java.util.List;

public record PerfilResponse(
    Integer idUsuario,
    String nombre,
    String apellido,
    String email,
    String emailPersonal,
    String numeroDocumento,
    boolean estado,
    String tipoDocumento,
    List<String> roles
) {}
