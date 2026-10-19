package pe.melamina.tienda.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.melamina.tienda.dto.PageResponse;
import pe.melamina.tienda.dto.ReciboDto;
import pe.melamina.tienda.entity.Pedido;
import pe.melamina.tienda.entity.Recibo;
import pe.melamina.tienda.exception.RecursoNoEncontradoException;
import pe.melamina.tienda.repository.PedidoRepository;
import pe.melamina.tienda.repository.ReciboRepository;

import java.time.LocalDateTime;

@Service
public class ReciboService {

    private static final Logger log = LoggerFactory.getLogger(ReciboService.class);
    private static final String SERIE = "R001";

    private final ReciboRepository reciboRepository;
    private final PedidoRepository pedidoRepository;

    @Value("${app.empresa.razon-social}") private String razonSocial;
    @Value("${app.empresa.ruc}") private String ruc;
    @Value("${app.empresa.direccion}") private String direccion;
    @Value("${app.empresa.telefono}") private String telefono;

    public ReciboService(ReciboRepository reciboRepository, PedidoRepository pedidoRepository) {
        this.reciboRepository = reciboRepository;
        this.pedidoRepository = pedidoRepository;
    }

    /** Emite el recibo del pedido. Si ya existe, devuelve el mismo (nunca duplica). */
    @Transactional
    public Recibo emitir(Pedido p) {
        return reciboRepository.findByPedidoId(p.getId()).orElseGet(() -> {
            Recibo r = new Recibo();
            r.setPedido(p);
            r.setFechaEmision(LocalDateTime.now());
            r.setEstado(Recibo.EMITIDO);
            r.setEmisorRazonSocial(razonSocial);
            r.setEmisorRuc(ruc);
            r.setEmisorDireccion(direccion);
            r.setEmisorTelefono(telefono);
            r.setClienteNombre(p.getNombreCliente());
            r.setClienteDocumentoTipo(p.getDocumento().length() == 11 ? "RUC" : "DNI");
            r.setClienteDocumento(p.getDocumento());
            r.setClienteDireccion(p.getDireccion() + ", " + p.getDistrito());
            r.setClienteEmail(p.getEmail());
            r.setSubtotal(p.getSubtotal());
            r.setIgv(p.getIgv());
            r.setTotal(p.getTotal());
            r.setMetodoPago(p.getMetodoPago().name());
            r.setReferenciaPago(p.getReferenciaPago());
            r = reciboRepository.saveAndFlush(r);           // obtenemos el id...
            r.setNumero(String.format("%s-%08d", SERIE, r.getId())); // ...y armamos el correlativo
            p.setRecibo(r);
            log.info("Recibo {} emitido para el pedido {}", r.getNumero(), p.getCodigo());
            return r;
        });
    }

    /** Al cancelar un pedido pagado, su recibo queda ANULADO (no se borra). */
    @Transactional
    public void anular(Pedido p) {
        reciboRepository.findByPedidoId(p.getId()).ifPresent(r -> {
            if (Recibo.EMITIDO.equals(r.getEstado())) {
                r.setEstado(Recibo.ANULADO);
                r.setFechaAnulacion(LocalDateTime.now());
                log.info("Recibo {} anulado por cancelación del pedido {}", r.getNumero(), p.getCodigo());
            }
        });
    }

    @Transactional(readOnly = true)
    public PageResponse<ReciboDto.Response> listar(String q, Pageable pageable) {
        String filtro = q == null ? "" : q.trim();
        return PageResponse.of(reciboRepository.buscar(filtro, pageable).map(ReciboDto.Response::from));
    }

    @Transactional(readOnly = true)
    public ReciboDto.Response obtener(Long id) {
        return ReciboDto.Response.from(reciboRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Recibo no encontrado")));
    }

    /** Consulta del cliente: necesita el código del pedido y su correo. */
    @Transactional(readOnly = true)
    public ReciboDto.Response obtenerPublico(String codigo, String email) {
        Pedido p = pedidoRepository.findByCodigo(codigo)
                .filter(x -> x.getEmail().equalsIgnoreCase(email == null ? "" : email.trim()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
        Recibo r = reciboRepository.findByPedidoId(p.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Este pedido aún no tiene recibo: se emite al confirmar el pago"));
        return ReciboDto.Response.from(r);
    }
}
