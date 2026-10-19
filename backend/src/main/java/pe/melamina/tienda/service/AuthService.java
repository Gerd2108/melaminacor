package pe.melamina.tienda.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.melamina.tienda.dto.AuthDto;
import pe.melamina.tienda.entity.Usuario;
import pe.melamina.tienda.repository.UsuarioRepository;
import pe.melamina.tienda.security.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthDto.LoginResponse login(AuthDto.LoginRequest req) {
        Usuario u = usuarioRepository.findByEmailIgnoreCase(req.email())
                .filter(Usuario::isActivo)
                .filter(x -> passwordEncoder.matches(req.password(), x.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("credenciales"));
        return new AuthDto.LoginResponse(jwtService.generarToken(u), u.getNombre(), u.getEmail(), u.getRol());
    }
}
