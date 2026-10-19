import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Producto } from '../core/models/modelos';
import { ImagenPipe, SolesPipe } from './pipes';

@Component({
  selector: 'app-producto-card',
  imports: [RouterLink, SolesPipe, ImagenPipe],
  template: `
    @let p = producto();
    <a class="tarjeta" [routerLink]="['/producto', p.id]">
      <div class="tarjeta__imagen">
        @if (p.imagenUrl | imagen; as src) {
          <img [src]="src" [alt]="p.nombre" loading="lazy" />
        } @else {
          <div class="sin-imagen" [attr.data-tipo]="p.tipo" aria-hidden="true"></div>
        }
        @if (p.precioOferta) { <span class="etiqueta etiqueta--oferta">Oferta</span> }
      </div>
      <div class="tarjeta__cuerpo">
        <h3 class="tarjeta__nombre">{{ p.nombre }}</h3>
        @if (detalle(); as d) {
          <p class="tarjeta__detalle">{{ d }}</p>
        }
        <div class="tarjeta__precio">
          @if (p.precioVigente === 0) {
            <span>Sin costo con tu compra</span>
          } @else if (p.soloCotizacion) {
            <span>Desde {{ p.precioVigente | soles }}@if (p.medidas) {<small class="precio__unidad"> {{ p.medidas.toLowerCase() }}</small>}</span>
          } @else {
            <span>{{ p.precioVigente | soles }}</span>
            @if (p.precioOferta) { <s>{{ p.precio | soles }}</s> }
          }
        </div>
      </div>
    </a>
  `,
})
export class ProductoCardComponent {
  producto = input.required<Producto>();

  /** "Roble Santana, 2.44 x 2.14 m, 18 mm" */
  detalle = computed(() => {
    const p = this.producto();
    return [p.color, p.medidas, p.espesor].filter(Boolean).join(', ');
  });
}
