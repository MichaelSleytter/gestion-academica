import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CargaNotas } from './carga-notas';

describe('CargaNotas', () => {
  let component: CargaNotas;
  let fixture: ComponentFixture<CargaNotas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CargaNotas],
    }).compileComponents();

    fixture = TestBed.createComponent(CargaNotas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
