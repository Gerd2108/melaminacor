package pe.melamina.tienda.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.melamina.tienda.dto.CategoriaDto;
import pe.melamina.tienda.entity.Categoria;
import pe.melamina.tienda.exception.NegocioException;
import pe.melamina.tienda.exception.RecursoNoEncontradoException;
import pe.melamina.tienda.repository.CategoriaRepository;
import pe.melamina.tienda.repository.ProductoRepository;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto.Response> listar() {
        return categoriaRepository.findAllByOrderByTipoAscNombreAsc().stream()
                .map(CategoriaDto.Response::from).toList();
    }

    @Transactional
    public CategoriaDto.Response crear(CategoriaDto.Request req) {
        if (categoriaRepository.existsByNombreIgnoreCase(req.nombre())) {
            throw new NegocioException("Ya existe una categoría llamada \"" + req.nombre() + "\"");
        }
        Categoria c = new Categoria();
        c.setNombre(req.nombre().trim());
        c.setDescripcion(req.descripcion());
        c.setTipo(req.tipo());
        return CategoriaDto.Response.from(categoriaRepository.save(c));
    }

    @Transactional
    public CategoriaDto.Response actualizar(Long id, CategoriaDto.Request req) {
        Categoria c = buscar(id);
        if (!c.getNombre().equalsIgnoreCase(req.nombre()) && categoriaRepository.existsByNombreIgnoreCase(req.nombre())) {
            throw new NegocioException("Ya existe una categoría llamada \"" + req.nombre() + "\"");
        }
        c.setNombre(req.nombre().trim());
        c.setDescripcion(req.descripcion());
        c.setTipo(req.tipo());
        return CategoriaDto.Response.from(c);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria c = buscar(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new NegocioException("No se puede eliminar: la categoría tiene productos. Muévelos a otra categoría primero.");
        }
        categoriaRepository.delete(c);
    }

    Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }
}
