import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Pagina, Producto, TIPOS, TipoProducto, mensajeError } from '../../core/models/modelos';
import { AdminService } from '../../core/services/admin.service';
import { AuthService } from '../../core/services/auth.service';
import { ImagenPipe, SolesPipe } from '../../shared/pipes';

@Component({
  selector: 'app-admin-productos',
  imports: [FormsModule, RouterLink, SolesPipe, ImagenPipe],
  templateUrl: './productos.component.html',
})
export class AdminProductosComponent {
  private admin = inject(AdminService);
  auth = inject(AuthService);

  tipos = TIPOS;
  q = '';
  tipo: TipoProducto | '' = '';
  pagina = signal<Pagina<Producto> | null>(null);
  error = signal<string | null>(null);

  constructor() {
    this.cargar(0);
  }

  cargar(page: number): void {
    this.error.set(null);
    this.admin.productos({ q: this.q.trim(), tipo: this.tipo || null, page, size: 20 }).subscribe({
      next: (p) => this.pagina.set(p),
      error: (e) => this.error.set(mensajeError(e)),
    });
  }

  eliminar(p: Producto): void {
    if (!confirm(`¿Eliminar "${p.nombre}"? Esta acción no se puede deshacer.`)) return;
    this.admin.eliminarProducto(p.id).subscribe({
      next: () => this.cargar(this.pagina()?.page ?? 0),
      error: (e) => this.error.set(mensajeError(e)),
    });
  }

  nombreTipo(t: TipoProducto): string {
    return TIPOS.find((x) => x.valor === t)?.nombre ?? t;
  }
}
