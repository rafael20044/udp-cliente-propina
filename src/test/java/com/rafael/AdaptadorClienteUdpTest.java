package com.rafael;

import com.rafael.propina.adaptadores.red.AdaptadorClienteUdp;
import com.rafael.propina.adaptadores.red.CanalUdp;
import com.rafael.propina.adaptadores.red.ProtocoloUdpMapper;
import com.rafael.propina.aplicacion.excepciones.ClienteRedException;
import com.rafael.propina.dominio.modelos.DatosPropina;
import com.rafael.propina.dominio.vo.PorcentajePropina;
import com.rafael.propina.dominio.vo.DestinoServidor;
import com.rafael.propina.dominio.vo.ValorCuenta;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Objects;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdaptadorClienteUdpTest {
    private static final DestinoServidor DESTINO = new DestinoServidor("127.0.0.1", 9876);
    private static final DatosPropina DATOS = new DatosPropina(new ValorCuenta(100000.0), new PorcentajePropina(15.0));

    @Test
    void cierraElCanalCuandoLaConexionSuperaElTiempoLimite() {
        // Arrange
        final CanalControlado canal = new CanalControlado();
        canal.errorIntercambio = new SocketTimeoutException("timeout");
        final AdaptadorClienteUdp cliente = crearCliente(canal);

        // Act y Assert
        assertThatThrownBy(() -> cliente.conectar(DESTINO))
                .isInstanceOf(ClienteRedException.class)
                .hasMessage("El servidor no respondió dentro del tiempo esperado.")
                .hasCauseInstanceOf(SocketTimeoutException.class);
        assertThat(canal.estaAbierto()).isFalse();
        assertThat(cliente.estaConectado()).isFalse();
    }

    @Test
    void traduceUnaRespuestaDeConexionInvalida() {
        // Arrange
        final CanalControlado canal = new CanalControlado();
        canal.respuesta = "RESPUESTA_DESCONOCIDA";
        final AdaptadorClienteUdp cliente = crearCliente(canal);

        // Act y Assert
        assertThatThrownBy(() -> cliente.conectar(DESTINO))
                .isInstanceOf(ClienteRedException.class)
                .hasMessage("No se pudo conectar con 127.0.0.1:9876.");
        assertThat(canal.estaAbierto()).isFalse();
    }

    @Test
    void traduceUnFalloAlAbrirElCanal() {
        // Arrange
        final CanalControlado canal = new CanalControlado();
        final IOException errorEsperado = new IOException("sin socket");
        canal.errorApertura = errorEsperado;
        final AdaptadorClienteUdp cliente = crearCliente(canal);

        // Act y Assert
        assertThatThrownBy(() -> cliente.conectar(DESTINO))
                .isInstanceOf(ClienteRedException.class)
                .hasCause(errorEsperado);
        assertThat(canal.estaAbierto()).isFalse();
    }

    @Test
    void siempreLiberaElEstadoAunqueFalleLaDesconexion() throws ClienteRedException {
        // Arrange
        final CanalControlado canal = new CanalControlado();
        final AdaptadorClienteUdp cliente = crearCliente(canal);
        cliente.conectar(DESTINO);
        canal.errorEnvio = new IOException("sin red");

        // Act y Assert
        assertThatThrownBy(cliente::desconectar)
                .isInstanceOf(ClienteRedException.class)
                .hasMessage("No se pudo notificar la desconexión.");
        assertThat(cliente.estaConectado()).isFalse();
        assertThat(canal.estaAbierto()).isFalse();
    }

    @Test
    void desconectarSinConexionEsIdempotente() throws ClienteRedException {
        // Arrange
        final CanalControlado canal = new CanalControlado();
        final AdaptadorClienteUdp cliente = crearCliente(canal);

        // Act
        cliente.desconectar();

        // Assert
        assertThat(canal.envios).isZero();
        assertThat(cliente.estaConectado()).isFalse();
    }

    @Test
    void traduceTimeoutYRespuestaInvalidaDelCalculo() throws ClienteRedException {
        // Arrange
        final CanalControlado canal = new CanalControlado();
        final AdaptadorClienteUdp cliente = crearCliente(canal);
        cliente.conectar(DESTINO);
        canal.errorIntercambio = new SocketTimeoutException("timeout");

        // Act y Assert
        assertThatThrownBy(() -> cliente.solicitarCalculo(DATOS))
                .isInstanceOf(ClienteRedException.class)
                .hasMessage("El servidor no respondió al cálculo.");

        canal.errorIntercambio = null;
        canal.respuesta = "FORMATO_INVALIDO";
        assertThatThrownBy(() -> cliente.solicitarCalculo(DATOS))
                .isInstanceOf(ClienteRedException.class)
                .hasMessage("Falló la comunicación del cálculo.");
    }

    private static AdaptadorClienteUdp crearCliente(final CanalUdp canal) {
        return new AdaptadorClienteUdp(canal, new ProtocoloUdpMapper());
    }

    private static final class CanalControlado extends CanalUdp {
        private boolean abierto;
        private String respuesta = "CONECTADO_OK;listo";
        private IOException errorApertura;
        private IOException errorIntercambio;
        private IOException errorEnvio;
        private int envios;

        private CanalControlado() {
            super(100);
        }

        @Override
        public synchronized void abrir() throws IOException {
            if (Objects.nonNull(errorApertura)) {
                throw errorApertura;
            }
            abierto = true;
        }

        @Override
        public synchronized void cerrar() {
            abierto = false;
        }

        @Override
        public synchronized boolean estaAbierto() {
            return abierto;
        }

        @Override
        public synchronized String intercambiar(
                final String mensaje, final InetAddress host, final int puerto) throws IOException {
            if (Objects.nonNull(errorIntercambio)) {
                throw errorIntercambio;
            }
            return respuesta;
        }

        @Override
        public synchronized void enviar(
                final String mensaje, final InetAddress host, final int puerto) throws IOException {
            envios++;
            if (Objects.nonNull(errorEnvio)) {
                throw errorEnvio;
            }
        }
    }
}
