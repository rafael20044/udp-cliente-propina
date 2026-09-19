package com.rafael.propina.dominio.modelos;

import com.rafael.propina.dominio.vo.PorcentajePropina;
import com.rafael.propina.dominio.vo.ValorCuenta;

import java.util.Objects;

public record DatosPropina(ValorCuenta valorCuenta, PorcentajePropina porcentajePropina) {
    public DatosPropina {
        Objects.requireNonNull(valorCuenta, "El valor de la cuenta es obligatorio.");
        Objects.requireNonNull(porcentajePropina, "El porcentaje de propina es obligatorio.");
    }
}
