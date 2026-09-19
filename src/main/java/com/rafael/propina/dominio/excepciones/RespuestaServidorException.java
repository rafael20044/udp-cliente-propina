package com.rafael.propina.dominio.excepciones;

import java.io.Serial;

public final class RespuestaServidorException extends DominioException {
    @Serial
    private static final long serialVersionUID = 1L;

    public RespuestaServidorException(final String mensaje) {
        super(mensaje);
    }
}
