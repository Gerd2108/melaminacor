package pe.melamina.tienda.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class AuthDto {

    private AuthDto() {}

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    public record LoginResponse(String token, String nombre, String email, String rol) {}
}
