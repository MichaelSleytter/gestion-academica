import { Component } from '@angular/core';

/**
 * Vista del perfil del usuario autenticado.
 *
 * Muestra información personal del usuario (nombre, email, documento, rol)
 * y permite modificar datos básicos o cambiar contraseña.
 *
 * Accesible para: TODOS los roles (ADMIN, DOCENTE, ESTUDIANTE)
 */
@Component({
  imports: [],
  templateUrl: './perfil.html',
  styles: ``,
})
export class Perfil {}
