import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

/** Agrega el token a las llamadas del panel admin y cierra sesión si el backend responde 401. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const esAdmin = req.url.startsWith(`${environment.apiUrl}/api/admin`);
  const token = auth.token();

  const peticion = esAdmin && token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(peticion).pipe(
    catchError((err: HttpErrorResponse) => {
      if (esAdmin && err.status === 401) auth.logout();
      return throwError(() => err);
    }),
  );
};
