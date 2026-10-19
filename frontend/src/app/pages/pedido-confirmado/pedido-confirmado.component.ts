import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ESTADOS, METODOS_PAGO, Pedido, mensajeError } from '../../core/models/modelos';
import { CulqiService } from '../../core/services/culqi.service';
import { PedidoService } from '../../core/services/pedido.service';
import { WhatsappService } from '../../core/services/whatsapp.service';
import { PagoCancelado } from '../../core/services/pago-simulado.service';
import { SolesPipe } from '../../shared/pipes';

@Component({
  selector: 'app-pedido-confirmado',
  imports: [RouterLink, SolesPipe],
  templateUrl: './pedido-confirmado.component.html',
})
export class PedidoConfirmadoComponent implements OnInit {
  private pedidos = inject(PedidoService);
  private culqi = inject(CulqiService);
  private whatsapp = inject(WhatsappService);

  codigo = input.required<string>();   // de la ruta /pedido/:codigo
  email = input<string>('');           // del query param ?email=

  pedido = signal<Pedido | null>(null);
  error = signal<string | null>(null);
  pagando = signal(false);

  estados = ESTADOS;
  metodos = METODOS_PAGO;
  yape = environment.yapePlin;
  banco = environment.cuentaBancaria;

  enlaceVoucher = computed(() => {
    const p = this.pedido();
    return p ? this.whatsapp.enlace(`Hola, envío el comprobante de pago del pedido ${p.codigo} por ${p.total}.`) : '';
  });

  ngOnInit(): void {
    this.pedidos.consultar(this.codigo(), this.email()).subscribe({
      next: (p) => this.pedido.set(p),
      error: (e) => this.error.set(mensajeError(e, 'No encontramos el pedido')),
    });
  }

  async pagar(): Promise<void> {
    const p = this.pedido();
    if (!p) return;
    this.error.set(null);
    try {
      const token = await this.culqi.obtenerToken(p.total, `Pedido ${p.codigo}`, p.email);
      this.pagando.set(true);
      this.pedido.set(await firstValueFrom(this.pedidos.pagarConCulqi(p.codigo, token, p.email)));
    } catch (e: any) {
      if (e instanceof PagoCancelado) return;
      this.error.set(e instanceof Error ? e.message : mensajeError(e));
    } finally {
      this.pagando.set(false);
    }
  }
}
