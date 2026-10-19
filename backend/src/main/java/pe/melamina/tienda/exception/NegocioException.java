package pe.melamina.tienda.exception;

/** Errores de reglas de negocio (sin stock, pago rechazado, etc.). */
public class NegocioException extends RuntimeException {
    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
