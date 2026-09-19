package com.rafael.propina.dominio.vo;

import com.rafael.propina.dominio.excepciones.PorcentajePropinaIncorrectoException;

public record PorcentajePropina(double valor) {
    public PorcentajePropina {
        if (!Double.isFinite(valor) || valor < 0) {
            throw new PorcentajePropinaIncorrectoException("Porcentaje de propina incorrecto: debe ser mayor o igual a 0");
        }
    }
}
