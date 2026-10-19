package pe.melamina.tienda.dto;

import pe.melamina.tienda.entity.Recibo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ReciboDto {

    private ReciboDto() {}

    public record Response(
            Long id, String numero, String estado, LocalDateTime fechaEmision, LocalDateTime fechaAnulacion,
            String pedidoCodigo,
            String emisorRazonSocial, String emisorRuc, String emisorDireccion, String emisorTelefono,
            String clienteNombre, String clienteDocumentoTipo, String clienteDocumento,
            String clienteDireccion, String clienteEmail,
            BigDecimal subtotal, BigDecimal igv, BigDecimal total,
            String metodoPago, String referenciaPago,
            List<PedidoDto.ItemResponse> items) {

        public static Response from(Recibo r) {
            var pedido = r.getPedido();
            return new Response(
                    r.getId(), r.getNumero(), r.getEstado(), r.getFechaEmision(), r.getFechaAnulacion(),
                    pedido.getCodigo(),
                    r.getEmisorRazonSocial(), r.getEmisorRuc(), r.getEmisorDireccion(), r.getEmisorTelefono(),
                    r.getClienteNombre(), r.getClienteDocumentoTipo(), r.getClienteDocumento(),
                    r.getClienteDireccion(), r.getClienteEmail(),
                    r.getSubtotal(), r.getIgv(), r.getTotal(),
                    r.getMetodoPago(), r.getReferenciaPago(),
                    pedido.getItems().stream().map(PedidoDto.ItemResponse::from).toList());
        }
    }
}
