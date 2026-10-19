package pe.melamina.tienda.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import pe.melamina.tienda.dto.CategoriaDto;
import pe.melamina.tienda.dto.PageResponse;
import pe.melamina.tienda.dto.ProductoDto;
import pe.melamina.tienda.entity.TipoProducto;
import pe.melamina.tienda.service.CategoriaService;
import pe.melamina.tienda.service.ProductoService;

import java.util.List;

/** Endpoints públicos de la tienda (no requieren login). */
@RestController
@RequestMapping("/api/public")
public class CatalogoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public CatalogoController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    // GET /api/public/productos?tipo=PLANCHA&categoriaId=2&q=roble&page=0&size=12&sort=precio,asc
    @GetMapping("/productos")
    public PageResponse<ProductoDto.Response> productos(
            @RequestParam(required = false) TipoProducto tipo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean destacados,
            @PageableDefault(size = 12, sort = "creadoEn", direction = Sort.Direction.DESC) Pageable pageable) {
        return productoService.buscar(tipo, categoriaId, q, destacados, true, pageable);
    }

    @GetMapping("/productos/{id}")
    public ProductoDto.Response producto(@PathVariable Long id) {
        return productoService.obtenerPublico(id);
    }

    @GetMapping("/categorias")
    public List<CategoriaDto.Response> categorias() {
        return categoriaService.listar();
    }
}
