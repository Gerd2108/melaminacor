package pe.melamina.tienda.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.melamina.tienda.entity.Imagen;
import pe.melamina.tienda.exception.NegocioException;
import pe.melamina.tienda.repository.ImagenRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Guarda las imágenes en PostgreSQL (tabla "imagenes") y las sirve en /uploads/{nombre}.
 * Si una imagen antigua no está en la base, la busca en la carpeta local "uploads"
 * (compatibilidad con las fotos subidas antes de este cambio).
 */
@Service
public class AlmacenamientoService {

    private static final Map<String, String> EXTENSIONES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final ImagenRepository imagenRepository;
    private final Path carpetaLocal;

    public AlmacenamientoService(ImagenRepository imagenRepository, @Value("${app.uploads.dir}") String dir) {
        this.imagenRepository = imagenRepository;
        this.carpetaLocal = Path.of(dir).toAbsolutePath().normalize();
    }

    /** Guarda una imagen subida desde el panel y devuelve su ruta pública, ej. "/uploads/3f2a....jpg". */
    @Transactional
    public String guardarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw new NegocioException("Selecciona una imagen");
        String ext = EXTENSIONES.get(archivo.getContentType());
        if (ext == null) throw new NegocioException("Formato no permitido. Usa JPG, PNG o WEBP.");
        try {
            return guardar(UUID.randomUUID() + ext, archivo.getContentType(), archivo.getBytes());
        } catch (IOException e) {
            throw new NegocioException("No se pudo guardar la imagen");
        }
    }

    /** Guarda un archivo con nombre fijo (imágenes de muestra). No lo duplica si ya existe. */
    @Transactional
    public String guardarArchivo(String nombre, InputStream contenido) throws IOException {
        if (!imagenRepository.existsById(nombre)) {
            guardar(nombre, tipoPorNombre(nombre), contenido.readAllBytes());
        }
        return "/uploads/" + nombre;
    }

    @Transactional(readOnly = true)
    public Optional<Imagen> buscar(String nombre) {
        Optional<Imagen> enBase = imagenRepository.findById(nombre);
        if (enBase.isPresent()) return enBase;

        // Compatibilidad: imagen antigua guardada en disco
        Path archivo = carpetaLocal.resolve(nombre).normalize();
        if (!archivo.startsWith(carpetaLocal) || !Files.isRegularFile(archivo)) return Optional.empty();
        try {
            Imagen img = new Imagen();
            img.setNombre(nombre);
            img.setContentType(tipoPorNombre(nombre));
            img.setDatos(Files.readAllBytes(archivo));
            return Optional.of(img);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private String guardar(String nombre, String contentType, byte[] datos) {
        Imagen img = new Imagen();
        img.setNombre(nombre);
        img.setContentType(contentType);
        img.setDatos(datos);
        imagenRepository.save(img);
        return "/uploads/" + nombre;
    }

    private String tipoPorNombre(String nombre) {
        String n = nombre.toLowerCase();
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
