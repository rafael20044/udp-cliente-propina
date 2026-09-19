package com.rafael.propina.adaptadores.red;

import com.rafael.propina.aplicacion.excepciones.ClienteRedException;
import com.rafael.propina.dominio.excepciones.RespuestaServidorException;
import com.rafael.propina.dominio.modelos.DatosPropina;
import com.rafael.propina.dominio.modelos.ResultadoPropina;
import com.rafael.propina.dominio.puertos.salida.ClienteUdpPort;
import com.rafael.propina.dominio.vo.DestinoServidor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AdaptadorClienteUdp implements ClienteUdpPort {
    private static final Logger LOG = LoggerFactory.getLogger(AdaptadorClienteUdp.class);
    private static final String LOG_ERROR_RED = "Fallo UDP durante {}";
    private final CanalUdp canal;
    private final ProtocoloUdpMapper protocolo;
    private volatile ConexionActiva conexion;

    public AdaptadorClienteUdp(final CanalUdp canal, final ProtocoloUdpMapper protocolo) {
        this.canal = Objects.requireNonNull(canal, "El canal UDP es obligatorio.");
        this.protocolo = Objects.requireNonNull(protocolo, "El protocolo UDP mapper es obligatorio.");
    }

    @Override
    public synchronized void conectar(final DestinoServidor nuevoDestino) throws ClienteRedException {
        Objects.requireNonNull(nuevoDestino);
        try {
            final InetAddress nuevaDireccion = InetAddress.getByName(nuevoDestino.host());
            canal.abrir();
            final String respuesta = canal.intercambiar(
                    ProtocoloUdpMapper.CONECTAR, nuevaDireccion, nuevoDestino.puerto());
            protocolo.validarConexion(respuesta);
            conexion = new ConexionActiva(nuevaDireccion, nuevoDestino);
        } catch (final SocketTimeoutException excepcion) {
            LOG.warn(LOG_ERROR_RED, "conexión por timeout", excepcion);
            canal.cerrar();
            throw new ClienteRedException("El servidor no respondió dentro del tiempo esperado.", excepcion);
        } catch (final UnknownHostException excepcion) {
            LOG.warn(LOG_ERROR_RED, "resolución del host", excepcion);
            canal.cerrar();
            throw new ClienteRedException("No se pudo resolver el host " + nuevoDestino.host() + ".", excepcion);
        } catch (final IOException | RespuestaServidorException excepcion) {
            LOG.error(LOG_ERROR_RED, "conexión", excepcion);
            canal.cerrar();
            throw new ClienteRedException("No se pudo conectar con " + nuevoDestino.endpoint() + ".", excepcion);
        }
    }

    @Override
    public synchronized void desconectar() throws ClienteRedException {
        try {
            if (Objects.nonNull(conexion)) {
                final ConexionActiva actual = conexion;
                canal.enviar(ProtocoloUdpMapper.DESCONECTAR, actual.direccion(), actual.destino().puerto());
            }
        } catch (final IOException excepcion) {
            LOG.warn(LOG_ERROR_RED, "desconexión", excepcion);
            throw new ClienteRedException("No se pudo notificar la desconexión.", excepcion);
        } finally {
            conexion = null;
            canal.cerrar();
        }
    }

    @Override
    public synchronized ResultadoPropina solicitarCalculo(final DatosPropina datos) throws ClienteRedException {
        if (Objects.isNull(conexion)) {
            throw new ClienteRedException("Debe conectarse antes de solicitar un cálculo.");
        }
        try {
            final ConexionActiva actual = conexion;
            final String respuesta = canal.intercambiar(protocolo.serializarCalculo(datos),
                    actual.direccion(), actual.destino().puerto());
            return protocolo.parsearCalculo(respuesta);
        } catch (final SocketTimeoutException excepcion) {
            LOG.warn(LOG_ERROR_RED, "cálculo por timeout", excepcion);
            throw new ClienteRedException("El servidor no respondió al cálculo.", excepcion);
        } catch (final IOException | RespuestaServidorException excepcion) {
            LOG.error(LOG_ERROR_RED, "cálculo", excepcion);
            throw new ClienteRedException("Falló la comunicación del cálculo.", excepcion);
        }
    }

    @Override
    public boolean estaConectado() {
        return Objects.nonNull(conexion) && canal.estaAbierto();
    }

    private record ConexionActiva(InetAddress direccion, DestinoServidor destino) {
        private ConexionActiva {
            Objects.requireNonNull(direccion, "La dirección es obligatoria.");
            Objects.requireNonNull(destino, "El destino es obligatorio.");
        }
    }
}
