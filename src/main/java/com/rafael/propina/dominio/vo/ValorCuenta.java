package com.rafael.propina.dominio.vo;

import com.rafael.propina.dominio.excepciones.ValorCuentaIncorrectoException;

public record ValorCuenta(double valor) {
    public ValorCuenta {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new ValorCuentaIncorrectoException("Valor de la cuenta incorrecto: debe ser mayor que 0");
        }
    }
}
