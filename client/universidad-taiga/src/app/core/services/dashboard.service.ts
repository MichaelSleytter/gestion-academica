import { inject, Injectable } from '@angular/core';
import { EstudianteService } from './estudiante.service';
import { DocenteService } from './docente.service';
import { CursoService } from './curso.service';
import { SeccionService } from './seccion.service';

/**
 * Estadísticas agregadas del dashboard.
 */
export interface DashboardStats {
  /** Total de estudiantes registrados */
  estudiantes: number;
  /** Total de docentes registrados */
  docentes: number;
  /** Total de cursos */
  cursos: number;
  /** Total de secciones */
  secciones: number;
}

/**
 * Servicio que agrega estadísticas del dashboard consultando
 * los endpoints paginados con tamaño=1 y extrayendo totalElements.
 */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly estudianteService = inject(EstudianteService);
  private readonly docenteService = inject(DocenteService);
  private readonly cursoService = inject(CursoService);
  private readonly seccionService = inject(SeccionService);

  /**
   * Obtiene las estadísticas del sistema en paralelo.
   *
   * @returns objeto con totales de estudiantes, docentes, cursos y secciones
   */
  async getStats(): Promise<DashboardStats> {
    const [estudiantes, docentes, cursos, secciones] = await Promise.all([
      this.estudianteService.getEstudiantesPaginado(0, 1),
      this.docenteService.getDocentesPaginado(0, 1),
      this.cursoService.getCursosPaginado(0, 1),
      this.seccionService.getSeccionesPaginado(0, 1),
    ]);

    return {
      estudiantes: estudiantes.totalElements,
      docentes: docentes.totalElements,
      cursos: cursos.totalElements,
      secciones: secciones.totalElements,
    };
  }
}
