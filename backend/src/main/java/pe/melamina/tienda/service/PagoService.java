package pe.melamina.tienda.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pe.melamina.tienda.dto.PedidoDto;
import pe.melamina.tienda.entity.EstadoPedido;
import pe.melamina.tienda.entity.MetodoPago;
import pe.melamina.tienda.entity.Pedido;
import pe.melamina.tienda.exception.NegocioException;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Cobro en línea mediante Culqi (tarjeta o Yape).
 * Flujo: el frontend abre el Checkout Custom de Culqi -> obtiene un token (de tarjeta o de Yape)
 * -> lo envía aquí -> creamos el cargo con la llave secreta. Ambos tokens se cobran igual.
 * Revisa la documentación vigente de Culqi (docs.culqi.com) antes de pasar a producción.
 */
@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final PedidoService pedidoService;
    private final RestClient culqi;
    private final boolean simuladoPermitido;

    public PagoService(PedidoService pedidoService,
                       @Value("${app.culqi.secret-key}") String secretKey,
                       @Value("${app.culqi.api-url}") String apiUrl,
                       @Value("${app.pagos.simulado:false}") boolean simuladoPermitido) {
        this.pedidoService = pedidoService;
        this.simuladoPermitido = simuladoPermitido;
        this.culqi = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .build();
    }

    @Transactional
    public PedidoDto.Response pagarConCulqi(PedidoDto.PagoCulqi req) {
        Pedido pedido = pedidoService.buscarPorCodigo(req.pedidoCodigo());

        if (pedido.getMetodoPago() != MetodoPago.TARJETA) {
            throw new NegocioException("Este pedido no fue registrado para pago en línea");
        }
        if (pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) {
            throw new NegocioException("Este pedido ya fue pagado o cancelado");
        }

        pedidoService.verificarStock(pedido);

        // Pago de prueba (sin Culqi): solo si está permitido en application.yml
        if (req.token().startsWith("sim_")) {
            if (!simuladoPermitido) {
                throw new NegocioException("Los pagos de prueba están desactivados en este servidor");
            }
            pedido.setReferenciaPago("PRUEBA-" + req.token().substring(4, Math.min(req.token().length(), 30)));
            pedidoService.aplicarEstado(pedido, EstadoPedido.PAGADO);
            log.info("Pedido {} marcado como pagado con un pago SIMULADO", pedido.getCodigo());
            return PedidoDto.Response.from(pedido);
        }

        // Culqi trabaja en céntimos: S/ 125.50 -> 12550
        long centimos = pedido.getTotal().multiply(BigDecimal.valueOf(100)).longValueExact();

        Map<String, Object> cuerpo = Map.of(
                "amount", centimos,
                "currency_code", "PEN",
                "email", req.email(),
                "source_id", req.token(),
                "description", "Pedido " + pedido.getCodigo(),
                "metadata", Map.of("pedido", pedido.getCodigo())
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> respuesta = culqi.post()
                    .uri("/charges")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(Map.class);

            String cargoId = respuesta != null ? String.valueOf(respuesta.get("id")) : null;
            pedido.setReferenciaPago(cargoId);
            pedidoService.aplicarEstado(pedido, EstadoPedido.PAGADO);
            log.info("Pedido {} pagado con cargo Culqi {}", pedido.getCodigo(), cargoId);
            return PedidoDto.Response.from(pedido);

        } catch (RestClientResponseException e) {
            log.warn("Culqi rechazó el pago del pedido {}: {}", pedido.getCodigo(), e.getResponseBodyAsString());
            throw new NegocioException(mensajeCulqi(e));
        }
    }

    @SuppressWarnings("unchecked")
    private String mensajeCulqi(RestClientResponseException e) {
        try {
            Map<String, Object> err = e.getResponseBodyAs(Map.class);
            if (err != null && err.get("user_message") != null) return err.get("user_message").toString();
        } catch (Exception ignored) {
            // cuerpo no legible: usamos el mensaje por defecto
        }
        return "El pago fue rechazado. Verifica tus datos o intenta con otro medio de pago.";
    }
}
