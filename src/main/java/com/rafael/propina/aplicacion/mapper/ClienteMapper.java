package com.rafael.propina.aplicacion.mapper;

import com.rafael.propina.aplicacion.dto.CalcularPropinaCommand;
import com.rafael.propina.aplicacion.dto.ConectarCommand;
import com.rafael.propina.dominio.modelos.DatosPropina;
import com.rafael.propina.dominio.vo.DestinoServidor;
import com.rafael.propina.dominio.vo.PorcentajePropina;
import com.rafael.propina.dominio.vo.ValorCuenta;

import java.util.Objects;

public final class ClienteMapper {
    public DestinoServidor toDestino(final ConectarCommand comando) {
        Objects.requireNonNull(comando, "El comando no puede ser nulo.");
        return new DestinoServidor(comando.host(), comando.puerto());
    }

    public DatosPropina toDomain(final CalcularPropinaCommand comando) {
        Objects.requireNonNull(comando, "El comando no puede ser nulo.");
        return new DatosPropina(new ValorCuenta(comando.valorCuenta()), new PorcentajePropina(comando.porcentajePropina()));
    }
}
