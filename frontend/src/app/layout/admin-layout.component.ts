import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { environment } from '../../environments/environment';
import { ROLES } from '../core/models/modelos';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="admin">
      <aside class="admin__lateral no-imprimir">
        <a routerLink="/admin" class="marca marca--clara">
          <span class="marca__canto" aria-hidden="true"></span>{{ empresa }}
        </a>
        <nav class="admin__nav" aria-label="Secciones del panel">
          @if (auth.puede('productos')) { <a routerLink="/admin/productos" routerLinkActive="activo">Productos</a> }
          @if (auth.puede('categorias')) { <a routerLink="/admin/categorias" routerLinkActive="activo">Categorías</a> }
          @if (auth.puede('pedidos')) { <a routerLink="/admin/pedidos" routerLinkActive="activo">Pedidos</a> }
          @if (auth.puede('recibos')) { <a routerLink="/admin/recibos" routerLinkActive="activo">Recibos</a> }
          @if (auth.puede('usuarios')) { <a routerLink="/admin/usuarios" routerLinkActive="activo">Usuarios</a> }
          <a routerLink="/admin/mi-cuenta" routerLinkActive="activo">Mi cuenta</a>
          <a routerLink="/">Ver tienda</a>
        </nav>
        <div class="admin__usuario">
          <span>{{ auth.sesion()?.nombre }}</span>
          <small>{{ nombreRol() }}</small>
          <button type="button" class="enlace" (click)="auth.logout()">Cerrar sesión</button>
        </div>
      </aside>
      <section class="admin__contenido">
        <router-outlet />
      </section>
    </div>
  `,
})
export class AdminLayoutComponent {
  auth = inject(AuthService);
  empresa = environment.nombreEmpresa;
  nombreRol = computed(() => {
    const r = this.auth.rol();
    return r ? ROLES[r].nombre : '';
  });
}
