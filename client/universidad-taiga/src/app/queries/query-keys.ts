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

export const SECCIONES_PAGINADOS_KEY = (pagina: number, tamaño: number, busqueda?: string) =>
  ['secciones', 'pagina', pagina, tamaño, busqueda ?? ''] as const;
export const SECCIONES_KEY = ['secciones'] as const;
export const SECCION_KEY = (id: number) => ['seccion', id] as const;
export const SECCION_CREAR_MUTATION_KEY = ['secciones', 'crear'] as const;
export const SECCION_ACTUALIZAR_MUTATION_KEY = ['secciones', 'actualizar'] as const;
export const SECCION_ELIMINAR_MUTATION_KEY = ['secciones', 'eliminar'] as const;

export const CICLOS_ACADEMICOS_KEY = ['ciclos-academicos'] as const;
