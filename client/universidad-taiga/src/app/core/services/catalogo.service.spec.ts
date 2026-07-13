import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { APP_API_URL } from '../tokens/api.tokens';
import { CatalogoService } from './catalogo.service';

describe('CatalogoService', () => {
  const apiUrl = 'https://api.test/api/v1';
  let service: CatalogoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: APP_API_URL, useValue: apiUrl },
      ],
    });

    service = TestBed.inject(CatalogoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('loads especializaciones from the catalog endpoint', async () => {
    const result = service.getEspecializaciones();
    const request = http.expectOne(`${apiUrl}/especializaciones`);

    expect(request.request.method).toBe('GET');
    request.flush([{ idEspecializacion: 1, nombre: 'Matemáticas' }]);

    await expect(result).resolves.toEqual([{ idEspecializacion: 1, nombre: 'Matemáticas' }]);
  });

  it('creates, updates and deletes carrera catalog items', async () => {
    const create = service.createCatalogItem('carreras', { nombre: 'Ingeniería' });
    const createRequest = http.expectOne(`${apiUrl}/carreras`);
    expect(createRequest.request.method).toBe('POST');
    expect(createRequest.request.body).toEqual({ nombre: 'Ingeniería' });
    createRequest.flush({ idCarrera: 2, nombre: 'Ingeniería' });
    await expect(create).resolves.toEqual({ idCarrera: 2, nombre: 'Ingeniería' });

    const update = service.updateCatalogItem('carreras', 2, { nombre: 'Ingeniería Civil' });
    const updateRequest = http.expectOne(`${apiUrl}/carreras/2`);
    expect(updateRequest.request.method).toBe('PUT');
    expect(updateRequest.request.body).toEqual({ nombre: 'Ingeniería Civil' });
    updateRequest.flush({ idCarrera: 2, nombre: 'Ingeniería Civil' });
    await expect(update).resolves.toEqual({ idCarrera: 2, nombre: 'Ingeniería Civil' });

    const remove = service.deleteCatalogItem('carreras', 2);
    const deleteRequest = http.expectOne(`${apiUrl}/carreras/2`);
    expect(deleteRequest.request.method).toBe('DELETE');
    deleteRequest.flush(null);
    await expect(remove).resolves.toBeNull();
  });

  it('generates academic cycles for a year', async () => {
    const result = service.generarCiclosAcademicos({ anio: 2026 });
    const request = http.expectOne(`${apiUrl}/ciclos-academicos/generar-anio`);

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ anio: 2026 });
    request.flush([{ idCiclo: 10, nombre: '2026-I', fechaInicio: '2026-01-01', fechaFin: '2026-06-30' }]);

    await expect(result).resolves.toEqual([
      { idCiclo: 10, nombre: '2026-I', fechaInicio: '2026-01-01', fechaFin: '2026-06-30' },
    ]);
  });
});
