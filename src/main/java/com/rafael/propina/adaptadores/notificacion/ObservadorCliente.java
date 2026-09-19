package com.rafael.propina.adaptadores.notificacion;

import com.rafael.propina.dominio.enums.EstadoConexion;
import com.rafael.propina.dominio.modelos.EventoCliente;
import com.rafael.propina.dominio.modelos.ResultadoPropina;

public interface ObservadorCliente {
    void onEstado(EstadoConexion estado, String endpoint);

    void onEvento(EventoCliente evento);

    void onResultado(ResultadoPropina resultado);

    void onError(String mensaje);
}
