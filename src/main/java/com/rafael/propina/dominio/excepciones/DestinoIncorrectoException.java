package com.rafael.propina.dominio.excepciones;

import java.io.Serial;

public final class DestinoIncorrectoException extends DominioException {
    @Serial
    private static final long serialVersionUID = 1L;

    public DestinoIncorrectoException(final String mensaje) {
        super(mensaje);
    }
}
