package pe.melamina.tienda.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pe.melamina.tienda.security.JwtAuthFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)          // API stateless con JWT
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/public/**", "/api/auth/**", "/uploads/**", "/error").permitAll()
                        // ---- Permisos del panel por rol ----
                        .requestMatchers("/api/admin/perfil/**").authenticated()
                        .requestMatchers("/api/admin/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/recibos/**").hasAnyRole("ADMIN", "VENTAS")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/productos/**", "/api/admin/categorias/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/productos/**", "/api/admin/categorias/**", "/api/admin/uploads/**")
                            .hasAnyRole("ADMIN", "VENTAS", "ALMACEN")
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/pedidos/**").hasAnyRole("ADMIN", "VENTAS", "OPERARIO")
                        .requestMatchers("/api/admin/pedidos/**").hasAnyRole("ADMIN", "VENTAS", "ALMACEN", "OPERARIO")
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().denyAll())
                // Sin token -> 401 (el frontend redirige al login); con token pero sin permiso -> 403
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"detail\":\"Tu rol no tiene permiso para esta acción\"}");
                        }))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
