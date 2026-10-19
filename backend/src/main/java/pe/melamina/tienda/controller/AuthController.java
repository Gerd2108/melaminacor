package pe.melamina.tienda.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import pe.melamina.tienda.dto.AuthDto;
import pe.melamina.tienda.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthDto.LoginResponse login(@Valid @RequestBody AuthDto.LoginRequest req) {
        return authService.login(req);
    }
}
