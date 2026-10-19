package pe.melamina.tienda.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.melamina.tienda.dto.PageResponse;
import pe.melamina.tienda.dto.PedidoDto;
import pe.melamina.tienda.entity.EstadoPedido;
import pe.melamina.tienda.service.PedidoService;

@RestController
@RequestMapping("/api/admin/pedidos")
public class AdminPedidoController {

    private final PedidoService pedidoService;

    public AdminPedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public PageResponse<PedidoDto.Response> listar(@RequestParam(required = false) EstadoPedido estado,
                                                   @PageableDefault(size = 20) Pageable pageable) {
        return pedidoService.listar(estado, pageable);
    }

    @GetMapping("/{id}")
    public PedidoDto.Response obtener(@PathVariable Long id) {
        return pedidoService.obtener(id);
    }

    /** Ej.: confirmar un Yape -> { "estado": "PAGADO", "referenciaPago": "N° operación 123456" } */
    @PatchMapping("/{id}/estado")
    public PedidoDto.Response cambiarEstado(@PathVariable Long id, @Valid @RequestBody PedidoDto.CambioEstado req,
                                            Authentication auth) {
        boolean soloOperario = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OPERARIO"));
        return pedidoService.cambiarEstado(id, req, soloOperario);
    }
}
