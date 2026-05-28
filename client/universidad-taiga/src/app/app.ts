import { TuiRoot } from '@taiga-ui/core';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Layout principal de la aplicación.
 *
 * LÓGICA:
 * - Si NO está autenticado: muestra SOLO el login (sin sidebar/header)
 * - Si ESTÁ autenticado: muestra el layout completo con sidebar/header
 *
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, TuiRoot],
  template: `
    <tui-root>
      <router-outlet />
    </tui-root>
  `,
  styleUrl: './app.less',
})
export class App {}
