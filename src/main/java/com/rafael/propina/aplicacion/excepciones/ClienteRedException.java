package com.rafael.propina.aplicacion.excepciones;

import java.io.Serial;

public final class ClienteRedException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public ClienteRedException(final String mensaje, final Throwable causa) {
        super(mensaje, causa);
    }

    public ClienteRedException(final String mensaje) {
        super(mensaje);
    }
}
