package pe.melamina.tienda.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.melamina.tienda.dto.UsuarioDto;
import pe.melamina.tienda.service.UsuarioService;

/** Acciones sobre la propia cuenta, disponibles para cualquier usuario del personal. */
@RestController
@RequestMapping("/api/admin/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@Valid @RequestBody UsuarioDto.CambioPassword req, Authentication auth) {
        usuarioService.cambiarPassword(auth.getName(), req);
    }
}
