/**
 * Respuesta de un contenido (archivo) de sección desde el backend.
 */
export interface CursoContenidoResponse {
  idContenido: number;
  idSeccion: number;
  nombreOriginal: string;
  key: string;
  url: string;
  mimeType: string;
  sizeBytes: number;
  semana: number;
  fechaSubida: string;
  activo: boolean;
}
