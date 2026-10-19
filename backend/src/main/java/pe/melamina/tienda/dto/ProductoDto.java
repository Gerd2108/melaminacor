package pe.melamina.tienda.dto;

import jakarta.validation.constraints.*;
import pe.melamina.tienda.entity.Producto;
import pe.melamina.tienda.entity.TipoProducto;

import java.math.BigDecimal;

public final class ProductoDto {

    private ProductoDto() {}

    public record Request(
            @NotBlank @Size(max = 150) String nombre,
            @Size(max = 50) String sku,
            String descripcion,
            @NotNull @DecimalMin("0.0") @Digits(integer = 8, fraction = 2) BigDecimal precio, // 0 = sin costo
            @DecimalMin("0.0") @Digits(integer = 8, fraction = 2) BigDecimal precioOferta,
            @NotNull @Min(0) Integer stock,
            @NotNull TipoProducto tipo,
            Long categoriaId,
            @Size(max = 80) String medidas,
            @Size(max = 60) String color,
            @Size(max = 30) String espesor,
            String imagenUrl,
            boolean activo,
            boolean destacado,
            boolean soloCotizacion) {}

    public record Response(
            Long id, String nombre, String sku, String descripcion,
            BigDecimal precio, BigDecimal precioOferta, BigDecimal precioVigente,
            Integer stock, TipoProducto tipo,
            Long categoriaId, String categoriaNombre,
            String medidas, String color, String espesor, String imagenUrl,
            boolean activo, boolean destacado, boolean soloCotizacion) {

        public static Response from(Producto p) {
            var cat = p.getCategoria();
            return new Response(
                    p.getId(), p.getNombre(), p.getSku(), p.getDescripcion(),
                    p.getPrecio(), p.getPrecioOferta(), p.getPrecioVigente(),
                    p.getStock(), p.getTipo(),
                    cat != null ? cat.getId() : null, cat != null ? cat.getNombre() : null,
                    p.getMedidas(), p.getColor(), p.getEspesor(), p.getImagenUrl(),
                    p.isActivo(), p.isDestacado(), p.isSoloCotizacion());
        }
    }
}
