import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { environment } from '../../environments/environment';
import { TIPOS } from '../core/models/modelos';
import { AuthService } from '../core/services/auth.service';
import { CarritoService } from '../core/services/carrito.service';
import { WhatsappService } from '../core/services/whatsapp.service';
import { PagoSimuladoComponent } from '../shared/pago-simulado.component';

@Component({
  selector: 'app-public-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, PagoSimuladoComponent],
  templateUrl: './public-layout.component.html',
})
export class PublicLayoutComponent {
  carrito = inject(CarritoService);
  auth = inject(AuthService);
  whatsapp = inject(WhatsappService);
  empresa = environment.nombreEmpresa;
  direccion = environment.direccion;
  horario = environment.horario;
  /** 51987654321 -> "987 654 321" para mostrarlo legible. */
  telefono = environment.whatsapp.replace(/^51/, '').replace(/(\d{3})(\d{3})(\d{3})/, '$1 $2 $3');
  muestras = ['blanco', 'grafito', 'cedro', 'nogal', 'roble'];
  tipos = TIPOS;
  anio = new Date().getFullYear();
  menuAbierto = false;
}
