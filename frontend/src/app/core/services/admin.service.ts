import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Categoria, EstadoPedido, FiltrosProducto, Pagina, Pedido, Producto, ProductoRequest, Recibo, Rol, TipoProducto, Usuario } from '../models/modelos';
import { aParams } from './catalogo.service';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/api/admin`;

  // Productos
  productos(filtros: FiltrosProducto): Observable<Pagina<Producto>> {
    return this.http.get<Pagina<Producto>>(`${this.api}/productos`, { params: aParams(filtros) });
  }
  producto(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.api}/productos/${id}`);
  }
  crearProducto(p: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(`${this.api}/productos`, p);
  }
  actualizarProducto(id: number, p: ProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(`${this.api}/productos/${id}`, p);
  }
  eliminarProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/productos/${id}`);
  }

  // Categorías (el listado sale del endpoint público)
  crearCategoria(c: { nombre: string; descripcion: string; tipo: TipoProducto }): Observable<Categoria> {
    return this.http.post<Categoria>(`${this.api}/categorias`, c);
  }
  actualizarCategoria(id: number, c: { nombre: string; descripcion: string; tipo: TipoProducto }): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.api}/categorias/${id}`, c);
  }
  eliminarCategoria(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/categorias/${id}`);
  }

  // Pedidos
  pedidos(estado: EstadoPedido | null, page: number): Observable<Pagina<Pedido>> {
    return this.http.get<Pagina<Pedido>>(`${this.api}/pedidos`, { params: aParams({ estado, page }) });
  }
  cambiarEstado(id: number, estado: EstadoPedido, referenciaPago?: string): Observable<Pedido> {
    return this.http.patch<Pedido>(`${this.api}/pedidos/${id}/estado`, { estado, referenciaPago });
  }

  // Recibos (ADMIN y VENTAS)
  recibos(q: string, page: number): Observable<Pagina<Recibo>> {
    return this.http.get<Pagina<Recibo>>(`${this.api}/recibos`, { params: aParams({ q, page }) });
  }
  recibo(id: number): Observable<Recibo> {
    return this.http.get<Recibo>(`${this.api}/recibos/${id}`);
  }

  // Usuarios (solo ADMIN)
  usuarios(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.api}/usuarios`);
  }
  crearUsuario(u: { email: string; nombre: string; rol: Rol; password: string }): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.api}/usuarios`, u);
  }
  actualizarUsuario(id: number, u: { nombre: string; rol: Rol; activo: boolean; password?: string | null }): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.api}/usuarios/${id}`, u);
  }

  // Mi cuenta (cualquier rol)
  cambiarPassword(actual: string, nueva: string): Observable<void> {
    return this.http.put<void>(`${this.api}/perfil/password`, { actual, nueva });
  }

  // Imágenes
  subirImagen(archivo: File): Observable<{ url: string }> {
    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.http.post<{ url: string }>(`${this.api}/uploads`, datos);
  }
}
