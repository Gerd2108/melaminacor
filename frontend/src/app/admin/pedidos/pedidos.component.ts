import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ESTADOS, ESTADOS_OPERARIO, EstadoPedido, METODOS_PAGO, Pagina, Pedido, mensajeError } from '../../core/models/modelos';
import { AuthService } from '../../core/services/auth.service';
import { AdminService } from '../../core/services/admin.service';
import { SolesPipe } from '../../shared/pipes';

@Component({
  selector: 'app-admin-pedidos',
  imports: [FormsModule, DatePipe, SolesPipe, RouterLink],
  templateUrl: './pedidos.component.html',
})
export class AdminPedidosComponent {
  private admin = inject(AdminService);
  private auth = inject(AuthService);

  estados = Object.entries(ESTADOS) as [EstadoPedido, string][];
  /** Lo que puede elegir este usuario al actualizar un pedido. */
  esOperario = this.auth.rol() === 'OPERARIO';
  estadosEditables = this.esOperario ? this.estados.filter(([e]) => ESTADOS_OPERARIO.includes(e)) : this.estados;
  puedeActualizar = this.auth.tiene(['ADMIN', 'VENTAS', 'OPERARIO']);
  puedeVerRecibos = this.auth.puede('recibos');
  nombresEstado = ESTADOS;
  metodos = METODOS_PAGO;

  filtro: EstadoPedido | '' = '';
  pagina = signal<Pagina<Pedido> | null>(null);
  abierto = signal<number | null>(null);
  error = signal<string | null>(null);

  // Formulario de cambio de estado del pedido abierto
  nuevoEstado: EstadoPedido = 'PAGADO';
  referencia = '';

  constructor() {
    this.cargar(0);
  }

  cargar(page: number): void {
    this.error.set(null);
    this.admin.pedidos(this.filtro || null, page).subscribe({
      next: (p) => this.pagina.set(p),
      error: (e) => this.error.set(mensajeError(e)),
    });
  }

  alternar(p: Pedido): void {
    this.abierto.set(this.abierto() === p.id ? null : p.id);
    this.nuevoEstado = this.esOperario && !ESTADOS_OPERARIO.includes(p.estado) ? 'EN_PREPARACION' : p.estado;
    this.referencia = p.referenciaPago ?? '';
  }

  actualizar(p: Pedido): void {
    if (this.nuevoEstado === 'CANCELADO' && !confirm(`¿Cancelar el pedido ${p.codigo}? Se repondrá el stock si ya fue descontado.`)) return;
    this.error.set(null);
    this.admin.cambiarEstado(p.id, this.nuevoEstado, this.referencia).subscribe({
      next: (actualizado) =>
        this.pagina.update((pg) => pg && { ...pg, content: pg.content.map((x) => (x.id === actualizado.id ? actualizado : x)) }),
      error: (e) => this.error.set(mensajeError(e)),
    });
  }
}
