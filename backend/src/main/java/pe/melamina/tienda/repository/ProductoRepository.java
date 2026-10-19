package pe.melamina.tienda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.melamina.tienda.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {
    boolean existsByCategoriaId(Long categoriaId);
    boolean existsBySkuIgnoreCase(String sku);
}
