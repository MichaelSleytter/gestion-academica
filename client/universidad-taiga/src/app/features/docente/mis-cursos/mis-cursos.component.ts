import { Component, inject, computed } from '@angular/core';

/**
 * Vista principal del Docente.
 *
 * Muestra las secciones (cursos) asignadas al docente autenticado.
 * Desde aquí puede acceder a la carga de notas de cada sección.
 *
 * Es el home del rol DOCENTE (redirección post-login).
 *
 * Accesible para: DOCENTE
 */
@Component({
  imports: [],
  templateUrl: './mis-cursos.html',
  styles: ``,
})
export class MisCursos {}
