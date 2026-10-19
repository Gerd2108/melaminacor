import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Recibo, mensajeError } from '../../core/models/modelos';
import { AdminService } from '../../core/services/admin.service';
import { ReciboVistaComponent } from '../../shared/recibo-vista.component';

@Component({
  selector: 'app-recibo-detalle',
  imports: [RouterLink, ReciboVistaComponent],
  template: `
    <div class="recibo-acciones no-imprimir">
      <a routerLink="/admin/recibos" class="enlace">Volver a recibos</a>
      @if (recibo()) {
        <button type="button" class="boton boton--chico" (click)="imprimir()">Imprimir o guardar en PDF</button>
      }
    </div>
    @if (recibo(); as r) {
      <app-recibo-vista [recibo]="r" />
    } @else if (error()) {
      <p class="alerta alerta--error">{{ error() }}</p>
    } @else {
      <p class="estado">Cargando recibo…</p>
    }
  `,
})
export class ReciboDetalleComponent implements OnInit {
  private admin = inject(AdminService);
  id = input.required<string>();
  recibo = signal<Recibo | null>(null);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.admin.recibo(Number(this.id())).subscribe({
      next: (r) => this.recibo.set(r),
      error: (e) => this.error.set(mensajeError(e, 'No se encontró el recibo')),
    });
  }

  imprimir(): void {
    window.print();
  }
}
