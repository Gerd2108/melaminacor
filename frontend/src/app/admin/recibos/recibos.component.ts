import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Pagina, Recibo, mensajeError } from '../../core/models/modelos';
import { AdminService } from '../../core/services/admin.service';
import { SolesPipe } from '../../shared/pipes';

@Component({
  selector: 'app-admin-recibos',
  imports: [FormsModule, RouterLink, DatePipe, SolesPipe],
  template: `
    <div class="admin__cabecera"><h1>Recibos</h1></div>

    <form class="admin__filtros" (ngSubmit)="cargar(0)">
      <input type="search" name="q" [(ngModel)]="q" placeholder="Buscar por número, cliente o DNI/RUC" aria-label="Buscar recibos" />
      <button type="submit" class="boton boton--secundario boton--chico">Buscar</button>
    </form>

    @if (error()) { <p class="alerta alerta--error" role="alert">{{ error() }}</p> }

    @if (pagina(); as pg) {
      @if (pg.content.length === 0) {
        <p class="vacio">Aún no hay recibos. Se emiten solos cuando un pedido queda pagado.</p>
      } @else {
        <div class="tabla-envoltura">
          <table class="tabla">
            <thead><tr><th>Número</th><th>Emisión</th><th>Cliente</th><th>Documento</th><th>Total</th><th>Estado</th><th></th></tr></thead>
            <tbody>
              @for (r of pg.content; track r.id) {
                <tr>
                  <td><a [routerLink]="['/admin/recibos', r.id]">{{ r.numero }}</a><small class="bloque">{{ r.pedidoCodigo }}</small></td>
                  <td>{{ r.fechaEmision | date: 'dd/MM/yy HH:mm' }}</td>
                  <td>{{ r.clienteNombre }}</td>
                  <td>{{ r.clienteDocumentoTipo }} {{ r.clienteDocumento }}</td>
                  <td>{{ r.total | soles }}</td>
                  <td><span class="chip" [attr.data-estado]="r.estado === 'ANULADO' ? 'CANCELADO' : 'ENTREGADO'">{{ r.estado === 'ANULADO' ? 'Anulado' : 'Emitido' }}</span></td>
                  <td class="tabla__acciones"><a [routerLink]="['/admin/recibos', r.id]" class="enlace">Ver</a></td>
                </tr>
              }
            </tbody>
          </table>
        </div>
        @if (pg.totalPages > 1) {
          <nav class="paginacion" aria-label="Paginación">
            <button type="button" class="boton boton--secundario boton--chico" [disabled]="pg.page === 0" (click)="cargar(pg.page - 1)">Anterior</button>
            <span>Página {{ pg.page + 1 }} de {{ pg.totalPages }}</span>
            <button type="button" class="boton boton--secundario boton--chico" [disabled]="pg.page + 1 >= pg.totalPages" (click)="cargar(pg.page + 1)">Siguiente</button>
          </nav>
        }
      }
    } @else if (!error()) {
      <p class="estado">Cargando…</p>
    }
  `,
})
export class AdminRecibosComponent {
  private admin = inject(AdminService);
  q = '';
  pagina = signal<Pagina<Recibo> | null>(null);
  error = signal<string | null>(null);

  constructor() {
    this.cargar(0);
  }

  cargar(page: number): void {
    this.error.set(null);
    this.admin.recibos(this.q.trim(), page).subscribe({
      next: (p) => this.pagina.set(p),
      error: (e) => this.error.set(mensajeError(e)),
    });
  }
}
