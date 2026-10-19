package pe.melamina.tienda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.melamina.tienda.entity.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByOrderByTipoAscNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}
