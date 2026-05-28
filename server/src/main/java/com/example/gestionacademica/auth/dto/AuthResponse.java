package com.example.gestionacademica.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de autenticación.
 *
 * ¿QUÉ CONTIENE?
 * - accessToken: El JWT que usarás para autenticarte en cada request
 * - tokenType: Siempre "Bearer"
 * - expiresIn: Cuántos segundos hasta que expire el access token
 *
 *
 * ¿POR QUÉ retornar expiresIn?
 * Porque el frontend necesita saber cuánto tiempo tiene hasta que
 * el token expire. Así puede hacer refresh ANTES de que expire.
 * Un patrón común es refresh a los 3/4 de la vida útil del token.
 * Si expiresIn = 900 seg, refresh a los 675 seg (11 minutos).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
}