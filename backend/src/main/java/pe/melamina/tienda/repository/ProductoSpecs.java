package pe.melamina.tienda.repository;

import org.springframework.data.jpa.domain.Specification;
import pe.melamina.tienda.entity.Producto;
import pe.melamina.tienda.entity.TipoProducto;

/** Filtros dinámicos para el catálogo. Cada uno devuelve null si no aplica. */
public final class ProductoSpecs {

    private ProductoSpecs() {}

    public static Specification<Producto> activos(boolean soloActivos) {
        return soloActivos ? (root, q, cb) -> cb.isTrue(root.get("activo")) : null;
    }

    public static Specification<Producto> deTipo(TipoProducto tipo) {
        return tipo == null ? null : (root, q, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Producto> deCategoria(Long categoriaId) {
        return categoriaId == null ? null : (root, q, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    public static Specification<Producto> destacados(Boolean destacado) {
        return Boolean.TRUE.equals(destacado) ? (root, q, cb) -> cb.isTrue(root.get("destacado")) : null;
    }

    public static Specification<Producto> busqueda(String texto) {
        if (texto == null || texto.isBlank()) return null;
        String like = "%" + texto.trim().toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), like),
                cb.like(cb.lower(root.get("color")), like),
                cb.like(cb.lower(root.get("sku")), like)
        );
    }
}
