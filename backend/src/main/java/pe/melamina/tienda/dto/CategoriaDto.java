package pe.melamina.tienda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.melamina.tienda.entity.Categoria;
import pe.melamina.tienda.entity.TipoProducto;

public final class CategoriaDto {

    private CategoriaDto() {}

    public record Request(
            @NotBlank @Size(max = 80) String nombre,
            @Size(max = 255) String descripcion,
            @NotNull TipoProducto tipo) {}

    public record Response(Long id, String nombre, String descripcion, TipoProducto tipo) {
        public static Response from(Categoria c) {
            return new Response(c.getId(), c.getNombre(), c.getDescripcion(), c.getTipo());
        }
    }
}
