import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { METODOS_PAGO, METODOS_PAGO_DETALLE, MetodoPago, Pedido, mensajeError } from '../../core/models/modelos';
import { CarritoService } from '../../core/services/carrito.service';
import { CulqiService, LIMITE_YAPE } from '../../core/services/culqi.service';
import { PedidoService } from '../../core/services/pedido.service';
import { PagoCancelado } from '../../core/services/pago-simulado.service';
import { SolesPipe } from '../../shared/pipes';

@Component({
  selector: 'app-checkout',
  imports: [ReactiveFormsModule, RouterLink, SolesPipe],
  templateUrl: './checkout.component.html',
})
export class CheckoutComponent {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private pedidos = inject(PedidoService);
  culqi = inject(CulqiService);
  carrito = inject(CarritoService);

  metodos = Object.entries(METODOS_PAGO) as [MetodoPago, string][];
  detalle = METODOS_PAGO_DETALLE;
  limiteYape = LIMITE_YAPE;
  enviando = signal(false);
  error = signal<string | null>(null);
  /** Si el pago con tarjeta falla, guardamos el pedido para reintentar sin crear otro. */
  pedidoPendiente = signal<Pedido | null>(null);
  hayItems = computed(() => this.carrito.items().length > 0);
  /** Avisos que el cliente puede cerrar con la ×. */
  verAvisoPrueba = signal(true);
  verAvisoPendiente = signal(true);

  form = this.fb.nonNullable.group({
    nombreCliente: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', [Validators.required, Validators.pattern(/^9\d{8}$/)]],
    documento: ['', [Validators.required, Validators.pattern(/^(\d{8}|\d{11})$/)]],
    direccion: ['', [Validators.required, Validators.maxLength(200)]],
    distrito: ['', [Validators.required, Validators.maxLength(80)]],
    notas: ['', Validators.maxLength(500)],
    metodoPago: ['TARJETA' as MetodoPago, Validators.required],
  });

  invalido(campo: keyof typeof this.form.controls): boolean {
    const c = this.form.controls[campo];
    return c.invalid && (c.touched || c.dirty);
  }

  async confirmar(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    this.verAvisoPendiente.set(false);

    try {
      let pedido = this.pedidoPendiente();
      if (!pedido) {
        pedido = await firstValueFrom(
          this.pedidos.crear({
            ...this.form.getRawValue(),
            items: this.carrito.items().map((i) => ({ productoId: i.productoId, cantidad: i.cantidad })),
          }),
        );
      }

      if (pedido.metodoPago === 'TARJETA') {
        this.pedidoPendiente.set(pedido);
        // Liberamos el botón mientras el checkout está abierto: si el cliente lo cierra, puede reintentar.
        this.enviando.set(false);
        const token = await this.culqi.obtenerToken(pedido.total, `Pedido ${pedido.codigo}`, pedido.email);
        this.enviando.set(true);
        pedido = await firstValueFrom(this.pedidos.pagarConCulqi(pedido.codigo, token, pedido.email));
      }

      this.carrito.vaciar();
      this.router.navigate(['/pedido', pedido.codigo], { queryParams: { email: pedido.email } });
    } catch (e: any) {
      if (e instanceof PagoCancelado) {
        this.verAvisoPendiente.set(true); // solo recordamos que el pedido quedó registrado
      } else {
        this.error.set(e instanceof Error ? e.message : mensajeError(e));
      }
    } finally {
      this.enviando.set(false);
    }
  }
}
