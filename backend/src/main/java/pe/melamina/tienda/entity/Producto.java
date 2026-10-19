package pe.melamina.tienda.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(unique = true, length = 50)
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /** Precio en soles, IGV incluido. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /** Precio rebajado opcional (IGV incluido). */
    @Column(precision = 10, scale = 2)
    private BigDecimal precioOferta;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoProducto tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Atributos típicos de melamina (todos opcionales)
    @Column(length = 80)
    private String medidas;     

    @Column(length = 60)
    private String color;       

    @Column(length = 30)
    private String espesor;    

    private String imagenUrl;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false)
    private boolean destacado = false;

    /** Si es true no se vende online: solo se cotiza (ideal para muebles a medida). */
    @Column(nullable = false)
    private boolean soloCotizacion = false;

    @CreationTimestamp
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    private LocalDateTime actualizadoEn;

    /** Precio que realmente se cobra. */
    public BigDecimal getPrecioVigente() {
        return precioOferta != null && precioOferta.compareTo(BigDecimal.ZERO) > 0 ? precioOferta : precio;
    }

    // ---- getters y setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public BigDecimal getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(BigDecimal precioOferta) { this.precioOferta = precioOferta; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public TipoProducto getTipo() { return tipo; }
    public void setTipo(TipoProducto tipo) { this.tipo = tipo; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public String getMedidas() { return medidas; }
    public void setMedidas(String medidas) { this.medidas = medidas; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getEspesor() { return espesor; }
    public void setEspesor(String espesor) { this.espesor = espesor; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }

    public boolean isSoloCotizacion() { return soloCotizacion; }
    public void setSoloCotizacion(boolean soloCotizacion) { this.soloCotizacion = soloCotizacion; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
