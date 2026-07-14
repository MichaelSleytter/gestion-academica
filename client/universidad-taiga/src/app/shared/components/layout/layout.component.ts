import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { Sidebar } from '../sidebar/sidebar.component';
import { Header } from '../header/header.component';
import { RoleService } from '../../../core/services/role.service';
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
    <a class="skip-link" href="#app-main">Saltar al contenido principal</a>
    <div class="flex h-screen overflow-x-hidden bg-background">
      <app-sidebar />

      <div class="flex flex-1 flex-col min-w-0">
        <app-header />

        <main
          id="app-main"
          class="flex-1 min-h-0 p-4 pb-24 pt-[max(1rem,env(safe-area-inset-top))] md:p-6 md:pb-6"
          tuiNavigationMain
          tabindex="-1"
        >
          <tui-scrollbar class="h-full">
            <router-outlet />
          </tui-scrollbar>
        </main>
      </div>
    </div>
  `,
  styles: `
    .skip-link {
      position: fixed;
      left: 1rem;
      top: 1rem;
      z-index: 1000;
      transform: translateY(-150%);
      border-radius: 0.75rem;
      background: var(--color-primary);
      color: white;
      padding: 0.75rem 1rem;
      font-weight: 700;
      transition: transform 0.15s ease-out;
    }

    .skip-link:focus-visible {
      transform: translateY(0);
      outline: 2px solid currentColor;
      outline-offset: 2px;
    }
  `,
})
export class AppLayout implements OnInit {
  private readonly router = inject(Router);
  private readonly roleService = inject(RoleService);

  /**
   * Redirige al home del rol cuando se accede exactamente a /app.
   */
  ngOnInit(): void {
    const url = this.router.url;
    if (url === '/app' || url === '/app/') {
      const home = this.roleService.getHomeRouteByRole();
      this.router.navigateByUrl(home, { replaceUrl: true });
    }
  }
}
