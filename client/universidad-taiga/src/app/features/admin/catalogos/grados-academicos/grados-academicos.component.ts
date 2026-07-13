import { ChangeDetectionStrategy, Component } from '@angular/core';
import { SimpleCatalogPage } from '../simple-catalog-page/simple-catalog-page.component';

@Component({
  selector: 'app-grados-academicos-catalogo',
  imports: [SimpleCatalogPage],
  template: '<app-simple-catalog-page kind="grados-academicos" />',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GradosAcademicosCatalogo {}
