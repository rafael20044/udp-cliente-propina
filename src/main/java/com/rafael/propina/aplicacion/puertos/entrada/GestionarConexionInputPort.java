package com.rafael.propina.aplicacion.puertos.entrada;

import com.rafael.propina.aplicacion.dto.ConectarCommand;

import java.util.concurrent.CompletableFuture;

public interface GestionarConexionInputPort {
    CompletableFuture<Void> conectar(ConectarCommand comando);

    CompletableFuture<Void> desconectar();

    boolean estaConectado();
}
