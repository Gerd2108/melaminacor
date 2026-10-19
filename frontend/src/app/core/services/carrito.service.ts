import { Injectable, computed, effect, signal } from '@angular/core';
import { Producto } from '../models/modelos';

export interface ItemCarrito {
  productoId: number;
  nombre: string;
  precio: number;
  imagenUrl: string | null;
  stock: number;
  cantidad: number;
}

const CLAVE = 'melamina_carrito';

@Injectable({ providedIn: 'root' })
export class CarritoService {
  readonly items = signal<ItemCarrito[]>(this.leer());

  readonly cantidadTotal = computed(() => this.items().reduce((s, i) => s + i.cantidad, 0));
  /** Total con IGV incluido (los precios ya lo incluyen). */
  readonly total = computed(() => this.items().reduce((s, i) => s + i.precio * i.cantidad, 0));

  constructor() {
    // Cada vez que cambia el carrito, lo guardamos en el navegador.
    effect(() => {
      try {
        localStorage.setItem(CLAVE, JSON.stringify(this.items()));
      } catch {
        /* modo incógnito o almacenamiento lleno: el carrito sigue en memoria */
      }
    });
  }

  agregar(p: Producto, cantidad = 1): void {
    this.items.update((items) => {
      const existe = items.find((i) => i.productoId === p.id);
      if (existe) {
        return items.map((i) =>
          i.productoId === p.id ? { ...i, cantidad: Math.min(i.cantidad + cantidad, p.stock), stock: p.stock } : i,
        );
      }
      return [
        ...items,
        { productoId: p.id, nombre: p.nombre, precio: p.precioVigente, imagenUrl: p.imagenUrl, stock: p.stock, cantidad: Math.min(cantidad, p.stock) },
      ];
    });
  }

  cambiarCantidad(productoId: number, cantidad: number): void {
    if (cantidad < 1) return this.quitar(productoId);
    this.items.update((items) =>
      items.map((i) => (i.productoId === productoId ? { ...i, cantidad: Math.min(cantidad, i.stock) } : i)),
    );
  }

  quitar(productoId: number): void {
    this.items.update((items) => items.filter((i) => i.productoId !== productoId));
  }

  vaciar(): void {
    this.items.set([]);
  }

  private leer(): ItemCarrito[] {
    try {
      return JSON.parse(localStorage.getItem(CLAVE) ?? '[]');
    } catch {
      return [];
    }
  }
}
