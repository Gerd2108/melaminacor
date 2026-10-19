import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CarritoService } from '../../core/services/carrito.service';
import { WhatsappService } from '../../core/services/whatsapp.service';
import { ImagenPipe, SolesPipe } from '../../shared/pipes';

@Component({
  selector: 'app-carrito',
  imports: [RouterLink, SolesPipe, ImagenPipe],
  templateUrl: './carrito.component.html',
})
export class CarritoComponent {
  carrito = inject(CarritoService);
  private whatsapp = inject(WhatsappService);

  // Los precios incluyen IGV: separamos la base y el impuesto solo para mostrarlo.
  base = computed(() => Math.round((this.carrito.total() / 1.18) * 100) / 100);
  igv = computed(() => Math.round((this.carrito.total() - this.base()) * 100) / 100);
  enlaceCotizacion = computed(() => this.whatsapp.cotizarCarrito(this.carrito.items()));
}
