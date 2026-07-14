import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, Validators } from '@angular/forms';

import { provideAngularComponentTest } from '../../../../testing/angular-test-providers';
import { EstudianteForm } from './estudiante-form.component';

describe('EstudianteForm', () => {
  let fixture: ComponentFixture<EstudianteForm>;
  let component: EstudianteForm;
  let saveEmitted: boolean;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EstudianteForm],
      providers: [...provideAngularComponentTest()],
    }).compileComponents();

    const form = new FormBuilder().group({
      nombre: ['', [Validators.required, Validators.minLength(2)]],
      apellido: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      ciclo: [1, [Validators.required, Validators.min(1)]],
      numeroDocumento: ['', [Validators.required, Validators.minLength(8)]],
      tipoDocumento: [null, Validators.required],
      carrera: [null, Validators.required],
    });

    fixture = TestBed.createComponent(EstudianteForm);
    component = fixture.componentInstance;
    saveEmitted = false;
    fixture.componentRef.setInput('form', form);
    fixture.componentRef.setInput('modo', 'crear');
    component.guardar.subscribe(() => {
      saveEmitted = true;
    });
    fixture.detectChanges();
  });

  it('marks controls, focuses first invalid control, and does not emit save on invalid submit', async () => {
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;

    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
    await new Promise((resolve) => setTimeout(resolve));
    fixture.detectChanges();

    expect(component.form().controls['nombre'].touched).toBe(true);
    expect(fixture.nativeElement.querySelector('input[name="nombre"]').getAttribute('aria-invalid')).toBe('true');
    expect(document.activeElement).toBe(fixture.nativeElement.querySelector('input[name="nombre"]'));
    expect(saveEmitted).toBe(false);
  });
});
