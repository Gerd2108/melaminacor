package pe.melamina.tienda.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pe.melamina.tienda.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class PedidoDto {

    private PedidoDto() {}

    public record ItemRequest(@NotNull Long productoId, @NotNull @Min(1) @Max(999) Integer cantidad) {}

    public record Request(
            @NotBlank @Size(max = 120) String nombreCliente,
            @NotBlank @Email @Size(max = 120) String email,
            @NotBlank @Pattern(regexp = "^9\\d{8}$", message = "debe ser un celular peruano de 9 dígitos") String telefono,
            @NotBlank @Pattern(regexp = "^(\\d{8}|\\d{11})$", message = "debe ser DNI (8) o RUC (11)") String documento,
            @NotBlank @Size(max = 200) String direccion,
            @NotBlank @Size(max = 80) String distrito,
            @Size(max = 500) String notas,
            @NotNull MetodoPago metodoPago,
            @NotEmpty @Valid List<ItemRequest> items) {}

    public record ItemResponse(Long productoId, String nombreProducto, BigDecimal precioUnitario,
                               Integer cantidad, BigDecimal subtotal) {
        public static ItemResponse from(PedidoItem i) {
            return new ItemResponse(i.getProducto().getId(), i.getNombreProducto(),
                    i.getPrecioUnitario(), i.getCantidad(), i.getSubtotal());
        }
    }

    public record Response(
            Long id, String codigo, String nombreCliente, String email, String telefono,
            String documento, String direccion, String distrito, String notas,
            BigDecimal subtotal, BigDecimal igv, BigDecimal total,
            EstadoPedido estado, MetodoPago metodoPago, String referenciaPago,
            LocalDateTime fechaCreacion, List<ItemResponse> items,
            Long reciboId, String reciboNumero, String reciboEstado) {

        public static Response from(Pedido p) {
            return new Response(
                    p.getId(), p.getCodigo(), p.getNombreCliente(), p.getEmail(), p.getTelefono(),
                    p.getDocumento(), p.getDireccion(), p.getDistrito(), p.getNotas(),
                    p.getSubtotal(), p.getIgv(), p.getTotal(),
                    p.getEstado(), p.getMetodoPago(), p.getReferenciaPago(),
                    p.getFechaCreacion(), p.getItems().stream().map(ItemResponse::from).toList(),
                    p.getRecibo() != null ? p.getRecibo().getId() : null,
                    p.getRecibo() != null ? p.getRecibo().getNumero() : null,
                    p.getRecibo() != null ? p.getRecibo().getEstado() : null);
        }
    }

    public record CambioEstado(@NotNull EstadoPedido estado, @Size(max = 100) String referenciaPago) {}

    public record PagoCulqi(@NotBlank String pedidoCodigo, @NotBlank String token, @NotBlank @Email String email) {}
}
