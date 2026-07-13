import { ChangeDetectionStrategy, Component } from '@angular/core';
import { SimpleCatalogPage } from '../simple-catalog-page/simple-catalog-page.component';

@Component({
  selector: 'app-ciclos-catalogo',
  imports: [SimpleCatalogPage],
  template: '<app-simple-catalog-page kind="ciclos" />',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CiclosCatalogo {}
