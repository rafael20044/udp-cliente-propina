package com.rafael.propina.dominio.excepciones;

import java.io.Serial;

public final class ValorCuentaIncorrectoException extends DominioException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ValorCuentaIncorrectoException(final String mensaje) {
        super(mensaje);
    }
}
