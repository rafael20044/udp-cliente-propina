package com.rafael.propina.dominio.modelos;

import java.time.LocalDateTime;
import java.util.Objects;

public record EventoCliente(LocalDateTime fechaHora, String categoria, String descripcion) {
    public EventoCliente {
        Objects.requireNonNull(fechaHora, "La fecha es obligatoria.");
        Objects.requireNonNull(categoria, "La categoría es obligatoria.");
        Objects.requireNonNull(descripcion, "La descripción es obligatoria.");
    }

    public EventoCliente(final String categoria, final String descripcion) {
        this(LocalDateTime.now(), categoria, descripcion);
    }
}
