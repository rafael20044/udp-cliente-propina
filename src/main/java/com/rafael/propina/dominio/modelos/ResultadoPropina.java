package com.rafael.propina.dominio.modelos;

import com.rafael.propina.dominio.excepciones.RespuestaServidorException;

import java.util.Objects;

public record ResultadoPropina(
    double valorPropina,
    double totalPagar,
    String valorPropinaFormateado,
    String totalPagarFormateado) {
    public ResultadoPropina {
        if (!Double.isFinite(valorPropina)
                || !Double.isFinite(totalPagar)
                || Objects.isNull(valorPropinaFormateado) || valorPropinaFormateado.isBlank()
                || Objects.isNull(totalPagarFormateado) || totalPagarFormateado.isBlank()) {
            throw new RespuestaServidorException("El resultado recibido está incompleto o es inválido.");
        }
    }
}
