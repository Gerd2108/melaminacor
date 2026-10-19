package pe.melamina.tienda.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.melamina.tienda.entity.Categoria;
import pe.melamina.tienda.entity.TipoProducto;
import pe.melamina.tienda.entity.Usuario;
import pe.melamina.tienda.repository.CategoriaRepository;
import pe.melamina.tienda.repository.UsuarioRepository;

import java.util.List;

/** Crea el usuario administrador y categorías iniciales la primera vez que arranca la app. */
@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, CategoriaRepository categoriaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${app.admin.email}")
    private String adminEmail;
    @Value("${app.admin.password}")
    private String adminPassword;
    @Value("${app.admin.nombre}")
    private String adminNombre;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.findByEmailIgnoreCase(adminEmail).isEmpty()) {
            Usuario admin = new Usuario();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setNombre(adminNombre);
            admin.setRol("ADMIN");
            usuarioRepository.save(admin);
            log.info("Usuario administrador creado: {}", adminEmail);
        }

        if (categoriaRepository.count() == 0) {
            categoriaRepository.saveAll(List.of(
                    cat("Melamina 18 mm", "Planchas para estructuras de muebles", TipoProducto.PLANCHA),
                    cat("Melamina 15 mm", "Planchas para puertas, cajones y divisiones", TipoProducto.PLANCHA),
                    cat("Optimización y despiece", "Plan de cortes con Lepton Optimizer", TipoProducto.SERVICIO),
                    cat("Corte a medida", "Corte de piezas en maquinaria especializada", TipoProducto.SERVICIO),
                    cat("Canteado", "Pegado de tapacanto en los bordes", TipoProducto.SERVICIO),
                    cat("Instalación", "Armado e instalación en obra", TipoProducto.SERVICIO)
            ));
            log.info("Categorías iniciales creadas");
        }
    }

    private Categoria cat(String nombre, String descripcion, TipoProducto tipo) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setDescripcion(descripcion);
        c.setTipo(tipo);
        return c;
    }
}
