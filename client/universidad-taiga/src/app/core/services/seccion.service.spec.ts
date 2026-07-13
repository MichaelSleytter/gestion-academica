import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { APP_API_URL } from '../tokens/api.tokens';
import { SeccionService } from './seccion.service';

describe('SeccionService', () => {
  const apiUrl = 'https://api.test/api/v1';
  let service: SeccionService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: APP_API_URL, useValue: apiUrl },
      ],
    });

    service = TestBed.inject(SeccionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('requests the next section code with course and cycle query params', async () => {
    const result = service.getProximoCodigo(12, 34);
    const request = http.expectOne((req) => req.url === `${apiUrl}/secciones/proximo-codigo`);

    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('idCurso')).toBe('12');
    expect(request.request.params.get('idCiclo')).toBe('34');

    request.flush({ codigo: 'MAT-I-007' });

    await expect(result).resolves.toBe('MAT-I-007');
  });
});
