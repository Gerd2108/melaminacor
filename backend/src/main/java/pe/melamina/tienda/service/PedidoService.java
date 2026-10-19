package pe.melamina.tienda.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.melamina.tienda.dto.PageResponse;
import pe.melamina.tienda.dto.PedidoDto;
import pe.melamina.tienda.entity.*;
import pe.melamina.tienda.exception.NegocioException;
import pe.melamina.tienda.exception.RecursoNoEncontradoException;
import pe.melamina.tienda.repository.PedidoRepository;
import pe.melamina.tienda.repository.ProductoRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.Set;

@Service
public class PedidoService {

    private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; 
    private static final SecureRandom RANDOM = new SecureRandom();
    /** Estados en los que el pedido ya está pagado y el stock debe estar descontado. */
    private static final Set<EstadoPedido> ESTADOS_PAGADOS =
            EnumSet.of(EstadoPedido.PAGADO, EstadoPedido.EN_PREPARACION, EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO);

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final ReciboService reciboService;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository,
                         ReciboService reciboService) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.reciboService = reciboService;
    }

    @Transactional
    public PedidoDto.Response crear(PedidoDto.Request req) {
        Pedido pedido = new Pedido();
        pedido.setCodigo(generarCodigo());
        pedido.setNombreCliente(req.nombreCliente().trim());
        pedido.setEmail(req.email().trim().toLowerCase());
        pedido.setTelefono(req.telefono());
        pedido.setDocumento(req.documento());
        pedido.setDireccion(req.direccion().trim());
        pedido.setDistrito(req.distrito().trim());
        pedido.setNotas(req.notas());
        pedido.setMetodoPago(req.metodoPago());
        pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);

        BigDecimal total = BigDecimal.ZERO;
        for (PedidoDto.ItemRequest itemReq : req.items()) {
            Producto producto = productoRepository.findById(itemReq.productoId())
                    .filter(Producto::isActivo)
                    .orElseThrow(() -> new NegocioException("Un producto del carrito ya no está disponible"));

            if (producto.isSoloCotizacion()) {
                throw new NegocioException(producto.getNombre() + " se vende solo por cotización");
            }
            if (producto.getStock() < itemReq.cantidad()) {
                throw new NegocioException("Stock insuficiente para " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ")");
            }

            // El precio SIEMPRE se toma de la base de datos, nunca del frontend.
            BigDecimal precio = producto.getPrecioVigente();
            BigDecimal subtotalItem = precio.multiply(BigDecimal.valueOf(itemReq.cantidad()));
            PedidoItem item = new PedidoItem();
            item.setProducto(producto);
            item.setNombreProducto(producto.getNombre());
            item.setPrecioUnitario(precio);
            item.setCantidad(itemReq.cantidad());
            item.setSubtotal(subtotalItem);
            pedido.agregarItem(item);
            total = total.add(subtotalItem);
        }

        // Los precios incluyen IGV: separamos base imponible e IGV para la boleta/factura.
        BigDecimal base = total.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP);
        pedido.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        pedido.setSubtotal(base);
        pedido.setIgv(pedido.getTotal().subtract(base));

        return PedidoDto.Response.from(pedidoRepository.save(pedido));
    }

    /** Consulta pública: el cliente necesita el código y su correo. */
    @Transactional(readOnly = true)
    public PedidoDto.Response consultar(String codigo, String email) {
        Pedido p = pedidoRepository.findByCodigo(codigo)
                .filter(x -> x.getEmail().equalsIgnoreCase(email == null ? "" : email.trim()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
        return PedidoDto.Response.from(p);
    }

    @Transactional(readOnly = true)
    public PageResponse<PedidoDto.Response> listar(EstadoPedido estado, Pageable pageable) {
        var pagina = estado == null
                ? pedidoRepository.findAllByOrderByFechaCreacionDesc(pageable)
                : pedidoRepository.findByEstadoOrderByFechaCreacionDesc(estado, pageable);
        return PageResponse.of(pagina.map(PedidoDto.Response::from));
    }

    @Transactional(readOnly = true)
    public PedidoDto.Response obtener(Long id) {
        return PedidoDto.Response.from(buscar(id));
    }

    /** Estados que puede asignar un operario: solo el avance del trabajo, nunca pagos ni cancelaciones. */
    private static final Set<EstadoPedido> ESTADOS_OPERARIO =
            EnumSet.of(EstadoPedido.EN_PREPARACION, EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO);

    @Transactional
    public PedidoDto.Response cambiarEstado(Long id, PedidoDto.CambioEstado req, boolean soloOperario) {
        if (soloOperario && !ESTADOS_OPERARIO.contains(req.estado())) {
            throw new NegocioException("Como operario solo puedes marcar el pedido en preparación, listo/enviado o entregado");
        }
        Pedido p = buscar(id);
        if (soloOperario && !ESTADOS_PAGADOS.contains(p.getEstado())) {
            throw new NegocioException("Este pedido aún no está pagado; ventas debe confirmarlo antes de iniciar el trabajo");
        }
        if (!soloOperario && req.referenciaPago() != null && !req.referenciaPago().isBlank()) {
            p.setReferenciaPago(req.referenciaPago().trim());
        }
        aplicarEstado(p, req.estado());
        return PedidoDto.Response.from(p);
    }

    /** Usado por el pago con tarjeta y por el cambio manual de estado. */
    @Transactional
    public void aplicarEstado(Pedido p, EstadoPedido nuevo) {
        if (p.getEstado() == EstadoPedido.CANCELADO && nuevo != EstadoPedido.CANCELADO) {
            throw new NegocioException("Un pedido cancelado no puede reabrirse");
        }
        if (ESTADOS_PAGADOS.contains(nuevo) && !p.isStockDescontado()) {
            descontarStock(p);
        }
        if (nuevo == EstadoPedido.CANCELADO && p.isStockDescontado()) {
            reponerStock(p);
        }
        p.setEstado(nuevo);

        // Recibo automático: se emite al quedar pagado y se anula si se cancela
        if (ESTADOS_PAGADOS.contains(nuevo)) {
            reciboService.emitir(p);
        } else if (nuevo == EstadoPedido.CANCELADO) {
            reciboService.anular(p);
        }
    }

    Pedido buscarPorCodigo(String codigo) {
        return pedidoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
    }

    private Pedido buscar(Long id) {
        return pedidoRepository.findWithItemsById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
    }

    /** Verifica stock antes de cobrar, para no cobrar algo que ya no hay. */
    void verificarStock(Pedido p) {
        for (PedidoItem item : p.getItems()) {
            if (item.getProducto().getStock() < item.getCantidad()) {
                throw new NegocioException("Stock insuficiente para " + item.getNombreProducto()
                        + ". Escríbenos por WhatsApp para coordinar.");
            }
        }
    }

    private void descontarStock(Pedido p) {
        for (PedidoItem item : p.getItems()) {
            Producto prod = item.getProducto();
            if (prod.getStock() < item.getCantidad()) {
                throw new NegocioException("Stock insuficiente para " + prod.getNombre());
            }
            prod.setStock(prod.getStock() - item.getCantidad());
        }
        p.setStockDescontado(true);
    }

    private void reponerStock(Pedido p) {
        for (PedidoItem item : p.getItems()) {
            Producto prod = item.getProducto();
            prod.setStock(prod.getStock() + item.getCantidad());
        }
        p.setStockDescontado(false);
    }

    private String generarCodigo() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder("PED-");
            for (int i = 0; i < 6; i++) sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
            codigo = sb.toString();
        } while (pedidoRepository.existsByCodigo(codigo));
        return codigo;
    }
}
