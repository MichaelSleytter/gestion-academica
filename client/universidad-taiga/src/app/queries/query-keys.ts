/**
 * Claves de queries centralizadas para evitar strings mágicos y permitir
 * invalidaciones/consultas consistentes.
 */
export const ESTUDIANTES_KEY = ['estudiantes'] as const;
export const ESTUDIANTES_PAGINADOS_KEY = (pagina: number, tamaño: number, busqueda?: string) =>
  ['estudiantes', 'pagina', pagina, tamaño, busqueda ?? ''] as const;
export const ESTUDIANTE_KEY = (id: number) => ['estudiante', id] as const;
export const ESTUDIANTE_CREAR_MUTATION_KEY = ['estudiantes', 'crear'] as const;
export const ESTUDIANTE_ACTUALIZAR_MUTATION_KEY = ['estudiantes', 'actualizar'] as const;
export const ESTUDIANTE_ELIMINAR_MUTATION_KEY = ['estudiantes', 'eliminar'] as const;

export const CATALOGO_TIPOS_DOCUMENTO_KEY = ['catalogo', 'tipos-documento'] as const;
export const CATALOGO_CARRERAS_KEY = ['catalogo', 'carreras'] as const;
export const CATALOGO_GRADOS_KEY = ['catalogo', 'grados-academicos'] as const;
export const CATALOGO_ESPECIALIZACIONES_KEY = ['catalogo', 'especializaciones'] as const;
export const CATALOGO_CICLOS_KEY = ['catalogo', 'ciclos-academicos'] as const;

export const CATALOGO_ITEM_KEY = (id: number) => ['catalogo-item', id] as const;

export const DOCENTES_PAGINADOS_KEY = (pagina: number, tamaño: number, busqueda?: string) =>
  ['docentes', 'pagina', pagina, tamaño, busqueda ?? ''] as const;
export const DOCENTES_KEY = ['docentes'] as const;
export const DOCENTE_KEY = (id: number) => ['docente', id] as const;

export const CURSOS_PAGINADOS_KEY = (pagina: number, tamaño: number, busqueda?: string) =>
  ['cursos', 'pagina', pagina, tamaño, busqueda ?? ''] as const;
export const CURSOS_KEY = ['cursos'] as const;
export const CURSO_KEY = (id: number) => ['curso', id] as const;
export const CURSO_CREAR_MUTATION_KEY = ['cursos', 'crear'] as const;
export const CURSO_ACTUALIZAR_MUTATION_KEY = ['cursos', 'actualizar'] as const;
export const CURSO_ELIMINAR_MUTATION_KEY = ['cursos', 'eliminar'] as const;

export const SECCIONES_PAGINADOS_KEY = (
  pagina: number,
  tamaño: number,
  busqueda?: string,
  idCiclo?: number,
) => ['secciones', 'pagina', pagina, tamaño, busqueda ?? '', idCiclo ?? 'all'] as const;
export const SECCIONES_KEY = ['secciones'] as const;
export const SECCION_KEY = (id: number) => ['seccion', id] as const;
export const SECCION_CREAR_MUTATION_KEY = ['secciones', 'crear'] as const;
export const SECCION_ACTUALIZAR_MUTATION_KEY = ['secciones', 'actualizar'] as const;
export const SECCION_ELIMINAR_MUTATION_KEY = ['secciones', 'eliminar'] as const;

export const CICLOS_ACADEMICOS_KEY = ['ciclos-academicos'] as const;

export const HORARIOS_PAGINADOS_KEY = (pagina: number, tamaño: number, busqueda?: string) =>
  ['horarios', 'pagina', pagina, tamaño, busqueda ?? ''] as const;
export const HORARIOS_KEY = ['horarios'] as const;
export const HORARIO_KEY = (id: number) => ['horario', id] as const;
export const HORARIO_CREAR_MUTATION_KEY = ['horarios', 'crear'] as const;
export const HORARIO_ACTUALIZAR_MUTATION_KEY = ['horarios', 'actualizar'] as const;
export const HORARIO_ELIMINAR_MUTATION_KEY = ['horarios', 'eliminar'] as const;

export const EVALUACIONES_PAGINADOS_KEY = (pagina: number, tamaño: number, busqueda?: string) =>
  ['evaluaciones', 'pagina', pagina, tamaño, busqueda ?? ''] as const;
export const EVALUACIONES_KEY = ['evaluaciones'] as const;
export const EVALUACION_KEY = (id: number) => ['evaluacion', id] as const;
export const EVALUACION_CREAR_MUTATION_KEY = ['evaluaciones', 'crear'] as const;
export const EVALUACION_ACTUALIZAR_MUTATION_KEY = ['evaluaciones', 'actualizar'] as const;
export const EVALUACION_ELIMINAR_MUTATION_KEY = ['evaluaciones', 'eliminar'] as const;

export const DOCENTE_SECCIONES_KEY = (idDocente: number | null) =>
  ['docente', idDocente, 'secciones'] as const;
export const EVALUACIONES_BY_SECCION_KEY = (idSeccion: number | null) =>
  ['secciones', idSeccion, 'evaluaciones'] as const;
export const MATRICULAS_BY_SECCION_KEY = (idSeccion: number | null) =>
  ['secciones', idSeccion, 'matriculas'] as const;
export const MATRICULAR_MUTATION_KEY = ['matriculas', 'matricular'] as const;
export const RETIRAR_MUTATION_KEY = ['matriculas', 'retirar'] as const;
export const NOTAS_BY_EVALUACION_KEY = (idEvaluacion: number | null) =>
  ['evaluaciones', idEvaluacion, 'notas'] as const;

export const HISTORIAL_PROGRESO_ME_KEY = ['historial-progreso', 'me'] as const;
export const HISTORIAL_PROGRESO_ESTUDIANTE_KEY = (idEstudiante: number | null) =>
  ['historial-progreso', 'estudiante', idEstudiante] as const;
