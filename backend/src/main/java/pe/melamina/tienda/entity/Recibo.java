package pe.melamina.tienda.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Recibo de venta: se emite automáticamente cuando un pedido queda pagado.
 * Guarda una copia de los datos del emisor y del cliente en ese momento,
 * para que el recibo no cambie aunque después se editen los datos.
 * El detalle de productos se toma de los PedidoItem, que ya son una copia inmutable.
 * Es un documento interno: no reemplaza a un comprobante electrónico de SUNAT.
 */
@Entity
@Table(name = "recibos")
public class Recibo {

    public static final String EMITIDO = "EMITIDO";
    public static final String ANULADO = "ANULADO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Serie y correlativo, ej. R001-00000001. */
    @Column(unique = true, length = 20)
    private String numero;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    @Column(nullable = false, length = 10)
    private String estado;

    private LocalDateTime fechaAnulacion;

    // Emisor (copia de app.empresa.* al momento de emitir)
    @Column(nullable = false, length = 150) private String emisorRazonSocial;
    @Column(nullable = false, length = 11)  private String emisorRuc;
    @Column(length = 200) private String emisorDireccion;
    @Column(length = 30)  private String emisorTelefono;

    // Cliente (copia del pedido)
    @Column(nullable = false, length = 120) private String clienteNombre;
    @Column(nullable = false, length = 3)   private String clienteDocumentoTipo; // DNI o RUC
    @Column(nullable = false, length = 11)  private String clienteDocumento;
    @Column(length = 300) private String clienteDireccion;
    @Column(length = 120) private String clienteEmail;

    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal subtotal;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal igv;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal total;

    @Column(nullable = false, length = 20) private String metodoPago;
    @Column(length = 100) private String referenciaPago;

    // ---- getters y setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaAnulacion() { return fechaAnulacion; }
    public void setFechaAnulacion(LocalDateTime fechaAnulacion) { this.fechaAnulacion = fechaAnulacion; }

    public String getEmisorRazonSocial() { return emisorRazonSocial; }
    public void setEmisorRazonSocial(String emisorRazonSocial) { this.emisorRazonSocial = emisorRazonSocial; }

    public String getEmisorRuc() { return emisorRuc; }
    public void setEmisorRuc(String emisorRuc) { this.emisorRuc = emisorRuc; }

    public String getEmisorDireccion() { return emisorDireccion; }
    public void setEmisorDireccion(String emisorDireccion) { this.emisorDireccion = emisorDireccion; }

    public String getEmisorTelefono() { return emisorTelefono; }
    public void setEmisorTelefono(String emisorTelefono) { this.emisorTelefono = emisorTelefono; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteDocumentoTipo() { return clienteDocumentoTipo; }
    public void setClienteDocumentoTipo(String clienteDocumentoTipo) { this.clienteDocumentoTipo = clienteDocumentoTipo; }

    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }

    public String getClienteDireccion() { return clienteDireccion; }
    public void setClienteDireccion(String clienteDireccion) { this.clienteDireccion = clienteDireccion; }

    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String referenciaPago) { this.referenciaPago = referenciaPago; }
}
