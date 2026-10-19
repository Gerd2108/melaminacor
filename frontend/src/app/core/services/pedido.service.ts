import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Pedido, PedidoRequest, Recibo } from '../models/modelos';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/api/public`;

  crear(pedido: PedidoRequest): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.api}/pedidos`, pedido);
  }

  consultar(codigo: string, email: string): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.api}/pedidos/${codigo}`, { params: { email } });
  }

  recibo(codigo: string, email: string): Observable<Recibo> {
    return this.http.get<Recibo>(`${this.api}/pedidos/${codigo}/recibo`, { params: { email } });
  }

  pagarConCulqi(pedidoCodigo: string, token: string, email: string): Observable<Pedido> {
    return this.http.post<Pedido>(`${this.api}/pagos/culqi`, { pedidoCodigo, token, email });
  }
}
