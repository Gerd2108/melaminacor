import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Categoria, TIPOS, TipoProducto, mensajeError } from '../../core/models/modelos';
import { AdminService } from '../../core/services/admin.service';
import { CatalogoService } from '../../core/services/catalogo.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-categorias',
  imports: [ReactiveFormsModule],
  templateUrl: './categorias.component.html',
})
export class AdminCategoriasComponent {
  private fb = inject(FormBuilder);
  private admin = inject(AdminService);
  private catalogo = inject(CatalogoService);
  auth = inject(AuthService);

  tipos = TIPOS;
  categorias = signal<Categoria[]>([]);
  editando = signal<Categoria | null>(null);
  error = signal<string | null>(null);

  porTipo = computed(() =>
    TIPOS.map((t) => ({ ...t, items: this.categorias().filter((c) => c.tipo === t.valor) })),
  );

  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(80)]],
    descripcion: ['', Validators.maxLength(255)],
    tipo: ['PLANCHA' as TipoProducto, Validators.required],
  });

  constructor() {
    this.cargar();
  }

  cargar(): void {
    this.catalogo.refrescarCategorias();
    this.catalogo.categorias().subscribe({
      next: (c) => this.categorias.set(c),
      error: (e) => this.error.set(mensajeError(e)),
    });
  }

  editar(c: Categoria): void {
    this.editando.set(c);
    this.form.setValue({ nombre: c.nombre, descripcion: c.descripcion ?? '', tipo: c.tipo });
  }

  cancelar(): void {
    this.editando.set(null);
    this.form.reset();
  }

  guardar(): void {
    if (this.form.invalid) return;
    this.error.set(null);
    const datos = this.form.getRawValue();
    const actual = this.editando();
    const peticion = actual ? this.admin.actualizarCategoria(actual.id, datos) : this.admin.crearCategoria(datos);
    peticion.subscribe({
      next: () => {
        this.cancelar();
        this.cargar();
      },
      error: (e) => this.error.set(mensajeError(e)),
    });
  }

  eliminar(c: Categoria): void {
    if (!confirm(`¿Eliminar la categoría "${c.nombre}"?`)) return;
    this.admin.eliminarCategoria(c.id).subscribe({
      next: () => this.cargar(),
      error: (e) => this.error.set(mensajeError(e)),
    });
  }
}
