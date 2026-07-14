import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { throwError } from 'rxjs';

import { AuthService } from '../../../core/services/auth.service';
import { RoleService } from '../../../core/services/role.service';
import { provideAngularComponentTest } from '../../../testing/angular-test-providers';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let authService: ReturnType<typeof createAuthServiceMock>;

  beforeEach(async () => {
    authService = createAuthServiceMock();

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        ...provideAngularComponentTest(),
        { provide: AuthService, useValue: authService },
        { provide: RoleService, useValue: { getHomeRouteByRole: () => '/app' } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('focuses the first invalid field on invalid submit', () => {
    component.onSubmit();

    expect(component.form.controls.email.touched).toBe(true);
    expect(document.activeElement).toBe(fixture.nativeElement.querySelector('#email'));
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('renders authentication API errors inline', () => {
    authService.login.mockReturnValueOnce(throwError(() => ({ status: 401 })));
    component.form.setValue({ email: 'admin@example.com', password: 'secret1' });

    component.onSubmit();
    fixture.detectChanges();

    const alert = fixture.nativeElement.querySelector('[role="alert"]');
    expect(alert?.textContent).toContain('Email o contraseña incorrectos');
    expect(component.authError()).toContain('restablece tu contraseña');
  });
});

function createAuthServiceMock() {
  return {
    login: vi.fn(),
  };
}
