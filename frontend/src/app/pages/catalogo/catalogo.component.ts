import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, map, of, switchMap, tap } from 'rxjs';
import { FiltrosProducto, Pagina, Producto, TIPOS, TipoProducto, mensajeError } from '../../core/models/modelos';
import { CatalogoService } from '../../core/services/catalogo.service';
import { ProductoCardComponent } from '../../shared/producto-card.component';

@Component({
  selector: 'app-catalogo',
  imports: [FormsModule, ProductoCardComponent],
  templateUrl: './catalogo.component.html',
})
export class CatalogoComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private catalogo = inject(CatalogoService);

  tipos = TIPOS;
  orden = [
    { valor: 'creadoEn,desc', nombre: 'Más recientes' },
    { valor: 'precio,asc', nombre: 'Menor precio' },
    { valor: 'precio,desc', nombre: 'Mayor precio' },
    { valor: 'nombre,asc', nombre: 'Nombre A-Z' },
  ];

  filtros = signal<FiltrosProducto>({});
  resultado = signal<Pagina<Producto> | null>(null);
  cargando = signal(true);
  error = signal<string | null>(null);
  busqueda = '';

  private categorias = toSignal(this.catalogo.categorias(), { initialValue: [] });
  categoriasDelTipo = computed(() => {
    const tipo = this.filtros().tipo;
    return tipo ? this.categorias().filter((c) => c.tipo === tipo) : this.categorias();
  });
  titulo = computed(() => TIPOS.find((t) => t.valor === this.filtros().tipo)?.nombre ?? 'Catálogo');

  constructor() {
    // La URL es la fuente de verdad: /catalogo?tipo=PLANCHA&q=roble&page=1
    this.route.queryParamMap
      .pipe(
        map((q) => ({
          tipo: (q.get('tipo') as TipoProducto) || null,
          categoriaId: q.get('categoriaId') ? Number(q.get('categoriaId')) : null,
          q: q.get('q'),
          sort: q.get('sort') || 'creadoEn,desc',
          page: Number(q.get('page') ?? 0),
          size: 12,
        })),
        tap((f) => {
          this.filtros.set(f);
          this.busqueda = f.q ?? '';
          this.cargando.set(true);
          this.error.set(null);
        }),
        switchMap((f) =>
          this.catalogo.productos(f).pipe(
            catchError((e) => {
              this.error.set(mensajeError(e, 'No se pudo cargar el catálogo'));
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((r) => {
        this.resultado.set(r);
        this.cargando.set(false);
      });
  }

  /** Cambia filtros en la URL; cualquier cambio vuelve a la página 0. */
  aplicar(cambios: Record<string, unknown>): void {
    const limpio = Object.fromEntries(Object.entries(cambios).map(([k, v]) => [k, v === '' ? null : v]));
    this.router.navigate([], { queryParams: { page: null, ...limpio }, queryParamsHandling: 'merge' });
  }

  cambiarTipo(tipo: TipoProducto | null): void {
    this.aplicar({ tipo, categoriaId: null });
  }

  irAPagina(page: number): void {
    this.router.navigate([], { queryParams: { page: page || null }, queryParamsHandling: 'merge' });
  }

  limpiar(): void {
    this.router.navigate([], { queryParams: {} });
  }
}
