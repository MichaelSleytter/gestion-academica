package com.example.gestionacademica.estudiantes.util;

import java.security.SecureRandom;
import java.util.Locale;

import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;

/**
 * Utilidad para operaciones relacionadas con Estudiante.
 * - generar codigo de estudiante
 * - generar correo institucional a partir del codigo
 * - generar contraseña segura
 */
@NoArgsConstructor
@Component
public final class EstudianteUtil {

    private static final String PREFIJO_ESTUDIANTE = "EST";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Genera un código de estudiante determinístico a partir del identificador.
     *
     * @param id    identificador numérico del estudiante
     * @param ancho longitud del componente numérico con ceros a la izquierda
     * @return código en formato {@code EST-XXXXXXXX}
     * @throws IllegalArgumentException si id es negativo o ancho no es positivo
     */
    public static String generarCodigoDesdeId(long id, int ancho) {
        if (id < 0)
            throw new IllegalArgumentException("id debe ser >= 0");
        if (ancho <= 0)
            throw new IllegalArgumentException(
                    "ancho debe ser > 0");
        String rellenado = String.format(Locale.ROOT, "%0" + ancho + "d", id);
        return PREFIJO_ESTUDIANTE + "-" + rellenado;
    }

    /**
     * Genera un código de estudiante aleatorio con cantidad fija de dígitos.
     *
     * @param digitos cantidad de dígitos de la parte numérica
     * @return código en formato {@code EST-XXXXXXXX}
     * @throws IllegalArgumentException si digitos está fuera del rango 1..18
     */
    public static String generarCodigoAleatorio(int digitos) {
        if (digitos <= 0 || digitos > 18)
            throw new IllegalArgumentException(
                    "digitos debe ser 1..18");
        long max = (long) Math.pow(10, digitos);
        long valor = Math.abs(RANDOM.nextLong()) % max;
        String fmt = "%0" + digitos + "d";
        return (PREFIJO_ESTUDIANTE + "-" + String.format(Locale.ROOT, fmt, valor));
    }

    /**
     * Genera un correo institucional a partir del código de estudiante.
     *
     * @param codigo  código del estudiante
     * @param dominio dominio institucional preferido
     * @return correo institucional saneado
     */
    public static String generarCorreoDesdeCodigo(
            String codigo,
            String dominio) {
        String local = codigo
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "");
        String d = dominio == null
                ? "institution.edu.pe"
                : dominio.trim().toLowerCase(Locale.ROOT);
        if (!d.contains("."))
            d = d + ".edu.pe";
        return local + "@" + d;
    }

    /**
     * Genera una contraseña aleatoria segura de longitud dada.
     *
     * @param longitud longitud de la contraseña
     * @return contraseña aleatoria
     * @throws IllegalArgumentException si la longitud es menor a 8
     */
    public static String generarContrasenaSegura(int longitud) {
        if (longitud < 8)
            throw new IllegalArgumentException(
                    "longitud minima 8");
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_";
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            int idx = Math.abs(RANDOM.nextInt()) % chars.length();
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }
}
