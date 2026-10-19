import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PERMISOS, Rol } from '../models/modelos';

interface Sesion {
  token: string;
  nombre: string;
  email: string;
  rol: Rol;
}

const CLAVE = 'melamina_sesion';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  readonly sesion = signal<Sesion | null>(this.leer());
  readonly autenticado = computed(() => {
    const s = this.sesion();
    return !!s && !this.expirado(s.token);
  });

  readonly rol = computed(() => (this.autenticado() ? this.sesion()!.rol : null));

  /** ¿El usuario actual tiene alguno de estos roles? */
  tiene(roles: Rol[]): boolean {
    const r = this.rol();
    return !!r && roles.includes(r);
  }

  puede(seccion: keyof typeof PERMISOS): boolean {
    return this.tiene(PERMISOS[seccion]);
  }

  /** Primera pantalla del panel según el rol. */
  rutaInicio(): string {
    return this.puede('productos') ? '/admin/productos' : '/admin/pedidos';
  }

  login(email: string, password: string): Observable<Sesion> {
    return this.http.post<Sesion>(`${environment.apiUrl}/api/auth/login`, { email, password }).pipe(
      tap((s) => {
        this.sesion.set(s);
        try {
          localStorage.setItem(CLAVE, JSON.stringify(s));
        } catch {
          /* sin almacenamiento: la sesión dura hasta recargar */
        }
      }),
    );
  }

  /** Cierra la sesión. Desde la tienda se queda en la página actual; desde el panel va al login. */
  logout(destino: string | null = '/admin/login'): void {
    this.sesion.set(null);
    try {
      localStorage.removeItem(CLAVE);
    } catch {}
    if (destino) this.router.navigateByUrl(destino);
  }

  token(): string | null {
    return this.sesion()?.token ?? null;
  }

  /** Lee la fecha de expiración del JWT sin librerías. */
  private expirado(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }

  private leer(): Sesion | null {
    try {
      return JSON.parse(localStorage.getItem(CLAVE) ?? 'null');
    } catch {
      return null;
    }
  }
}
