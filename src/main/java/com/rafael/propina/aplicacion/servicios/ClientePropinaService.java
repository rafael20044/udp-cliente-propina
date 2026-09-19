package com.rafael.propina.aplicacion.servicios;

import com.rafael.propina.aplicacion.dto.CalcularPropinaCommand;
import com.rafael.propina.aplicacion.dto.ConectarCommand;
import com.rafael.propina.aplicacion.excepciones.ClienteRedException;
import com.rafael.propina.aplicacion.mapper.ClienteMapper;
import com.rafael.propina.aplicacion.puertos.entrada.CalcularPropinaInputPort;
import com.rafael.propina.aplicacion.puertos.entrada.GestionarConexionInputPort;
import com.rafael.propina.dominio.enums.EstadoConexion;
import com.rafael.propina.dominio.modelos.EventoCliente;
import com.rafael.propina.dominio.modelos.ResultadoPropina;
import com.rafael.propina.dominio.puertos.salida.ClienteUdpPort;
import com.rafael.propina.dominio.puertos.salida.PuertoNotificacionCliente;
import com.rafael.propina.dominio.vo.DestinoServidor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientePropinaService implements GestionarConexionInputPort, CalcularPropinaInputPort {
    private static final Logger LOG = LoggerFactory.getLogger(ClientePropinaService.class);
    private static final String LOG_ERROR_OPERACION = "Falló la operación UDP {}";
    private static final String OPERACION_CONECTAR = "conectar";
    private static final String OPERACION_DESCONECTAR = "desconectar";
    private static final String OPERACION_CALCULAR = "calcular";
    private final ClienteUdpPort clienteUdp;
    private final PuertoNotificacionCliente notificador;
    private final ClienteMapper mapper;
    private final Executor executor;

    public ClientePropinaService(
            final ClienteUdpPort clienteUdp,
            final PuertoNotificacionCliente notificador,
            final ClienteMapper mapper,
            final Executor executor) {
        this.clienteUdp = Objects.requireNonNull(clienteUdp, "El puerto cliente UDP es obligatorio.");
        this.notificador = Objects.requireNonNull(notificador, "El notificador es obligatorio.");
        this.mapper = Objects.requireNonNull(mapper, "El mapper es obligatorio.");
        this.executor = Objects.requireNonNull(executor, "El executor es obligatorio.");
    }

    @Override
    public CompletableFuture<Void> conectar(final ConectarCommand comando) {
        final DestinoServidor destino = mapper.toDestino(comando);
        notificador.notificarEstado(EstadoConexion.CONECTANDO, destino.endpoint());
        return CompletableFuture.runAsync(() -> {
            try {
                clienteUdp.conectar(destino);
                notificador.notificarEstado(EstadoConexion.CONECTADO, destino.endpoint());
                notificador.notificarEvento(new EventoCliente("CONEXIÓN", "Conectado a " + destino.endpoint()));
            } catch (final ClienteRedException excepcion) {
                LOG.error(LOG_ERROR_OPERACION, OPERACION_CONECTAR, excepcion);
                notificador.notificarEstado(EstadoConexion.DESCONECTADO, destino.endpoint());
                notificador.notificarError(excepcion.getMessage());
                throw new CompletionException(excepcion);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> desconectar() {
        return CompletableFuture.runAsync(() -> {
            try {
                clienteUdp.desconectar();
                notificador.notificarEstado(EstadoConexion.DESCONECTADO, "");
                notificador.notificarEvento(new EventoCliente("CONEXIÓN", "Cliente desconectado."));
            } catch (final ClienteRedException excepcion) {
                LOG.error(LOG_ERROR_OPERACION, OPERACION_DESCONECTAR, excepcion);
                notificador.notificarError(excepcion.getMessage());
                throw new CompletionException(excepcion);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<ResultadoPropina> calcular(final CalcularPropinaCommand comando) {
        final var datos = mapper.toDomain(comando);
        return CompletableFuture.supplyAsync(() -> {
            try {
                final ResultadoPropina resultado = clienteUdp.solicitarCalculo(datos);
                notificador.notificarResultado(resultado);
                return resultado;
            } catch (final ClienteRedException excepcion) {
                LOG.error(LOG_ERROR_OPERACION, OPERACION_CALCULAR, excepcion);
                notificador.notificarError(excepcion.getMessage());
                throw new CompletionException(excepcion);
            }
        }, executor);
    }

    @Override
    public boolean estaConectado() {
        return clienteUdp.estaConectado();
    }
}
