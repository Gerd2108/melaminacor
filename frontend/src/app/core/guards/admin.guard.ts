import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { PERMISOS } from '../models/modelos';
import { AuthService } from '../services/auth.service';

/** Exige haber iniciado sesión con cualquier usuario del personal. */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.autenticado() ? true : inject(Router).createUrlTree(['/admin/login']);
};

/** Exige que el rol del usuario tenga acceso a la sección indicada. */
export const permisoGuard =
  (seccion: keyof typeof PERMISOS): CanActivateFn =>
  () => {
    const auth = inject(AuthService);
    if (auth.puede(seccion)) return true;
    return inject(Router).createUrlTree([auth.autenticado() ? auth.rutaInicio() : '/admin/login']);
  };
