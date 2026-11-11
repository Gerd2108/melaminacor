package pe.melamina.tienda.entity;

/**
 * Roles del personal, basados en los actores identificados en el análisis de MelaminCorp.
 * Spring Security los usa como "ROLE_" + nombre (ej. ROLE_VENTAS).
 */
public enum Rol {
    ADMIN,     // Responsable de la gestión: acceso total y administración de usuarios
    VENTAS,    // Atención al cliente y cotización: productos, categorías, pedidos y pagos
    ALMACEN,   // Responsable del inventario: productos y existencias
    OPERARIO   // Operario de corte / canteado: consulta pedidos y actualiza su avance
}
