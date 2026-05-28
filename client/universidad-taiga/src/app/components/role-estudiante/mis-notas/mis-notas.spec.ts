import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MisNotas } from './mis-notas';

describe('MisNotas', () => {
  let component: MisNotas;
  let fixture: ComponentFixture<MisNotas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MisNotas],
    }).compileComponents();

    fixture = TestBed.createComponent(MisNotas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
