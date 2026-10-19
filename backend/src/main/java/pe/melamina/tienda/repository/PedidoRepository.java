package pe.melamina.tienda.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.melamina.tienda.entity.EstadoPedido;
import pe.melamina.tienda.entity.Pedido;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<Pedido> findByCodigo(String codigo);

    @EntityGraph(attributePaths = "items")
    Optional<Pedido> findWithItemsById(Long id);

    Page<Pedido> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    Page<Pedido> findByEstadoOrderByFechaCreacionDesc(EstadoPedido estado, Pageable pageable);

    boolean existsByCodigo(String codigo);

    List<Pedido> findByEstadoIn(Collection<EstadoPedido> estados);
}
