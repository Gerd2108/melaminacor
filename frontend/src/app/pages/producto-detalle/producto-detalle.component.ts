import { Component, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';
import { Producto, TIPOS } from '../../core/models/modelos';
import { CarritoService } from '../../core/services/carrito.service';
import { CatalogoService } from '../../core/services/catalogo.service';
import { WhatsappService } from '../../core/services/whatsapp.service';
import { ImagenPipe, SolesPipe } from '../../shared/pipes';

@Component({
  selector: 'app-producto-detalle',
  imports: [RouterLink, SolesPipe, ImagenPipe],
  templateUrl: './producto-detalle.component.html',
})
export class ProductoDetalleComponent {
  private catalogo = inject(CatalogoService);
  private carrito = inject(CarritoService);
  whatsapp = inject(WhatsappService);

  /** Llega desde la ruta /producto/:id gracias a withComponentInputBinding. */
  id = input.required<string>();

  producto = signal<Producto | null>(null);
  noEncontrado = signal(false);
  cantidad = signal(1);
  agregado = signal(false);

  constructor() {
    toObservable(this.id)
      .pipe(
        tap(() => {
          this.producto.set(null);
          this.noEncontrado.set(false);
          this.cantidad.set(1);
          this.agregado.set(false);
        }),
        switchMap((id) => this.catalogo.producto(Number(id)).pipe(catchError(() => of(null)))),
        takeUntilDestroyed(),
      )
      .subscribe((p) => {
        this.producto.set(p);
        this.noEncontrado.set(!p);
      });
  }

  nombreTipo(p: Producto): string {
    return TIPOS.find((t) => t.valor === p.tipo)?.nombre ?? '';
  }

  cambiarCantidad(delta: number, max: number): void {
    this.cantidad.update((c) => Math.max(1, Math.min(max, c + delta)));
  }

  agregar(p: Producto): void {
    this.carrito.agregar(p, this.cantidad());
    this.agregado.set(true);
  }
}
