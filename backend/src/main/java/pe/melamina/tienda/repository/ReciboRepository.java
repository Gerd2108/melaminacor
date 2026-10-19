package pe.melamina.tienda.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.melamina.tienda.entity.Recibo;

import java.util.Optional;

public interface ReciboRepository extends JpaRepository<Recibo, Long> {

    Optional<Recibo> findByPedidoId(Long pedidoId);

    @Query("""
            select r from Recibo r
            where (:q = ''
                   or lower(r.numero) like lower(concat('%', :q, '%'))
                   or lower(r.clienteNombre) like lower(concat('%', :q, '%'))
                   or r.clienteDocumento like concat('%', :q, '%'))
            order by r.fechaEmision desc
            """)
    Page<Recibo> buscar(@Param("q") String q, Pageable pageable);
}
