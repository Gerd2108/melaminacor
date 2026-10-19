package pe.melamina.tienda.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Respuesta paginada estable (evita serializar PageImpl directamente). */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <T> PageResponse<T> of(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
}
