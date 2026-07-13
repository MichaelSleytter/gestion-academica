import { ChangeDetectionStrategy, Component } from '@angular/core';
import { SimpleCatalogPage } from '../simple-catalog-page/simple-catalog-page.component';

@Component({
  selector: 'app-especializaciones-catalogo',
  imports: [SimpleCatalogPage],
  template: '<app-simple-catalog-page kind="especializaciones" />',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EspecializacionesCatalogo {}
