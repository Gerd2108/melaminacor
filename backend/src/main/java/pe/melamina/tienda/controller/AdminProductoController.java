package pe.melamina.tienda.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.melamina.tienda.dto.PageResponse;
import pe.melamina.tienda.dto.ProductoDto;
import pe.melamina.tienda.entity.TipoProducto;
import pe.melamina.tienda.service.ProductoService;

@RestController
@RequestMapping("/api/admin/productos")
public class AdminProductoController {

    private final ProductoService productoService;

    public AdminProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public PageResponse<ProductoDto.Response> listar(
            @RequestParam(required = false) TipoProducto tipo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "creadoEn", direction = Sort.Direction.DESC) Pageable pageable) {
        return productoService.buscar(tipo, categoriaId, q, null, false, pageable);
    }

    @GetMapping("/{id}")
    public ProductoDto.Response obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoDto.Response crear(@Valid @RequestBody ProductoDto.Request req) {
        return productoService.crear(req);
    }

    @PutMapping("/{id}")
    public ProductoDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDto.Request req) {
        return productoService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}
