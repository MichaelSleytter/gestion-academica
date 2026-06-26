/**
 * Respuesta paginada genérica del backend (Spring Page).
 * Coincide con la estructura de org.springframework.data.domain.Page serializada por Jackson.
 */
export interface PageResponse<T> {
  /** Elementos de la página actual */
  content: T[];
  /** Cantidad total de elementos en todas las páginas */
  totalElements: number;
  /** Cantidad total de páginas */
  totalPages: number;
  /** Número de página actual (0-based) */
  number: number;
  /** Tamaño de página */
  size: number;
  /** Indica si es la primera página */
  first: boolean;
  /** Indica si es la última página */
  last: boolean;
  /** Cantidad de elementos en esta página */
  numberOfElements: number;
  /** Indica si la página está vacía */
  empty: boolean;
}
