import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ProductoRequest, TIPOS, TipoProducto, mensajeError } from '../../core/models/modelos';
import { AdminService } from '../../core/services/admin.service';
import { CatalogoService } from '../../core/services/catalogo.service';
import { ImagenPipe } from '../../shared/pipes';

@Component({
  selector: 'app-producto-form',
  imports: [ReactiveFormsModule, RouterLink, ImagenPipe],
  templateUrl: './producto-form.component.html',
})
export class ProductoFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private admin = inject(AdminService);
  private catalogo = inject(CatalogoService);
  private router = inject(Router);

  /** Viene de la ruta /admin/productos/:id (no existe en /nuevo). */
  id = input<string>();
  esEdicion = computed(() => !!this.id());

  tipos = TIPOS;
  guardando = signal(false);
  subiendo = signal(false);
  error = signal<string | null>(null);

  form = this.fb.group({
    nombre: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(150)]),
    sku: this.fb.control<string | null>(null, Validators.maxLength(50)),
    descripcion: this.fb.control<string | null>(null),
    precio: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
    precioOferta: this.fb.control<number | null>(null, Validators.min(0)),
    stock: this.fb.nonNullable.control(0, [Validators.required, Validators.min(0)]),
    tipo: this.fb.nonNullable.control<TipoProducto>('PLANCHA', Validators.required),
    categoriaId: this.fb.control<number | null>(null),
    medidas: this.fb.control<string | null>(null),
    color: this.fb.control<string | null>(null),
    espesor: this.fb.control<string | null>(null),
    imagenUrl: this.fb.control<string | null>(null),
    activo: this.fb.nonNullable.control(true),
    destacado: this.fb.nonNullable.control(false),
    soloCotizacion: this.fb.nonNullable.control(false),
  });

  private categorias = toSignal(this.catalogo.categorias(), { initialValue: [] });
  private tipoActual = toSignal(this.form.controls.tipo.valueChanges, { initialValue: this.form.controls.tipo.value });
  categoriasDelTipo = computed(() => this.categorias().filter((c) => c.tipo === this.tipoActual()));
  imagen = toSignal(this.form.controls.imagenUrl.valueChanges, { initialValue: null as string | null });

  ngOnInit(): void {
    const id = this.id();
    if (!id) return;
    this.admin.producto(Number(id)).subscribe({
      next: (p) => this.form.patchValue(p),
      error: (e) => this.error.set(mensajeError(e, 'No se encontró el producto')),
    });
  }

  /** Al cambiar de línea, limpiamos la categoría si ya no corresponde. */
  alCambiarTipo(): void {
    const cat = this.form.controls.categoriaId.value;
    if (cat && !this.categoriasDelTipo().some((c) => c.id === cat)) {
      this.form.controls.categoriaId.setValue(null);
    }
  }

  subirImagen(evento: Event): void {
    const archivo = (evento.target as HTMLInputElement).files?.[0];
    if (!archivo) return;
    this.subiendo.set(true);
    this.error.set(null);
    this.admin.subirImagen(archivo).subscribe({
      next: ({ url }) => {
        this.form.controls.imagenUrl.setValue(url);
        this.subiendo.set(false);
      },
      error: (e) => {
        this.error.set(mensajeError(e, 'No se pudo subir la imagen'));
        this.subiendo.set(false);
      },
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.guardando.set(true);
    this.error.set(null);

    const datos = this.form.getRawValue() as ProductoRequest;
    const id = this.id();
    const peticion = id ? this.admin.actualizarProducto(Number(id), datos) : this.admin.crearProducto(datos);

    peticion.subscribe({
      next: () => this.router.navigate(['/admin/productos']),
      error: (e) => {
        this.error.set(mensajeError(e));
        this.guardando.set(false);
      },
    });
  }
}
