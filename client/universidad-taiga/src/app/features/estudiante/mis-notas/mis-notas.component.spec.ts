import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';

import { MisNotas } from './mis-notas.component';
import { APP_API_URL } from '../../../core/tokens/api.tokens';
import {
  provideAngularComponentTest,
  provideQueryTestClient,
} from '../../../testing/angular-test-providers';

describe('MisNotas', () => {
  let component: MisNotas;
  let fixture: ComponentFixture<MisNotas>;

  beforeEach(async () => {
    const activatedRouteStub = {
      paramMap: of({ get: () => null }),
      url: of([]),
      snapshot: { paramMap: { get: () => null } },
    };

    await TestBed.configureTestingModule({
      imports: [MisNotas],
      providers: [
        provideHttpClient(),
        provideAngularComponentTest(),
        provideQueryTestClient(),
        { provide: ActivatedRoute, useValue: activatedRouteStub },
        { provide: APP_API_URL, useValue: 'http://localhost:8080/api/v1' },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MisNotas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
