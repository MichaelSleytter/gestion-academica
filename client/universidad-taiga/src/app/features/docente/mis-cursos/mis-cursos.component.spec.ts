import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideTanStackQuery, QueryClient } from '@tanstack/angular-query-experimental';
import { TokenService } from '../../../core/services/token.service';
import { DocenteRoleService } from '../../../core/services/docente-role.service';

import { MisCursos } from './mis-cursos.component';

describe('MisCursos', () => {
  let component: MisCursos;
  let fixture: ComponentFixture<MisCursos>;

  beforeEach(async () => {
    const router = { navigate: vi.fn().mockResolvedValue(true) };

    await TestBed.configureTestingModule({
      imports: [MisCursos],
      providers: [
        provideTanStackQuery(new QueryClient()),
        { provide: Router, useValue: router },
        { provide: TokenService, useValue: { extractCurrentUserId: () => 42 } },
        { provide: DocenteRoleService, useValue: { getSeccionesByDocente: vi.fn().mockResolvedValue([]) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MisCursos);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create with docente id from token', () => {
    expect(component).toBeTruthy();
    expect(component.docenteId()).toBe(42);
  });

  it('should navigate to grade loading route', () => {
    component.navigateToNotas(7);

    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith(['/app/docente/mis-cursos', 7, 'notas']);
  });
});
