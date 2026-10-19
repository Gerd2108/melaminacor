package pe.melamina.tienda.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.melamina.tienda.dto.UsuarioDto;
import pe.melamina.tienda.service.UsuarioService;

import java.util.List;

/** Gestión del personal (solo ADMIN, ver SecurityConfig). */
@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioDto.Response> listar() {
        return usuarioService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto.Response crear(@Valid @RequestBody UsuarioDto.Crear req) {
        return usuarioService.crear(req);
    }

    @PutMapping("/{id}")
    public UsuarioDto.Response actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioDto.Actualizar req,
                                          Authentication auth) {
        return usuarioService.actualizar(id, req, auth.getName());
    }
}
