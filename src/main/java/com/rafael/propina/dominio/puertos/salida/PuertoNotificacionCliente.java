package com.rafael.propina.dominio.puertos.salida;

import com.rafael.propina.dominio.enums.EstadoConexion;
import com.rafael.propina.dominio.modelos.EventoCliente;
import com.rafael.propina.dominio.modelos.ResultadoPropina;

public interface PuertoNotificacionCliente {
    void notificarEstado(EstadoConexion estado, String endpoint);

    void notificarEvento(EventoCliente evento);

    void notificarResultado(ResultadoPropina resultado);

    void notificarError(String mensaje);
}
