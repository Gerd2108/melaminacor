import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ROLES, Rol, Usuario, mensajeError } from '../../core/models/modelos';
import { AdminService } from '../../core/services/admin.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-usuarios',
  imports: [ReactiveFormsModule],
  templateUrl: './usuarios.component.html',
})
export class AdminUsuariosComponent {
  private fb = inject(FormBuilder);
  private admin = inject(AdminService);
  auth = inject(AuthService);

  roles = Object.entries(ROLES) as [Rol, { nombre: string; descripcion: string }][];
  nombresRol = ROLES;
  usuarios = signal<Usuario[]>([]);
  editando = signal<Usuario | null>(null);
  error = signal<string | null>(null);
  aviso = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    rol: ['VENTAS' as Rol, Validators.required],
    activo: [true],
    // Obligatoria al crear; opcional al editar (si se llena, se resetea)
    password: ['', [Validators.minLength(8)]],
  });

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.admin.usuarios().subscribe({
      next: (u) => this.usuarios.set(u),
      error: (e) => this.error.set(mensajeError(e)),
    });
  }

  esUnoMismo(u: Usuario): boolean {
    return u.email.toLowerCase() === this.auth.sesion()?.email.toLowerCase();
  }

  editar(u: Usuario): void {
    this.editando.set(u);
    this.aviso.set(null);
    this.form.reset({ nombre: u.nombre, email: u.email, rol: u.rol, activo: u.activo, password: '' });
    this.form.controls.email.disable();
  }

  cancelar(): void {
    this.editando.set(null);
    this.form.reset({ nombre: '', email: '', rol: 'VENTAS', activo: true, password: '' });
    this.form.controls.email.enable();
  }

  guardar(): void {
    const v = this.form.getRawValue();
    const actual = this.editando();
    if (this.form.invalid || (!actual && v.password.length < 8)) {
      this.form.markAllAsTouched();
      this.error.set(!actual && v.password.length < 8 ? 'La contraseña inicial debe tener al menos 8 caracteres.' : null);
      return;
    }
    this.error.set(null);

    const peticion = actual
      ? this.admin.actualizarUsuario(actual.id, { nombre: v.nombre, rol: v.rol, activo: v.activo, password: v.password || null })
      : this.admin.crearUsuario({ nombre: v.nombre, email: v.email, rol: v.rol, password: v.password });

    peticion.subscribe({
      next: (u) => {
        this.aviso.set(actual ? `Cambios guardados para ${u.nombre}.` : `Usuario ${u.email} creado. Compártele su contraseña inicial.`);
        this.cancelar();
        this.cargar();
      },
      error: (e) => this.error.set(mensajeError(e)),
    });
  }
}
