package com.example.gestionacademica.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio de envío de correos electrónicos transaccionales.
 *
 * <p>Utiliza {@link JavaMailSender} configurado vía {@code spring.mail.*}
 * (Brevo SMTP en este proyecto). Antes de enviar, valida el formato y
 * los registros MX del dominio con {@link EmailValidatorService}.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailValidatorService emailValidatorService;

    @Value("${spring.mail.from}")
    private String fromAddress;

    /**
     * Envía las credenciales institucionales (email + contraseña temporal) al usuario.
     *
     * @param destinatario       correo del destinatario
     * @param nombre             nombre del destinatario para personalizar el saludo
     * @param emailInstitucional correo institucional asignado
     * @param passwordTemporal   contraseña temporal en texto plano
     * @throws RuntimeException si el email del destinatario no es válido
     */
    public void enviarCredenciales(
            String destinatario,
            String nombre,
            String emailInstitucional,
            String passwordTemporal) {
        validarDestinatario(destinatario);

        String asunto = "Bienvenido a Universidad Taiga - Sus credenciales";
        String cuerpo = String.format(
                "Hola %s,%n%n"
                        + "Le damos la bienvenida al sistema académico. "
                        + "A continuación sus credenciales de acceso:%n%n"
                        + "Correo institucional: %s%n"
                        + "Contraseña temporal: %s%n%n"
                        + "Por seguridad, cambie su contraseña al iniciar sesión.%n%n"
                        + "Atentamente,%n"
                        + "Equipo Universidad Taiga",
                nombre, emailInstitucional, passwordTemporal);

        enviar(destinatario, asunto, cuerpo);
    }

    /**
     * Envía el correo de recuperación de contraseña con el enlace de reset.
     *
     * @param destinatario correo del destinatario
     * @param nombre       nombre del destinatario
     * @param tokenReset   token UUID que el cliente usará para restablecer
     * @param urlBase      URL base del frontend (sin query params)
     * @throws RuntimeException si el email del destinatario no es válido
     */
    public void enviarRecuperacionPassword(
            String destinatario,
            String nombre,
            String tokenReset,
            String urlBase) {
        validarDestinatario(destinatario);

        String link = urlBase + "?token=" + tokenReset;
        String asunto = "Recuperación de contraseña - Universidad Taiga";
        String cuerpo = String.format(
                "Hola %s,%n%n"
                        + "Recibimos una solicitud para restablecer la contraseña de su cuenta.%n%n"
                        + "Para continuar, ingrese al siguiente enlace:%n"
                        + "%s%n%n"
                        + "Si usted no realizó esta solicitud, ignore este mensaje.%n%n"
                        + "El enlace expira en 30 minutos.%n%n"
                        + "Atentamente,%n"
                        + "Equipo Universidad Taiga",
                nombre, link);

        enviar(destinatario, asunto, cuerpo);
    }

    /**
     * Envía la confirmación de que la contraseña fue cambiada exitosamente.
     *
     * @param destinatario correo del destinatario
     * @param nombre       nombre del destinatario
     * @throws RuntimeException si el email del destinatario no es válido
     */
    public void enviarConfirmacionCambioPassword(String destinatario, String nombre) {
        validarDestinatario(destinatario);

        String asunto = "Contraseña actualizada - Universidad Taiga";
        String cuerpo = String.format(
                "Hola %s,%n%n"
                        + "Le confirmamos que su contraseña fue actualizada correctamente.%n%n"
                        + "Si usted no realizó este cambio, contacte al administrador inmediatamente.%n%n"
                        + "Atentamente,%n"
                        + "Equipo Universidad Taiga",
                nombre);

        enviar(destinatario, asunto, cuerpo);
    }

    private void validarDestinatario(String destinatario) {
        EmailValidatorService.ResultadoValidacion resultado =
                emailValidatorService.validar(destinatario);
        if (!resultado.esValido()) {
            throw new RuntimeException(
                    "El email del destinatario no es válido: " + resultado.getMensaje());
        }
    }

    private void enviar(String destinatario, String asunto, String cuerpo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(fromAddress);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            mailSender.send(mensaje);
            log.info("Correo enviado a {} con asunto '{}'", destinatario, asunto);
        } catch (MailException e) {
            log.error("Error al enviar correo a {}: {}", destinatario, e.getMessage(), e);
            throw new RuntimeException(
                    "No se pudo enviar el correo electrónico: " + e.getMessage(), e);
        }
    }
}
