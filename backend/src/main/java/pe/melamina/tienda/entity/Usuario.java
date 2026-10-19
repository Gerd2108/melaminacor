package pe.melamina.tienda.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String password; // hash BCrypt

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String rol; // ADMIN, VENTAS, ALMACEN u OPERARIO (ver enum Rol)

    /** Un usuario desactivado no puede iniciar sesión (no se borra para conservar el historial). */
    @Column(nullable = false, columnDefinition = "boolean not null default true")
    private boolean activo = true;

    // ---- getters y setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
