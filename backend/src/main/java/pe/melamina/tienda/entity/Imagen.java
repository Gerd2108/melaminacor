package pe.melamina.tienda.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Imagen guardada dentro de PostgreSQL (columna bytea).
 * Así las fotos sobreviven aunque el servidor se reinicie, algo que pasa seguido en hostings gratuitos.
 */
@Entity
@Table(name = "imagenes")
public class Imagen {

    /** Nombre del archivo, ej. "3f2a...jpg". Es la parte final de la URL /uploads/{nombre}. */
    @Id
    @Column(length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String contentType;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] datos;

    @CreationTimestamp
    private LocalDateTime creadoEn;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public byte[] getDatos() { return datos; }
    public void setDatos(byte[] datos) { this.datos = datos; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
}
