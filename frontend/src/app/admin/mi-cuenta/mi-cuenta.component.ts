import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ROLES, mensajeError } from '../../core/models/modelos';
import { AdminService } from '../../core/services/admin.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-mi-cuenta',
  imports: [ReactiveFormsModule],
  template: `
    <div class="admin__cabecera"><h1>Mi cuenta</h1></div>

    <div class="formulario formulario--admin">
      <dl class="ficha">
        <dt>Nombre</dt><dd>{{ auth.sesion()?.nombre }}</dd>
        <dt>Correo</dt><dd>{{ auth.sesion()?.email }}</dd>
        <dt>Rol</dt><dd>{{ rol() }}</dd>
      </dl>

      <form [formGroup]="form" (ngSubmit)="guardar()" novalidate>
        <fieldset>
          <legend>Cambiar contraseña</legend>
          <label class="campo">
            <span>Contraseña actual</span>
            <input formControlName="actual" type="password" autocomplete="current-password" />
          </label>
          <div class="campos-2">
            <label class="campo">
              <span>Nueva contraseña</span>
              <input formControlName="nueva" type="password" autocomplete="new-password" placeholder="Mínimo 8 caracteres" />
            </label>
            <label class="campo">
              <span>Repite la nueva contraseña</span>
              <input formControlName="repetir" type="password" autocomplete="new-password" />
            </label>
          </div>
        </fieldset>
        @if (error()) { <p class="alerta alerta--error" role="alert">{{ error() }}</p> }
        @if (listo()) { <p class="alerta alerta--ok" role="status">Contraseña actualizada.</p> }
        <button type="submit" class="boton" [disabled]="form.invalid">Cambiar contraseña</button>
      </form>
    </div>
  `,
})
export class MiCuentaComponent {
  private fb = inject(FormBuilder);
  private admin = inject(AdminService);
  auth = inject(AuthService);

  rol = computed(() => {
    const r = this.auth.rol();
    return r ? ROLES[r].nombre : '';
  });
  error = signal<string | null>(null);
  listo = signal(false);

  form = this.fb.nonNullable.group({
    actual: ['', Validators.required],
    nueva: ['', [Validators.required, Validators.minLength(8)]],
    repetir: ['', Validators.required],
  });

  guardar(): void {
    const { actual, nueva, repetir } = this.form.getRawValue();
    this.listo.set(false);
    if (nueva !== repetir) {
      this.error.set('Las contraseñas nuevas no coinciden.');
      return;
    }
    this.error.set(null);
    this.admin.cambiarPassword(actual, nueva).subscribe({
      next: () => {
        this.listo.set(true);
        this.form.reset();
      },
      error: (e) => this.error.set(mensajeError(e)),
    });
  }
}
