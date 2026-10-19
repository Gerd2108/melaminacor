import { Component, ElementRef, computed, effect, inject, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PagoSimuladoService } from '../core/services/pago-simulado.service';
import { SolesPipe } from './pipes';

/** Algoritmo de Luhn: el mismo control que usan las pasarelas para validar números de tarjeta. */
function luhnValido(numero: string): boolean {
  let suma = 0;
  let doble = false;
  for (let i = numero.length - 1; i >= 0; i--) {
    let d = Number(numero[i]);
    if (doble) {
      d *= 2;
      if (d > 9) d -= 9;
    }
    suma += d;
    doble = !doble;
  }
  return numero.length >= 13 && suma % 10 === 0;
}

@Component({
  selector: 'app-pago-simulado',
  imports: [FormsModule, SolesPipe],
  template: `
    @if (servicio.solicitud(); as s) {
      <div class="pago-fondo" (click)="cerrar()"></div>
      <div class="pago" role="dialog" aria-modal="true" aria-labelledby="pago-titulo" (keydown.escape)="cerrar()">
        <header class="pago__cabecera">
          <div>
            <h2 id="pago-titulo">Pagar {{ s.monto | soles }}</h2>
            <p>{{ s.descripcion }}</p>
          </div>
          <button type="button" class="pago__cerrar" (click)="cerrar()" aria-label="Cerrar">×</button>
        </header>

        <p class="pago__aviso">Modo de prueba: no se realiza ningún cobro real.</p>

        <div class="pago__pestanas" role="tablist">
          <button type="button" role="tab" [attr.aria-selected]="medio() === 'tarjeta'" [class.activa]="medio() === 'tarjeta'" (click)="elegir('tarjeta')">Tarjeta</button>
          @if (s.permiteYape) {
            <button type="button" role="tab" [attr.aria-selected]="medio() === 'yape'" [class.activa]="medio() === 'yape'" (click)="elegir('yape')">Yape</button>
          }
        </div>

        <form class="pago__form" (ngSubmit)="pagar()" novalidate>
          @if (medio() === 'tarjeta') {
            <label class="campo">
              <span>Número de tarjeta</span>
              <input #primerCampo [value]="numero" (input)="alEscribirTarjeta($event)"
                     inputmode="numeric" autocomplete="cc-number" placeholder="4111 1111 1111 1111" />
            </label>
            <div class="campos-2">
              <label class="campo">
                <span>Vencimiento</span>
                <input [value]="vencimiento" (input)="alEscribirVencimiento($event)"
                       inputmode="numeric" autocomplete="cc-exp" placeholder="MM/AA" />
              </label>
              <label class="campo">
                <span>CVV</span>
                <input name="cvv" [(ngModel)]="cvv" inputmode="numeric" autocomplete="cc-csc" placeholder="123" maxlength="4" />
              </label>
            </div>
          } @else {
            <label class="campo">
              <span>Celular registrado en Yape</span>
              <input #primerCampo name="celular" [(ngModel)]="celular" inputmode="numeric" placeholder="900 000 001" maxlength="11" />
            </label>
            <label class="campo">
              <span>Código de aprobación</span>
              <input name="codigo" [(ngModel)]="codigo" inputmode="numeric" placeholder="6 dígitos" maxlength="6" />
              <small class="bloque">Lo encuentras en tu app Yape, menú "Código de aprobación".</small>
            </label>
          }

          @if (error()) { <p class="alerta alerta--error" role="alert">{{ error() }}</p> }

          <button type="submit" class="boton boton--ancho" [disabled]="procesando()">
            {{ procesando() ? 'Procesando…' : 'Pagar ' + (s.monto | soles) }}
          </button>
        </form>

        <details class="pago__ayuda">
          <summary>Datos de prueba</summary>
          <p>Tarjeta aprobada: 4111 1111 1111 1111, 09/30, CVV 123.<br />
             Tarjeta rechazada: 4000 0400 0000 0008 (fondos insuficientes).<br />
             Yape: cualquier celular de 9 dígitos y cualquier código, excepto 000000 (código vencido).</p>
        </details>
      </div>
    }
  `,
})
export class PagoSimuladoComponent {
  servicio = inject(PagoSimuladoService);
  private primerCampo = viewChild<ElementRef<HTMLInputElement>>('primerCampo');

  medio = signal<'tarjeta' | 'yape'>('tarjeta');
  procesando = signal(false);
  error = signal<string | null>(null);

  numero = '';
  vencimiento = '';
  cvv = '';
  celular = '';
  codigo = '';

  constructor() {
    // Al abrir, limpiamos el formulario y llevamos el foco al primer campo.
    effect(() => {
      if (this.servicio.solicitud()) {
        this.reiniciar();
        setTimeout(() => this.primerCampo()?.nativeElement.focus());
      }
    });
  }

  elegir(m: 'tarjeta' | 'yape'): void {
    this.medio.set(m);
    this.error.set(null);
    setTimeout(() => this.primerCampo()?.nativeElement.focus());
  }

  /**
   * Formatea mientras se escribe sin perder dígitos: tomamos el texto del campo,
   * dejamos solo números y lo reescribimos con espacios cada 4.
   */
  alEscribirTarjeta(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    this.numero = this.formatearTarjeta(input.value);
    input.value = this.numero;
  }

  alEscribirVencimiento(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    this.vencimiento = this.formatearVencimiento(input.value);
    input.value = this.vencimiento;
  }

  formatearTarjeta(v: string): string {
    return v.replace(/\D/g, '').slice(0, 16).replace(/(\d{4})(?=\d)/g, '$1 ');
  }

  formatearVencimiento(v: string): string {
    const d = v.replace(/\D/g, '').slice(0, 4);
    return d.length > 2 ? `${d.slice(0, 2)}/${d.slice(2)}` : d;
  }

  pagar(): void {
    const problema = this.medio() === 'tarjeta' ? this.validarTarjeta() : this.validarYape();
    if (problema) {
      this.error.set(problema);
      return;
    }
    this.error.set(null);
    this.procesando.set(true);

    // Simulamos la espera de la pasarela
    setTimeout(() => {
      this.procesando.set(false);
      const rechazo = this.rechazoSimulado();
      if (rechazo) {
        this.error.set(rechazo);
        return;
      }
      this.servicio.aprobar(this.medio());
    }, 1200);
  }

  cerrar(): void {
    if (!this.procesando()) this.servicio.cancelar();
  }

  private validarTarjeta(): string | null {
    const n = this.numero.replace(/\s/g, '');
    if (n.length < 15) return `Al número de tarjeta le faltan dígitos (tiene ${n.length}).`;
    if (!luhnValido(n)) return 'El número de tarjeta no es válido. Revisa los dígitos.';
    const m = this.vencimiento.match(/^(\d{2})\/(\d{2})$/);
    if (!m || +m[1] < 1 || +m[1] > 12) return 'Ingresa el vencimiento como MM/AA.';
    const fin = new Date(2000 + +m[2], +m[1], 0, 23, 59);
    if (fin < new Date()) return 'La tarjeta está vencida.';
    if (!/^\d{3,4}$/.test(this.cvv)) return 'El CVV debe tener 3 o 4 dígitos.';
    return null;
  }

  private validarYape(): string | null {
    if (!/^9\d{8}$/.test(this.celular.replace(/\s/g, ''))) return 'Ingresa un celular de 9 dígitos que empiece con 9.';
    if (!/^\d{6}$/.test(this.codigo)) return 'El código de aprobación tiene 6 dígitos.';
    return null;
  }

  private rechazoSimulado(): string | null {
    if (this.medio() === 'tarjeta' && this.numero.replace(/\s/g, '') === '4000040000000008') {
      return 'Pago rechazado: fondos insuficientes. Prueba con otra tarjeta.';
    }
    if (this.medio() === 'yape' && this.codigo === '000000') {
      return 'El código de aprobación venció. Genera uno nuevo en tu app Yape.';
    }
    return null;
  }

  private reiniciar(): void {
    this.medio.set('tarjeta');
    this.error.set(null);
    this.procesando.set(false);
    this.numero = this.vencimiento = this.cvv = this.celular = this.codigo = '';
  }
}
