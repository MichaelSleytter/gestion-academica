import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { Sidebar } from '../sidebar/sidebar';
import { Header } from '../header/header';
import { AuthService } from '../../../services/auth.service';
import { TuiNavigation } from '@taiga-ui/layout';
import { TuiScrollbar } from '@taiga-ui/core';

/**
 * Layout principal de la aplicación protegida.
 *
 * Contiene sidebar + header + router-outlet.
 * Redirige automáticamente al home del rol cuando se accede a la raíz /app.
 */
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, Sidebar, Header, TuiNavigation, TuiScrollbar],
  template: `
    <div class="flex h-screen bg-bg">
      <app-sidebar />

      <div class="flex flex-1 flex-col min-w-0">
        <app-header />

        <main
          class="flex-1 p-6 min-h-0"
          tuiNavigationMain
        >
          <tui-scrollbar class="h-full">
            <router-outlet />
          </tui-scrollbar>
        </main>
      </div>
    </div>
  `,
})
export class AppLayout implements OnInit {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  /**
   * Redirige al home del rol cuando se accede exactamente a /app.
   */
  ngOnInit(): void {
    const url = this.router.url;
    if (url === '/app' || url === '/app/') {
      const home = this.authService.getHomeRouteByRole();
      this.router.navigateByUrl(home, { replaceUrl: true });
    }
  }
}
