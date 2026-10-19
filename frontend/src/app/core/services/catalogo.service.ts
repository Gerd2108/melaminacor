import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Categoria, FiltrosProducto, Pagina, Producto } from '../models/modelos';

/** Convierte un objeto de filtros en query params, ignorando vacíos. */
export function aParams(filtros: object): HttpParams {
  let params = new HttpParams();
  Object.entries(filtros).forEach(([k, v]) => {
    if (v !== null && v !== undefined && v !== '' && v !== false) params = params.set(k, String(v));
  });
  return params;
}

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private http = inject(HttpClient);
  private api = `${environment.apiUrl}/api/public`;

  // Las categorías casi no cambian: las pedimos una vez y las compartimos.
  private categorias$?: Observable<Categoria[]>;

  productos(filtros: FiltrosProducto = {}): Observable<Pagina<Producto>> {
    return this.http.get<Pagina<Producto>>(`${this.api}/productos`, { params: aParams(filtros) });
  }

  producto(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.api}/productos/${id}`);
  }

  categorias(): Observable<Categoria[]> {
    this.categorias$ ??= this.http.get<Categoria[]>(`${this.api}/categorias`).pipe(shareReplay(1));
    return this.categorias$;
  }

  /** Úsalo después de crear/editar categorías en el admin. */
  refrescarCategorias(): void {
    this.categorias$ = undefined;
  }
}
