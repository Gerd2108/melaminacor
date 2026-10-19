import { Injectable, signal } from '@angular/core';

interface Solicitud {
  monto: number;
  descripcion: string;
  permiteYape: boolean;
  resolver: (token: string) => void;
  rechazar: (error: Error) => void;
}

/** Error especial: el cliente cerró la ventana. No se muestra como fallo. */
export class PagoCancelado extends Error {
  constructor() {
    super('Pago cancelado por el cliente');
    this.name = 'PagoCancelado';
  }
}

/**
 * Pasarela de PRUEBA: abre una ventana propia de la tienda (componente PagoSimuladoComponent)
 * mientras no haya llaves de Culqi configuradas. No cobra dinero.
 */
@Injectable({ providedIn: 'root' })
export class PagoSimuladoService {
  readonly solicitud = signal<Solicitud | null>(null);

  abrir(monto: number, descripcion: string, permiteYape: boolean): Promise<string> {
    return new Promise((resolver, rechazar) => {
      this.solicitud.set({ monto, descripcion, permiteYape, resolver, rechazar });
    });
  }

  aprobar(tipo: 'tarjeta' | 'yape'): void {
    const s = this.solicitud();
    this.solicitud.set(null);
    s?.resolver(`sim_${tipo}_${Math.random().toString(36).slice(2, 12)}`);
  }

  cancelar(): void {
    const s = this.solicitud();
    this.solicitud.set(null);
    s?.rechazar(new PagoCancelado());
  }
}
