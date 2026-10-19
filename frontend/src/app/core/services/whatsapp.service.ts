import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Producto } from '../models/modelos';
import { ItemCarrito } from './carrito.service';

@Injectable({ providedIn: 'root' })
export class WhatsappService {
  enlace(mensaje: string): string {
    return `https://wa.me/${environment.whatsapp}?text=${encodeURIComponent(mensaje)}`;
  }

  cotizarProducto(p: Producto): string {
    const detalles = [p.medidas && `Medidas: ${p.medidas}`, p.color && `Color: ${p.color}`, p.espesor && `Espesor: ${p.espesor}`]
      .filter(Boolean)
      .join('\n');
    return this.enlace(
      `Hola, quiero cotizar: ${p.nombre}${p.sku ? ` (${p.sku})` : ''}\n${detalles}\n\nCantidad / medidas que necesito: `,
    );
  }

  cotizarCarrito(items: ItemCarrito[]): string {
    const lineas = items.map((i) => `• ${i.cantidad} x ${i.nombre}`).join('\n');
    return this.enlace(`Hola, quiero cotizar estos productos:\n${lineas}\n\nEntrega en el distrito de: `);
  }

  consultaGeneral(): string {
    return this.enlace(`Hola, tengo una consulta sobre sus productos de melamina.`);
  }
}
