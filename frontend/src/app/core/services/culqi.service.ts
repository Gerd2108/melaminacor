import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PagoSimuladoService } from './pago-simulado.service';

declare global {
  interface Window {
    CulqiCheckout?: any;
  }
}

/** Yape vía Culqi acepta pagos de hasta S/ 2,000. */
export const LIMITE_YAPE = 2000;

/**
 * Pasarela Culqi (Checkout Custom): una sola ventana para pagar con tarjeta o con Yape.
 * Devuelve un token que el backend usa para crear el cargo con la llave secreta.
 * Documentación: https://docs.culqi.com/es/documentacion/checkout/checkout-custom
 */
@Injectable({ providedIn: 'root' })
export class CulqiService {
  private cargado?: Promise<any>;
  private simulado = inject(PagoSimuladoService);

  /** Sin llave pública real usamos la ventana de prueba de la tienda. */
  readonly modoPrueba = !environment.culqiPublicKey || environment.culqiPublicKey.includes('REEMPLAZAR');

  private cargarScript(): Promise<any> {
    this.cargado ??= new Promise((resolve, reject) => {
      if (window.CulqiCheckout) return resolve(window.CulqiCheckout);
      const script = document.createElement('script');
      script.src = 'https://js.culqi.com/checkout-js';
      script.onload = () =>
        window.CulqiCheckout ? resolve(window.CulqiCheckout) : reject(new Error('No se pudo iniciar la pasarela de pago.'));
      script.onerror = () => {
        this.cargado = undefined;
        reject(new Error('No se pudo cargar la pasarela de pago. Revisa tu conexión.'));
      };
      document.body.appendChild(script);
    });
    return this.cargado;
  }

  /** Abre la ventana de Culqi y resuelve con el token de la tarjeta o de Yape. */
  async obtenerToken(montoSoles: number, descripcion: string, email: string): Promise<string> {
    if (this.modoPrueba) {
      return this.simulado.abrir(montoSoles, descripcion, montoSoles <= LIMITE_YAPE);
    }
    const CulqiCheckout = await this.cargarScript();

    const paymentMethods = {
      tarjeta: true,
      yape: montoSoles <= LIMITE_YAPE,
    };

    const config = {
      settings: {
        title: environment.nombreEmpresa,
        currency: 'PEN',
        amount: Math.round(montoSoles * 100), // en céntimos; Yape lo necesita
        description: descripcion,
      },
      client: { email },
      options: {
        lang: 'auto',
        installments: false,
        modal: true,
        paymentMethods,
        paymentMethodsSort: Object.keys(paymentMethods),
      },
      appearance: {
        theme: 'default',
        menuType: 'sidebar',
        buttonCardPayText: 'Pagar',
        hiddenCulqiLogo: false,
        defaultStyle: {
          bannerColor: '#1E5B56',
          buttonBackground: '#1E5B56',
          menuColor: '#1E5B56',
          linksColor: '#1E5B56',
          buttonTextColor: '#FFFFFF',
          priceColor: '#1E5B56',
        },
      },
    };

    const culqi = new CulqiCheckout(environment.culqiPublicKey, config);

    return new Promise((resolve, reject) => {
      // Culqi llama a esta función cuando genera el token o cuando ocurre un error.
      culqi.culqi = () => {
        if (culqi.token) {
          const id = culqi.token.id;
          culqi.close();
          resolve(id);
        } else if (culqi.error) {
          culqi.close();
          reject(new Error(culqi.error.user_message ?? 'No se pudo procesar el pago.'));
        }
      };
      culqi.open();
    });
  }
}
