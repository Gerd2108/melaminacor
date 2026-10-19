package pe.melamina.tienda.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.melamina.tienda.service.AlmacenamientoService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/uploads")
public class AdminUploadController {

    private final AlmacenamientoService almacenamientoService;

    public AdminUploadController(AlmacenamientoService almacenamientoService) {
        this.almacenamientoService = almacenamientoService;
    }

    /** Recibe multipart/form-data con el campo "archivo" y devuelve { "url": "/uploads/..." }. */
    @PostMapping
    public Map<String, String> subir(@RequestParam("archivo") MultipartFile archivo) {
        return Map.of("url", almacenamientoService.guardarImagen(archivo));
    }
}
