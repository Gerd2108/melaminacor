package pe.melamina.tienda.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código legible para el cliente, ej. "PED-8F3K2A". */
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    // Datos del cliente
    @Column(nullable = false, length = 120)
    private String nombreCliente;
    @Column(nullable = false, length = 120)
    private String email;
    @Column(nullable = false, length = 20)
    private String telefono;
    /** DNI (8 dígitos) o RUC (11 dígitos) para la boleta/factura. */
    @Column(nullable = false, length = 11)
    private String documento;
    @Column(nullable = false, length = 200)
    private String direccion;
    @Column(nullable = false, length = 80)
    private String distrito;
    @Column(length = 500)
    private String notas;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;   // base imponible
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igv;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetodoPago metodoPago;

    /** Id del cargo en Culqi o número de operación de Yape/transferencia. */
    @Column(length = 100)
    private String referenciaPago;

    /** Evita descontar stock dos veces. */
    @Column(nullable = false)
    private boolean stockDescontado = false;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> items = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    /** Recibo emitido al confirmarse el pago (null mientras no esté pagado). */
    @OneToOne(mappedBy = "pedido", fetch = FetchType.LAZY)
    private Recibo recibo;

    public void agregarItem(PedidoItem item) {
        item.setPedido(this);
        items.add(item);
    }

    // ---- getters y setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }

    public boolean isStockDescontado() { return stockDescontado; }
    public void setStockDescontado(boolean stockDescontado) { this.stockDescontado = stockDescontado; }

    public List<PedidoItem> getItems() { return items; }
    public void setItems(List<PedidoItem> items) { this.items = items; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Recibo getRecibo() { return recibo; }
    public void setRecibo(Recibo recibo) { this.recibo = recibo; }
}
