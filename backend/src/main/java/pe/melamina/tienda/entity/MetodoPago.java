package pe.melamina.tienda.entity;

public enum MetodoPago {
    TARJETA,        // pago en línea con Culqi: tarjeta o Yape
    YAPE_PLIN,      // Plin: el cliente envía captura y ventas confirma manualmente
    TRANSFERENCIA   // depósito/transferencia; el admin confirma manualmente
}
