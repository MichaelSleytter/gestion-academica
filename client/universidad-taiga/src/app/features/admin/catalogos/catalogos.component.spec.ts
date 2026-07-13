import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { QueryClient } from '@tanstack/angular-query-experimental';
import { EMPTY } from 'rxjs';
import { TuiNotificationService } from '@taiga-ui/core';
import { CatalogoService } from '../../../core/services/catalogo.service';
import {
  provideAngularComponentTest,
  provideQueryTestClient,
} from '../../../testing/angular-test-providers';
import { CATALOGO_CARRERAS_KEY } from '../../../queries/query-keys';
import { CatalogosIndex } from './catalogos-index/catalogos-index.component';
import { CarrerasCatalogo } from './carreras/carreras.component';
import { SimpleCatalogPage } from './simple-catalog-page/simple-catalog-page.component';

describe('Admin catalog pages', () => {
  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('renders the catalog index cards', async () => {
    await TestBed.configureTestingModule({
      imports: [CatalogosIndex],
      providers: [...provideAngularComponentTest()],
    }).compileComponents();

    const fixture = TestBed.createComponent(CatalogosIndex);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Catálogos');
    expect(element.textContent).toContain('Carreras');
    expect(element.textContent).toContain('Especializaciones');
  });

  it('renders a CRUD catalog page and loads catalog items', async () => {
    const catalogoService = createCatalogoServiceMock();

    await TestBed.configureTestingModule({
      imports: [CarrerasCatalogo],
      providers: [
        ...provideAngularComponentTest(),
        ...provideQueryTestClient(),
        { provide: CatalogoService, useValue: catalogoService },
      ],
    }).compileComponents();

    const fixture: ComponentFixture<CarrerasCatalogo> = TestBed.createComponent(CarrerasCatalogo);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(catalogoService.getCatalogItems).toHaveBeenCalledWith('carreras');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Ingeniería de Sistemas');
  });

  it('invalidates the matching catalog query after a direct create operation', async () => {
    const catalogoService = createCatalogoServiceMock();
    const queryClient = { invalidateQueries: vi.fn().mockResolvedValue(undefined) };
    const notifications = { open: vi.fn(() => EMPTY) };

    catalogoService.createCatalogItem.mockResolvedValue({ idCarrera: 2, nombre: 'Medicina' });

    await TestBed.configureTestingModule({
      imports: [SimpleCatalogPage],
      providers: [
        ...provideAngularComponentTest(),
        { provide: CatalogoService, useValue: catalogoService },
        { provide: QueryClient, useValue: queryClient },
        { provide: TuiNotificationService, useValue: notifications },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(SimpleCatalogPage);
    fixture.componentRef.setInput('kind', 'carreras');
    fixture.detectChanges();
    await fixture.whenStable();

    const component = fixture.componentInstance;
    component.openCreate();
    component.form.controls.nombre.setValue('Medicina');

    await component.saveItem();

    expect(catalogoService.createCatalogItem).toHaveBeenCalledWith('carreras', {
      nombre: 'Medicina',
    });
    expect(queryClient.invalidateQueries).toHaveBeenCalledWith({ queryKey: CATALOGO_CARRERAS_KEY });
    expect(notifications.open).toHaveBeenCalledWith('Registro creado exitosamente', {
      label: 'Éxito',
      appearance: 'success',
      autoClose: 3000,
    });
  });
});

function createCatalogoServiceMock() {
  return {
    getCatalogItems: vi
      .fn()
      .mockResolvedValue([{ idCarrera: 1, nombre: 'Ingeniería de Sistemas' }]),
    createCatalogItem: vi.fn(),
    updateCatalogItem: vi.fn(),
    deleteCatalogItem: vi.fn(),
    generarCiclosAcademicos: vi.fn(),
  };
}
