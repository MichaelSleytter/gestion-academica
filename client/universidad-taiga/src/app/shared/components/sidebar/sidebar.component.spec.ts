import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { RoleService } from '../../../core/services/role.service';
import { Sidebar } from './sidebar.component';

describe('Sidebar', () => {
  const setup = (roleNames: string[], activeUrl = '/app/catalogos') => {
    const roles = signal(roleNames);
    const logout = vi.fn();
    const isActive = vi.fn((url: string) => url === activeUrl);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: { logout } },
        { provide: RoleService, useValue: { getRoles: roles.asReadonly() } },
        { provide: Router, useValue: { isActive } },
      ],
    });

    const component = TestBed.runInInjectionContext(() => new Sidebar());

    return { component, isActive, logout, roles };
  };

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('shows the admin academic navigation', () => {
    const { component } = setup(['ADMIN']);

    expect(component.menuItems().map((item) => item.label)).toEqual([
      'Inicio',
      'Estudiantes',
      'Docentes',
      'Cursos',
      'Catálogos',
      'Secciones',
      'Horarios',
      'Evaluaciones',
    ]);
    expect(component.menuItems().map((item) => item.route)).toEqual([
      'dashboard',
      'estudiantes',
      'docentes',
      'cursos',
      'catalogos',
      'secciones',
      'horarios',
      'evaluaciones',
    ]);
  });

  it('shows docente only its own courses in the sidebar', () => {
    const { component } = setup(['DOCENTE']);

    expect(component.menuItems().map((item) => item.label)).toEqual(['Mis Cursos']);
    expect(component.menuItems().map((item) => item.route)).toEqual(['docente/mis-cursos']);
  });

  it('keeps estudiante navigation unchanged', () => {
    const { component } = setup(['ESTUDIANTE']);

    expect(component.menuItems().map((item) => item.label)).toEqual(['Mis Cursos', 'Mi horario', 'Historial']);
    expect(component.menuItems().map((item) => item.route)).toEqual([
      'estudiante/mis-cursos',
      'estudiante/horario',
      'estudiante/historial',
    ]);
  });

  it('exposes active route state for accessibility', () => {
    const { component, isActive } = setup(['ADMIN'], '/app/catalogos');

    expect(component.isFocused('catalogos')).toBe(true);
    expect(component.isFocused('docentes')).toBe(false);
    expect(isActive).toHaveBeenCalledWith('/app/catalogos', {
      paths: 'subset',
      queryParams: 'ignored',
      matrixParams: 'ignored',
      fragment: 'ignored',
    });
  });

  it('delegates logout to the auth service', () => {
    const { component, logout } = setup(['ADMIN']);

    component.onLogout();

    expect(logout).toHaveBeenCalledOnce();
  });
});
