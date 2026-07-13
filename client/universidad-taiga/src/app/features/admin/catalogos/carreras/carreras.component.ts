import { ChangeDetectionStrategy, Component } from '@angular/core';
import { SimpleCatalogPage } from '../simple-catalog-page/simple-catalog-page.component';

@Component({
  selector: 'app-carreras-catalogo',
  imports: [SimpleCatalogPage],
  template: '<app-simple-catalog-page kind="carreras" />',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CarrerasCatalogo {}
