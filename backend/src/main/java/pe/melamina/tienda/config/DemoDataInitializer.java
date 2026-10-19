package pe.melamina.tienda.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.melamina.tienda.entity.Producto;
import pe.melamina.tienda.entity.TipoProducto;
import pe.melamina.tienda.repository.CategoriaRepository;
import pe.melamina.tienda.repository.ProductoRepository;
import pe.melamina.tienda.repository.UsuarioRepository;
import pe.melamina.tienda.entity.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.melamina.tienda.service.AlmacenamientoService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import static pe.melamina.tienda.entity.TipoProducto.*;

/**
 * Carga productos de muestra para ver la tienda con contenido.
 * Solo inserta los que no existen (se identifican por SKU), así que puedes reiniciar sin duplicar.
 * Para no cargarlos más, pon app.demo.cargar: false en application.yml.
 * Para quitarlos, elimínalos desde el panel (todos tienen SKU que empieza con "DEMO-").
 * Los precios son referenciales: reemplázalos por los reales de MelaminCorp.
 */
@Component
@Order(2)
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AlmacenamientoService almacenamientoService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo.cargar:true}")
    private boolean cargar;

    public DemoDataInitializer(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
                               AlmacenamientoService almacenamientoService, UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.almacenamientoService = almacenamientoService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Un usuario de prueba por rol. Contraseña: Melamin123! (cámbiala o desactívalos en producción). */
    private void crearUsuariosDemo() {
        String[][] demo = {
                {"ventas@melamincorp.pe", "Atención y Ventas (demo)", "VENTAS"},
                {"almacen@melamincorp.pe", "Almacén (demo)", "ALMACEN"},
                {"taller@melamincorp.pe", "Operario de taller (demo)", "OPERARIO"},
        };
        for (String[] d : demo) {
            if (usuarioRepository.findByEmailIgnoreCase(d[0]).isPresent()) continue;
            Usuario u = new Usuario();
            u.setEmail(d[0]);
            u.setNombre(d[1]);
            u.setRol(d[2]);
            u.setPassword(passwordEncoder.encode("Melamin123!"));
            u.setActivo(true);
            usuarioRepository.save(u);
            log.info("Usuario de prueba creado: {} ({})", d[0], d[2]);
        }
    }

    private record Demo(String sku, String nombre, TipoProducto tipo, String categoria,
                        String precio, String oferta, int stock,
                        String medidas, String color, String espesor,
                        String imagen, boolean destacado, boolean soloCotizacion, String descripcion) {}

    private static final List<Demo> PRODUCTOS = List.of(
            // ---------- Planchas de melamina 18 mm ----------
            new Demo("DEMO-PL-BL18", "Plancha de melamina Blanco 18 mm", PLANCHA, "Melamina 18 mm",
                    "189.00", null, 40, "2.44 x 2.14 m", "Blanco", "18 mm", "plancha-blanco.jpg", true, false,
                    "La plancha más pedida para closets, cocinas y muebles de oficina. Acabado liso y fácil de limpiar.\nLa cortamos y canteamos a tu medida."),
            new Demo("DEMO-PL-RS18", "Plancha de melamina Roble 18 mm", PLANCHA, "Melamina 18 mm",
                    "215.00", "199.00", 25, "2.44 x 2.14 m", "Roble", "18 mm", "plancha-roble.jpg", true, false,
                    "Veta de roble en tono miel. Ideal para frentes de cocina y escritorios."),
            new Demo("DEMO-PL-NO18", "Plancha de melamina Nogal 18 mm", PLANCHA, "Melamina 18 mm",
                    "219.00", null, 18, "2.44 x 2.14 m", "Nogal", "18 mm", "plancha-nogal.jpg", false, false,
                    "Tono nogal oscuro con veta marcada. Muy usado en closets y muebles de sala."),
            new Demo("DEMO-PL-GR18", "Plancha de melamina Grafito 18 mm", PLANCHA, "Melamina 18 mm",
                    "209.00", null, 22, "2.44 x 2.14 m", "Grafito", "18 mm", "plancha-grafito.jpg", true, false,
                    "Gris oscuro mate. Combina con blanco y con maderas claras."),
            new Demo("DEMO-PL-CE18", "Plancha de melamina Cedro 18 mm", PLANCHA, "Melamina 18 mm",
                    "215.00", null, 15, "2.44 x 2.14 m", "Cedro", "18 mm", "plancha-cedro.jpg", false, false,
                    "Veta rojiza tipo cedro, clásica para dormitorios."),
            new Demo("DEMO-PL-AL18", "Plancha de melamina Almendra 18 mm", PLANCHA, "Melamina 18 mm",
                    "209.00", null, 12, "2.44 x 2.14 m", "Almendra", "18 mm", "plancha-almendra.jpg", false, false,
                    "Madera clara de tono cálido. Da amplitud a espacios pequeños."),
            new Demo("DEMO-PL-GH18", "Plancha de melamina Gris Humo 18 mm", PLANCHA, "Melamina 18 mm",
                    "209.00", null, 10, "2.44 x 2.14 m", "Gris Humo", "18 mm", "plancha-gris.jpg", false, false,
                    "Gris medio liso, sobrio para oficinas y cocinas modernas."),
            new Demo("DEMO-PL-WE18", "Plancha de melamina Wengué 18 mm", PLANCHA, "Melamina 18 mm",
                    "225.00", null, 0, "2.44 x 2.14 m", "Wengué", "18 mm", "plancha-wengue.jpg", false, false,
                    "Madera muy oscura de veta fina. (Producto de muestra sin stock.)"),

            // ---------- Planchas de melamina 15 mm ----------
            new Demo("DEMO-PL-BL15", "Plancha de melamina Blanco 15 mm", PLANCHA, "Melamina 15 mm",
                    "165.00", null, 30, "2.44 x 2.14 m", "Blanco", "15 mm", "plancha-blanco-15.jpg", false, false,
                    "Más liviana, ideal para puertas, cajones y divisiones interiores."),
            new Demo("DEMO-PL-RS15", "Plancha de melamina Roble 15 mm", PLANCHA, "Melamina 15 mm",
                    "189.00", null, 14, "2.44 x 2.14 m", "Roble", "15 mm", "plancha-roble-15.jpg", false, false,
                    "Mismo acabado de la plancha de 18 mm, en espesor para puertas."),

            // ---------- Servicios ----------
            new Demo("DEMO-SV-OPT", "Optimización y despiece con Lepton Optimizer", SERVICIO, "Optimización y despiece",
                    "0.00", null, 0, "Por proyecto", null, null, "servicio-optimizacion.jpg", true, true,
                    "Con tu lista de piezas calculamos el plan de cortes que mejor aprovecha cada plancha, para que compres solo lo necesario.\nSin costo al comprar tus planchas con nosotros."),
            new Demo("DEMO-SV-COR", "Corte a medida", SERVICIO, "Corte a medida",
                    "15.00", null, 0, "Por plancha", null, null, "servicio-corte.jpg", true, true,
                    "Cortamos tus piezas con precisión en maquinaria especializada, siguiendo el despiece aprobado.\nEl precio final depende de la cantidad de cortes."),
            new Demo("DEMO-SV-CAD", "Canteado con tapacanto delgado (0.45 mm)", SERVICIO, "Canteado",
                    "1.50", null, 0, "Por metro lineal", "Del color de tu plancha", "0.45 mm", "servicio-canteado-delgado.jpg", false, true,
                    "Pegado de tapacanto en los bordes visibles de las piezas, con verificación del acabado. Ideal para piezas interiores."),
            new Demo("DEMO-SV-CAG", "Canteado con tapacanto grueso (2 mm)", SERVICIO, "Canteado",
                    "3.00", null, 0, "Por metro lineal", "Del color de tu plancha", "2 mm", "servicio-canteado-grueso.jpg", false, true,
                    "Tapacanto de mayor resistencia para frentes, puertas y bordes expuestos a golpes."),
            new Demo("DEMO-SV-INS", "Instalación en obra", SERVICIO, "Instalación",
                    "150.00", null, 0, "Según proyecto", null, null, "servicio-instalacion.jpg", true, true,
                    "Armamos e instalamos tus muebles en Lima. Coordinamos la visita y el precio según el tamaño del trabajo.")
    );

    @Override
    @Transactional
    public void run(String... args) {
        if (!cargar) return;
        crearUsuariosDemo();
        int creados = 0;
        for (Demo d : PRODUCTOS) {
            if (productoRepository.existsBySkuIgnoreCase(d.sku())) continue;

            Producto p = new Producto();
            p.setSku(d.sku());
            p.setNombre(d.nombre());
            p.setTipo(d.tipo());
            p.setCategoria(categoriaRepository.findByNombreIgnoreCase(d.categoria()).orElse(null));
            p.setPrecio(new BigDecimal(d.precio()));
            p.setPrecioOferta(d.oferta() != null ? new BigDecimal(d.oferta()) : null);
            p.setStock(d.stock());
            p.setMedidas(d.medidas());
            p.setColor(d.color());
            p.setEspesor(d.espesor());
            p.setDescripcion(d.descripcion());
            p.setDestacado(d.destacado());
            p.setSoloCotizacion(d.soloCotizacion());
            p.setActivo(true);
            p.setImagenUrl(copiarImagen(d.imagen()));
            productoRepository.save(p);
            creados++;
        }
        if (creados > 0) log.info("Productos de muestra creados: {}", creados);
    }

    /** Copia la imagen desde src/main/resources/demo a la carpeta de uploads. */
    private String copiarImagen(String archivo) {
        try (InputStream in = new ClassPathResource("demo/" + archivo).getInputStream()) {
            return almacenamientoService.guardarArchivo("demo-" + archivo, in);
        } catch (Exception e) {
            log.warn("No se pudo copiar la imagen de muestra {}: {}", archivo, e.getMessage());
            return null;
        }
    }
}
