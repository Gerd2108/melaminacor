package pe.melamina.tienda.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.melamina.tienda.dto.UsuarioDto;
import pe.melamina.tienda.entity.Rol;
import pe.melamina.tienda.entity.Usuario;
import pe.melamina.tienda.exception.NegocioException;
import pe.melamina.tienda.exception.RecursoNoEncontradoException;
import pe.melamina.tienda.repository.UsuarioRepository;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto.Response> listar() {
        return usuarioRepository.findAll().stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(UsuarioDto.Response::from).toList();
    }

    @Transactional
    public UsuarioDto.Response crear(UsuarioDto.Crear req) {
        if (usuarioRepository.findByEmailIgnoreCase(req.email()).isPresent()) {
            throw new NegocioException("Ya existe un usuario con el correo " + req.email());
        }
        Usuario u = new Usuario();
        u.setEmail(req.email().trim().toLowerCase());
        u.setNombre(req.nombre().trim());
        u.setRol(req.rol().name());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setActivo(true);
        return UsuarioDto.Response.from(usuarioRepository.save(u));
    }

    /** emailActual: quien hace el cambio, para que el admin no se bloquee a sí mismo. */
    @Transactional
    public UsuarioDto.Response actualizar(Long id, UsuarioDto.Actualizar req, String emailActual) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        boolean esUnoMismo = u.getEmail().equalsIgnoreCase(emailActual);
        if (esUnoMismo && (!req.activo() || req.rol() != Rol.ADMIN)) {
            throw new NegocioException("No puedes desactivarte ni quitarte el rol de administrador a ti mismo");
        }
        u.setNombre(req.nombre().trim());
        u.setRol(req.rol().name());
        u.setActivo(req.activo());
        if (req.password() != null && !req.password().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.password()));
        }
        return UsuarioDto.Response.from(u);
    }

    @Transactional
    public void cambiarPassword(String email, UsuarioDto.CambioPassword req) {
        Usuario u = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (!passwordEncoder.matches(req.actual(), u.getPassword())) {
            throw new NegocioException("La contraseña actual no es correcta");
        }
        u.setPassword(passwordEncoder.encode(req.nueva()));
    }
}
