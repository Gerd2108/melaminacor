package pe.melamina.tienda.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.melamina.tienda.entity.EstadoPedido;
import pe.melamina.tienda.repository.PedidoRepository;
import pe.melamina.tienda.service.ReciboService;

import java.util.EnumSet;

/** Al arrancar, emite el recibo de los pedidos pagados que aún no lo tengan (por ejemplo, los anteriores a este módulo). */
@Component
@Order(3)
public class ReciboInitializer implements CommandLineRunner {

    private final PedidoRepository pedidoRepository;
    private final ReciboService reciboService;

    public ReciboInitializer(PedidoRepository pedidoRepository, ReciboService reciboService) {
        this.pedidoRepository = pedidoRepository;
        this.reciboService = reciboService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        pedidoRepository.findByEstadoIn(EnumSet.of(
                        EstadoPedido.PAGADO, EstadoPedido.EN_PREPARACION, EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO))
                .stream()
                .sorted((a, b) -> a.getFechaCreacion().compareTo(b.getFechaCreacion()))
                .forEach(reciboService::emitir);
    }
}
