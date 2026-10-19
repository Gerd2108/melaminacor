import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { mensajeError } from '../../core/models/modelos';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="login">
      <form [formGroup]="form" (ngSubmit)="entrar()" class="login__caja" novalidate>
        <p class="marca"><span class="marca__canto" aria-hidden="true"></span>{{ empresa }}</p>
        <h1>Acceso del personal</h1>
        <label class="campo">
          <span>Correo</span>
          <input formControlName="email" type="email" autocomplete="username" />
        </label>
        <label class="campo">
          <span>Contraseña</span>
          <input formControlName="password" type="password" autocomplete="current-password" />
        </label>
        @if (error()) { <p class="alerta alerta--error" role="alert">{{ error() }}</p> }
        <button class="boton boton--ancho" type="submit" [disabled]="form.invalid || cargando()">
          {{ cargando() ? 'Ingresando…' : 'Ingresar' }}
        </button>
        <a routerLink="/" class="enlace login__volver">Volver a la tienda</a>
      </form>
    </div>
  `,
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  empresa = environment.nombreEmpresa;
  cargando = signal(false);
  error = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  constructor() {
    // Si ya hay sesión abierta, vamos directo al panel.
    if (this.auth.autenticado()) this.router.navigateByUrl(this.auth.rutaInicio());
  }

  entrar(): void {
    const { email, password } = this.form.getRawValue();
    this.cargando.set(true);
    this.error.set(null);
    this.auth.login(email, password).subscribe({
      next: () => this.router.navigateByUrl(this.auth.rutaInicio()),
      error: (e) => {
        this.error.set(mensajeError(e, 'Correo o contraseña incorrectos'));
        this.cargando.set(false);
      },
    });
  }
}
