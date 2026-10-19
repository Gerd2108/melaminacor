package pe.melamina.tienda.dto;

import jakarta.validation.constraints.*;
import pe.melamina.tienda.entity.Rol;
import pe.melamina.tienda.entity.Usuario;

public final class UsuarioDto {

    private UsuarioDto() {}

    public record Response(Long id, String email, String nombre, String rol, boolean activo) {
        public static Response from(Usuario u) {
            return new Response(u.getId(), u.getEmail(), u.getNombre(), u.getRol(), u.isActivo());
        }
    }

    public record Crear(
            @NotBlank @Email @Size(max = 120) String email,
            @NotBlank @Size(max = 100) String nombre,
            @NotNull Rol rol,
            @NotBlank @Size(min = 8, max = 72, message = "debe tener al menos 8 caracteres") String password) {}

    /** password es opcional: si viene, se reemplaza (reseteo por el admin). */
    public record Actualizar(
            @NotBlank @Size(max = 100) String nombre,
            @NotNull Rol rol,
            boolean activo,
            @Size(min = 8, max = 72, message = "debe tener al menos 8 caracteres") String password) {}

    public record CambioPassword(
            @NotBlank String actual,
            @NotBlank @Size(min = 8, max = 72, message = "debe tener al menos 8 caracteres") String nueva) {}
}
