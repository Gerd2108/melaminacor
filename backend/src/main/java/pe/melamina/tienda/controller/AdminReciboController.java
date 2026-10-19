package pe.melamina.tienda.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import pe.melamina.tienda.dto.PageResponse;
import pe.melamina.tienda.dto.ReciboDto;
import pe.melamina.tienda.service.ReciboService;

/** Recibos emitidos (ADMIN y VENTAS, ver SecurityConfig). */
@RestController
@RequestMapping("/api/admin/recibos")
public class AdminReciboController {

    private final ReciboService reciboService;

    public AdminReciboController(ReciboService reciboService) {
        this.reciboService = reciboService;
    }

    // GET /api/admin/recibos?q=R001&page=0
    @GetMapping
    public PageResponse<ReciboDto.Response> listar(@RequestParam(required = false) String q,
                                                   @PageableDefault(size = 20) Pageable pageable) {
        return reciboService.listar(q, pageable);
    }

    @GetMapping("/{id}")
    public ReciboDto.Response obtener(@PathVariable Long id) {
        return reciboService.obtener(id);
    }
}
