import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TIPOS } from '../../core/models/modelos';
import { CatalogoService } from '../../core/services/catalogo.service';
import { WhatsappService } from '../../core/services/whatsapp.service';
import { ProductoCardComponent } from '../../shared/producto-card.component';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink, ProductoCardComponent],
  templateUrl: './inicio.component.html',
})
export class InicioComponent {
  private catalogo = inject(CatalogoService);
  whatsapp = inject(WhatsappService);
  empresa = environment.nombreEmpresa;
  tipos = TIPOS;

  /** Acabados que se muestran en el muestrario del inicio. Cámbialos por los colores reales del taller. */
  acabados = [
    { nombre: 'Blanco', clase: 'lamina--blanco' },
    { nombre: 'Grafito', clase: 'lamina--grafito' },
    { nombre: 'Cedro', clase: 'lamina--cedro' },
    { nombre: 'Nogal', clase: 'lamina--nogal' },
    { nombre: 'Roble', clase: 'lamina--roble' },
  ];

  /** Proceso real del taller (según el análisis de MelaminCorp). */
  pasos = [
    { titulo: 'Trae tus medidas', texto: 'Con tu lista de piezas te asesoramos sobre el material y te damos una cotización sin costo.' },
    { titulo: 'Optimizamos el despiece', texto: 'Con Lepton Optimizer calculamos los cortes para usar la menor cantidad de planchas.' },
    { titulo: 'Confirmas con un adelanto', texto: 'Separas tu pedido con un adelanto y cancelas el saldo al recoger.' },
    { titulo: 'Cortamos y canteamos', texto: 'Tus piezas pasan por corte y canteado en el taller, con revisión de cada acabado.' },
    { titulo: 'Recoges o instalamos', texto: 'Retiras tu pedido en Ate Vitarte o lo instalamos en obra si lo solicitaste.' },
  ];

  destacados = toSignal(
    this.catalogo.productos({ destacados: true, size: 8 }).pipe(
      map((p) => p.content),
      catchError(() => of([])),
    ),
  );
}
