import { Routes } from '@angular/router';
import { inject } from '@angular/core';
import { adminGuard, permisoGuard } from './core/guards/admin.guard';
import { AuthService } from './core/services/auth.service';
import { PublicLayoutComponent } from './layout/public-layout.component';
import { AdminLayoutComponent } from './layout/admin-layout.component';

export const routes: Routes = [
  // Tienda pública
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      { path: '', loadComponent: () => import('./pages/inicio/inicio.component').then((m) => m.InicioComponent) },
      { path: 'catalogo', loadComponent: () => import('./pages/catalogo/catalogo.component').then((m) => m.CatalogoComponent) },
      { path: 'producto/:id', loadComponent: () => import('./pages/producto-detalle/producto-detalle.component').then((m) => m.ProductoDetalleComponent) },
      { path: 'carrito', loadComponent: () => import('./pages/carrito/carrito.component').then((m) => m.CarritoComponent) },
      { path: 'checkout', loadComponent: () => import('./pages/checkout/checkout.component').then((m) => m.CheckoutComponent) },
      { path: 'pedido/:codigo', loadComponent: () => import('./pages/pedido-confirmado/pedido-confirmado.component').then((m) => m.PedidoConfirmadoComponent) },
    ],
  },

  // Recibo imprimible del cliente (sin cabecera ni pie de la tienda)
  { path: 'recibo/:codigo', loadComponent: () => import('./pages/recibo/recibo.component').then((m) => m.ReciboComponent) },

  // Panel de administración
  { path: 'admin/login', loadComponent: () => import('./admin/login/login.component').then((m) => m.LoginComponent) },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [adminGuard],
    children: [
      // Cada rol entra a su primera pantalla (operario -> pedidos, el resto -> productos)
      { path: '', pathMatch: 'full', redirectTo: () => inject(AuthService).rutaInicio() },
      { path: 'productos', canActivate: [permisoGuard('productos')], loadComponent: () => import('./admin/productos/productos.component').then((m) => m.AdminProductosComponent) },
      { path: 'productos/nuevo', canActivate: [permisoGuard('productos')], loadComponent: () => import('./admin/producto-form/producto-form.component').then((m) => m.ProductoFormComponent) },
      { path: 'productos/:id', canActivate: [permisoGuard('productos')], loadComponent: () => import('./admin/producto-form/producto-form.component').then((m) => m.ProductoFormComponent) },
      { path: 'categorias', canActivate: [permisoGuard('categorias')], loadComponent: () => import('./admin/categorias/categorias.component').then((m) => m.AdminCategoriasComponent) },
      { path: 'pedidos', canActivate: [permisoGuard('pedidos')], loadComponent: () => import('./admin/pedidos/pedidos.component').then((m) => m.AdminPedidosComponent) },
      { path: 'recibos', canActivate: [permisoGuard('recibos')], loadComponent: () => import('./admin/recibos/recibos.component').then((m) => m.AdminRecibosComponent) },
      { path: 'recibos/:id', canActivate: [permisoGuard('recibos')], loadComponent: () => import('./admin/recibos/recibo-detalle.component').then((m) => m.ReciboDetalleComponent) },
      { path: 'usuarios', canActivate: [permisoGuard('usuarios')], loadComponent: () => import('./admin/usuarios/usuarios.component').then((m) => m.AdminUsuariosComponent) },
      { path: 'mi-cuenta', loadComponent: () => import('./admin/mi-cuenta/mi-cuenta.component').then((m) => m.MiCuentaComponent) },
    ],
  },

  { path: '**', redirectTo: '' },
];
