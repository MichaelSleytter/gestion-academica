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
