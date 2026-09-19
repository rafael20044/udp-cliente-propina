package com.rafael.propina.dominio.excepciones;

import java.io.Serial;

public final class PorcentajePropinaIncorrectoException extends DominioException {
    @Serial
    private static final long serialVersionUID = 1L;

    public PorcentajePropinaIncorrectoException(final String mensaje) {
        super(mensaje);
    }
}
