import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Recibo, mensajeError } from '../../core/models/modelos';
import { PedidoService } from '../../core/services/pedido.service';
import { ReciboVistaComponent } from '../../shared/recibo-vista.component';

/** Página del recibo para el cliente: /recibo/PED-XXXX?email=... (sin cabecera de la tienda, lista para imprimir). */
@Component({
  selector: 'app-recibo',
  imports: [RouterLink, ReciboVistaComponent],
  template: `
    <div class="pagina-recibo">
      <div class="recibo-acciones no-imprimir">
        <a [routerLink]="['/pedido', codigo()]" [queryParams]="{ email: email() }" class="enlace">Volver a mi pedido</a>
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
    </div>
  `,
})
export class ReciboComponent implements OnInit {
  private pedidos = inject(PedidoService);
  codigo = input.required<string>();
  email = input<string>('');
  recibo = signal<Recibo | null>(null);
  error = signal<string | null>(null);

  ngOnInit(): void {
    this.pedidos.recibo(this.codigo(), this.email()).subscribe({
      next: (r) => this.recibo.set(r),
      error: (e) => this.error.set(mensajeError(e, 'No se encontró el recibo')),
    });
  }

  imprimir(): void {
    window.print();
  }
}
