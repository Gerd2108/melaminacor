package pe.melamina.tienda.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.melamina.tienda.dto.CategoriaDto;
import pe.melamina.tienda.service.CategoriaService;

@RestController
@RequestMapping("/api/admin/categorias")
public class AdminCategoriaController {

    private final CategoriaService categoriaService;

    public AdminCategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaDto.Response crear(@Valid @RequestBody CategoriaDto.Request req) {
        return categoriaService.crear(req);
    }

    @PutMapping("/{id}")
    public CategoriaDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDto.Request req) {
        return categoriaService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
    }
}
