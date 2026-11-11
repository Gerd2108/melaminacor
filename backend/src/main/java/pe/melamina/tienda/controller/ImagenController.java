package pe.melamina.tienda.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pe.melamina.tienda.service.AlmacenamientoService;

import java.util.concurrent.TimeUnit;

/** Sirve las imágenes guardadas en la base de datos: GET /uploads/{nombre}. */
@RestController
public class ImagenController {

    private final AlmacenamientoService almacenamientoService;

    public ImagenController(AlmacenamientoService almacenamientoService) {
        this.almacenamientoService = almacenamientoService;
    }

    @GetMapping("/uploads/{nombre:.+}")
    public ResponseEntity<byte[]> imagen(@PathVariable String nombre) {
        return almacenamientoService.buscar(nombre)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(img.getContentType()))
                        .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                        .body(img.getDatos()))
                .orElse(ResponseEntity.notFound().build());
    }
}
