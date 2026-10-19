package pe.melamina.tienda.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.melamina.tienda.dto.PedidoDto;
import pe.melamina.tienda.dto.ReciboDto;
import pe.melamina.tienda.service.PagoService;
import pe.melamina.tienda.service.ReciboService;
import pe.melamina.tienda.service.PedidoService;

@RestController
@RequestMapping("/api/public")
public class PedidoController {

    private final PedidoService pedidoService;
    private final PagoService pagoService;
    private final ReciboService reciboService;

    public PedidoController(PedidoService pedidoService, PagoService pagoService, ReciboService reciboService) {
        this.pedidoService = pedidoService;
        this.pagoService = pagoService;
        this.reciboService = reciboService;
    }

    @PostMapping("/pedidos")
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoDto.Response crear(@Valid @RequestBody PedidoDto.Request req) {
        return pedidoService.crear(req);
    }

    // GET /api/public/pedidos/PED-ABC123?email=cliente@correo.com
    @GetMapping("/pedidos/{codigo}")
    public PedidoDto.Response consultar(@PathVariable String codigo, @RequestParam String email) {
        return pedidoService.consultar(codigo, email);
    }

    // GET /api/public/pedidos/PED-ABC123/recibo?email=cliente@correo.com
    @GetMapping("/pedidos/{codigo}/recibo")
    public ReciboDto.Response recibo(@PathVariable String codigo, @RequestParam String email) {
        return reciboService.obtenerPublico(codigo, email);
    }

    @PostMapping("/pagos/culqi")
    public PedidoDto.Response pagarConTarjeta(@Valid @RequestBody PedidoDto.PagoCulqi req) {
        return pagoService.pagarConCulqi(req);
    }
}
