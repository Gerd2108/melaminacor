package pe.melamina.tienda.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.melamina.tienda.dto.PageResponse;
import pe.melamina.tienda.dto.ProductoDto;
import pe.melamina.tienda.entity.Producto;
import pe.melamina.tienda.entity.TipoProducto;
import pe.melamina.tienda.exception.NegocioException;
import pe.melamina.tienda.exception.RecursoNoEncontradoException;
import pe.melamina.tienda.repository.ProductoRepository;

import static pe.melamina.tienda.repository.ProductoSpecs.*;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;

    public ProductoService(ProductoRepository productoRepository, CategoriaService categoriaService) {
        this.productoRepository = productoRepository;
        this.categoriaService = categoriaService;
    }

    /** Búsqueda con filtros. soloActivos=true para la tienda, false para el panel admin. */
    @Transactional(readOnly = true)
    public PageResponse<ProductoDto.Response> buscar(TipoProducto tipo, Long categoriaId, String q,
                                                     Boolean destacados, boolean soloActivos, Pageable pageable) {
        Specification<Producto> spec = Specification.where(activos(soloActivos))
                .and(deTipo(tipo))
                .and(deCategoria(categoriaId))
                .and(destacados(destacados))
                .and(busqueda(q));
        return PageResponse.of(productoRepository.findAll(spec, pageable).map(ProductoDto.Response::from));
    }

    @Transactional(readOnly = true)
    public ProductoDto.Response obtenerPublico(Long id) {
        Producto p = buscarEntidad(id);
        if (!p.isActivo()) throw new RecursoNoEncontradoException("Producto no encontrado");
        return ProductoDto.Response.from(p);
    }

    @Transactional(readOnly = true)
    public ProductoDto.Response obtener(Long id) {
        return ProductoDto.Response.from(buscarEntidad(id));
    }

    @Transactional
    public ProductoDto.Response crear(ProductoDto.Request req) {
        String sku = normalizarSku(req.sku());
        if (sku != null && productoRepository.existsBySkuIgnoreCase(sku)) {
            throw new NegocioException("El SKU " + sku + " ya está registrado");
        }
        Producto p = new Producto();
        aplicar(p, req, sku);
        return ProductoDto.Response.from(productoRepository.save(p));
    }

    @Transactional
    public ProductoDto.Response actualizar(Long id, ProductoDto.Request req) {
        Producto p = buscarEntidad(id);
        String sku = normalizarSku(req.sku());
        if (sku != null && !sku.equalsIgnoreCase(p.getSku()) && productoRepository.existsBySkuIgnoreCase(sku)) {
            throw new NegocioException("El SKU " + sku + " ya está registrado");
        }
        aplicar(p, req, sku);
        return ProductoDto.Response.from(p);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto p = buscarEntidad(id);
        try {
            productoRepository.delete(p);
            productoRepository.flush(); // forzamos el DELETE aquí para capturar el error de FK
        } catch (DataIntegrityViolationException e) {
            throw new NegocioException("Este producto tiene pedidos asociados. Desactívalo en lugar de eliminarlo.");
        }
    }

    Producto buscarEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }

    private void aplicar(Producto p, ProductoDto.Request req, String sku) {
        if (req.precioOferta() != null && req.precioOferta().compareTo(req.precio()) >= 0) {
            throw new NegocioException("El precio de oferta debe ser menor al precio normal");
        }
        p.setNombre(req.nombre().trim());
        p.setSku(sku);
        p.setDescripcion(req.descripcion());
        p.setPrecio(req.precio());
        p.setPrecioOferta(req.precioOferta());
        p.setStock(req.stock());
        p.setTipo(req.tipo());
        p.setCategoria(req.categoriaId() != null ? categoriaService.buscar(req.categoriaId()) : null);
        p.setMedidas(req.medidas());
        p.setColor(req.color());
        p.setEspesor(req.espesor());
        p.setImagenUrl(req.imagenUrl());
        p.setActivo(req.activo());
        p.setDestacado(req.destacado());
        p.setSoloCotizacion(req.soloCotizacion());
    }

    private String normalizarSku(String sku) {
        return sku == null || sku.isBlank() ? null : sku.trim().toUpperCase();
    }
}
