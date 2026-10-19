package pe.melamina.tienda.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.melamina.tienda.exception.NegocioException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class AlmacenamientoService {

    private static final Map<String, String> EXTENSIONES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path carpeta;

    public AlmacenamientoService(@Value("${app.uploads.dir}") String dir) throws IOException {
        this.carpeta = Path.of(dir).toAbsolutePath().normalize();
        Files.createDirectories(carpeta);
    }

    /** Copia un archivo (por ejemplo, imágenes de muestra) con un nombre fijo y devuelve su ruta pública. */
    public String guardarArchivo(String nombre, InputStream contenido) throws IOException {
        Files.copy(contenido, carpeta.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + nombre;
    }

    /** Guarda la imagen y devuelve la ruta pública, ej. "/uploads/3f2a....jpg". */
    public String guardarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw new NegocioException("Selecciona una imagen");
        String ext = EXTENSIONES.get(archivo.getContentType());
        if (ext == null) throw new NegocioException("Formato no permitido. Usa JPG, PNG o WEBP.");

        String nombre = UUID.randomUUID() + ext;
        try (InputStream in = archivo.getInputStream()) {
            Files.copy(in, carpeta.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new NegocioException("No se pudo guardar la imagen");
        }
        return "/uploads/" + nombre;
    }
}
