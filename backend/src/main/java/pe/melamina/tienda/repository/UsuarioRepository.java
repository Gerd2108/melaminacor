package pe.melamina.tienda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.melamina.tienda.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
}
