import { DatePipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { METODOS_PAGO, Recibo } from '../core/models/modelos';
import { montoEnLetras } from './monto-en-letras';
import { SolesPipe } from './pipes';

/** El documento del recibo. Se usa en la página del cliente y en el panel; está pensado para imprimirse. */
@Component({
  selector: 'app-recibo-vista',
  imports: [DatePipe, SolesPipe],
  template: `
    @let r = recibo();
    <article class="recibo" [class.recibo--anulado]="r.estado === 'ANULADO'">
      @if (r.estado === 'ANULADO') {
        <div class="recibo__sello" aria-label="Recibo anulado">Anulado</div>
      }

      <header class="recibo__cabecera">
        <div class="recibo__emisor">
          <p class="recibo__empresa"><span class="marca__canto" aria-hidden="true"></span>{{ r.emisorRazonSocial }}</p>
          @if (r.emisorDireccion) { <p>{{ r.emisorDireccion }}</p> }
          @if (r.emisorTelefono) { <p>Teléfono {{ r.emisorTelefono }}</p> }
        </div>
        <div class="recibo__caja">
          <p>RUC {{ r.emisorRuc }}</p>
          <p class="recibo__tipo">Recibo de venta</p>
          <p class="recibo__numero">{{ r.numero }}</p>
        </div>
      </header>

      <dl class="recibo__datos">
        <div><dt>Cliente</dt><dd>{{ r.clienteNombre }}</dd></div>
        <div><dt>{{ r.clienteDocumentoTipo }}</dt><dd>{{ r.clienteDocumento }}</dd></div>
        @if (r.clienteDireccion) { <div class="recibo__ancho"><dt>Dirección</dt><dd>{{ r.clienteDireccion }}</dd></div> }
        <div><dt>Fecha de emisión</dt><dd>{{ r.fechaEmision | date: 'dd/MM/yyyy HH:mm' }}</dd></div>
        <div><dt>Pedido</dt><dd>{{ r.pedidoCodigo }}</dd></div>
        <div><dt>Forma de pago</dt><dd>{{ metodo() }}</dd></div>
        @if (r.referenciaPago) { <div><dt>Referencia</dt><dd>{{ r.referenciaPago }}</dd></div> }
      </dl>

      <table class="recibo__tabla">
        <thead>
          <tr><th class="num">Cant.</th><th>Descripción</th><th class="num">P. unitario</th><th class="num">Importe</th></tr>
        </thead>
        <tbody>
          @for (i of r.items; track i.productoId) {
            <tr>
              <td class="num">{{ i.cantidad }}</td>
              <td>{{ i.nombreProducto }}</td>
              <td class="num">{{ i.precioUnitario | soles }}</td>
              <td class="num">{{ i.subtotal | soles }}</td>
            </tr>
          }
        </tbody>
      </table>

      <div class="recibo__pie">
        <p class="recibo__letras">{{ letras() }}</p>
        <dl class="recibo__totales">
          <dt>Op. gravada</dt><dd>{{ r.subtotal | soles }}</dd>
          <dt>IGV (18 %)</dt><dd>{{ r.igv | soles }}</dd>
          <dt class="recibo__total">Importe total</dt><dd class="recibo__total">{{ r.total | soles }}</dd>
        </dl>
      </div>

      @if (r.estado === 'ANULADO' && r.fechaAnulacion) {
        <p class="recibo__nota recibo__nota--anulado">Recibo anulado el {{ r.fechaAnulacion | date: 'dd/MM/yyyy HH:mm' }} por cancelación del pedido.</p>
      }
      <p class="recibo__nota">Documento interno de {{ r.emisorRazonSocial }}. No reemplaza a un comprobante de pago electrónico emitido ante SUNAT.</p>
    </article>
  `,
})
export class ReciboVistaComponent {
  recibo = input.required<Recibo>();
  letras = computed(() => montoEnLetras(this.recibo().total));
  metodo = computed(() => METODOS_PAGO[this.recibo().metodoPago] ?? this.recibo().metodoPago);
}
