import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { TuiButton, TuiIcon, TuiLoader } from '@taiga-ui/core';
import { map } from 'rxjs';
import { ContenidoService } from '../../../../core/services/contenido.service';
import type { CursoContenidoResponse } from '../../../../models/contenido/curso-contenido.response';

@Component({
  selector: 'app-contenido-curso',
  imports: [TuiButton, TuiIcon, TuiLoader],
  templateUrl: './contenido-curso.html',
  styles: ``,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContenidoCurso {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly contenidoService = inject(ContenidoService);

  readonly idSeccion = toSignal(
    this.route.paramMap.pipe(map((params) => Number(params.get('id')))),
    { initialValue: 0 },
  );

  readonly archivos = signal<CursoContenidoResponse[]>([]);
  readonly isLoading = signal(true);
  readonly uploading = signal(false);
  readonly uploadingFileName = signal('');
  readonly error = signal('');
  readonly semanas = Array.from({ length: 18 }, (_, index) => index + 1);
  readonly semanaSeleccionada = signal(1);

  constructor() {
    this.cargarArchivos();
  }

  private cargarArchivos(): void {
    const id = this.idSeccion();
    if (!id) return;

    this.isLoading.set(true);
    this.error.set('');

    this.contenidoService.listarPorSeccion(id).then((archivos) => {
      this.archivos.set(archivos);
      this.isLoading.set(false);
    }).catch(() => {
      this.error.set('No se pudieron cargar los archivos.');
      this.isLoading.set(false);
    });
  }

  onFileDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files?.[0];
    if (file) this.subirArchivo(file);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.subirArchivo(file);
    input.value = '';
  }

  onSemanaChange(event: Event): void {
    this.semanaSeleccionada.set(Number((event.target as HTMLSelectElement).value));
  }

  private async subirArchivo(file: File): Promise<void> {
    this.uploading.set(true);
    this.uploadingFileName.set(file.name);
    this.error.set('');

    try {
      await this.contenidoService.subirArchivo(this.idSeccion(), this.semanaSeleccionada(), file);
      this.cargarArchivos();
    } catch (err) {
      this.error.set(`Error al subir archivo: ${err instanceof Error ? err.message : 'Error desconocido'}`);
    } finally {
      this.uploading.set(false);
      this.uploadingFileName.set('');
    }
  }

  confirmarEliminar(idContenido: number, nombre: string): void {
    if (confirm(`¿Eliminar "${nombre}"? Esta acción no se puede deshacer.`)) {
      this.eliminarArchivo(idContenido);
    }
  }

  private eliminarArchivo(idContenido: number): void {
    this.contenidoService.eliminar(idContenido).then(() => {
      this.cargarArchivos();
    }).catch(() => {
      this.error.set('No se pudo eliminar el archivo.');
    });
  }

  fileIcon(mimeType: string): string {
    if (mimeType.startsWith('image/')) return '@tui.image';
    if (mimeType.startsWith('video/')) return '@tui.video';
    if (mimeType.startsWith('audio/')) return '@tui.music';
    if (mimeType.includes('pdf')) return '@tui.file-text';
    if (mimeType.includes('spreadsheet') || mimeType.includes('excel')) return '@tui.table';
    if (mimeType.includes('presentation') || mimeType.includes('powerpoint')) return '@tui.presentation';
    if (mimeType.includes('word') || mimeType.includes('document')) return '@tui.file-text';
    if (mimeType.includes('zip') || mimeType.includes('rar') || mimeType.includes('tar')) return '@tui.archive';
    return '@tui.file';
  }

  formatSize(bytes: number): string {
    if (!bytes) return '-';
    const units = ['B', 'KB', 'MB', 'GB'];
    let i = 0;
    let size = bytes;
    while (size >= 1024 && i < units.length - 1) {
      size /= 1024;
      i++;
    }
    return `${size.toFixed(i === 0 ? 0 : 1)} ${units[i]}`;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-PE', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  }

  goBack(): void {
    void this.router.navigate(['/app/docente/mis-cursos']);
  }
}
