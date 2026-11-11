package pe.melamina.tienda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.melamina.tienda.entity.Imagen;

public interface ImagenRepository extends JpaRepository<Imagen, String> {
}
