package com.rafael.propina.dominio.puertos.salida;

import com.rafael.propina.aplicacion.excepciones.ClienteRedException;
import com.rafael.propina.dominio.modelos.DatosPropina;
import com.rafael.propina.dominio.modelos.ResultadoPropina;
import com.rafael.propina.dominio.vo.DestinoServidor;

public interface ClienteUdpPort {
    void conectar(DestinoServidor destino) throws ClienteRedException;

    void desconectar() throws ClienteRedException;

    ResultadoPropina solicitarCalculo(DatosPropina datos) throws ClienteRedException;

    boolean estaConectado();
}
