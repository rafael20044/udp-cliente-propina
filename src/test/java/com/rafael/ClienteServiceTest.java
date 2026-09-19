package com.rafael;

import com.rafael.propina.aplicacion.dto.CalcularPropinaCommand;
import com.rafael.propina.aplicacion.dto.ConectarCommand;
import com.rafael.propina.aplicacion.excepciones.ClienteRedException;
import com.rafael.propina.aplicacion.mapper.ClienteMapper;
import com.rafael.propina.aplicacion.servicios.ClientePropinaService;
import com.rafael.propina.dominio.enums.EstadoConexion;
import com.rafael.propina.dominio.modelos.DatosPropina;
import com.rafael.propina.dominio.modelos.EventoCliente;
import com.rafael.propina.dominio.modelos.ResultadoPropina;
import com.rafael.propina.dominio.puertos.salida.ClienteUdpPort;
import com.rafael.propina.dominio.puertos.salida.PuertoNotificacionCliente;
import com.rafael.propina.dominio.vo.DestinoServidor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteServiceTest {
    @Test
    void orquestaConexionCalculoYDesconexion() {
        final UdpMemoria udp = new UdpMemoria();
        final NotificadorMemoria notificador = new NotificadorMemoria();
        final ClientePropinaService servicio = new ClientePropinaService(udp, notificador, new ClienteMapper(), Runnable::run);
        servicio.conectar(new ConectarCommand("localhost", 9876)).join();
        assertTrue(servicio.estaConectado());
        assertEquals(List.of(EstadoConexion.CONECTANDO, EstadoConexion.CONECTADO), notificador.estados);
        assertEquals(15000.0, servicio.calcular(new CalcularPropinaCommand(100000, 15)).join().valorPropina());
        assertEquals(1, notificador.resultados.size());
        servicio.desconectar().join();
        assertFalse(servicio.estaConectado());
        assertEquals(EstadoConexion.DESCONECTADO, notificador.estados.get(2));
    }

    @Test
    void notificaErroresAsincronos() {
        final UdpMemoria udp = new UdpMemoria();
        udp.fallar = true;
        final NotificadorMemoria notificador = new NotificadorMemoria();
        final ClientePropinaService servicio = new ClientePropinaService(udp, notificador, new ClienteMapper(), Runnable::run);
        assertThrows(CompletionException.class, () -> servicio.conectar(new ConectarCommand("localhost", 9876)).join());
        assertFalse(notificador.errores.isEmpty());
    }

    @Test
    void notificaErroresDeCalculoYDesconexion() {
        final UdpMemoria udp = new UdpMemoria();
        final NotificadorMemoria notificador = new NotificadorMemoria();
        final ClientePropinaService servicio = new ClientePropinaService(udp, notificador, new ClienteMapper(), Runnable::run);
        udp.fallarCalculo = true;
        assertThrows(CompletionException.class, () -> servicio.calcular(new CalcularPropinaCommand(100000, 15)).join());
        udp.fallarDesconexion = true;
        assertThrows(CompletionException.class, () -> servicio.desconectar().join());
        assertEquals(2, notificador.errores.size());
    }

    private static final class UdpMemoria implements ClienteUdpPort {
        private boolean conectado;
        private boolean fallar;
        private boolean fallarCalculo;
        private boolean fallarDesconexion;

        @Override
        public void conectar(final DestinoServidor destino) throws ClienteRedException {
            if (fallar)
                throw new ClienteRedException("fallo");
            conectado = true;
        }

        @Override
        public void desconectar() throws ClienteRedException {
            if (fallarDesconexion)
                throw new ClienteRedException("fallo desconexión");
            conectado = false;
        }

        @Override
        public ResultadoPropina solicitarCalculo(final DatosPropina datos) throws ClienteRedException {
            if (fallarCalculo)
                throw new ClienteRedException("fallo cálculo");
            return new ResultadoPropina(15000.0, 115000.0, "15000", "115000");
        }

        @Override
        public boolean estaConectado() {
            return conectado;
        }
    }

    private static final class NotificadorMemoria implements PuertoNotificacionCliente {
        private final List<EstadoConexion> estados = new ArrayList<>();
        private final List<ResultadoPropina> resultados = new ArrayList<>();
        private final List<String> errores = new ArrayList<>();

        @Override
        public void notificarEstado(final EstadoConexion estado, final String endpoint) {
            estados.add(estado);
        }

        @Override
        public void notificarEvento(final EventoCliente evento) {
        }

        @Override
        public void notificarResultado(final ResultadoPropina resultado) {
            resultados.add(resultado);
        }

        @Override
        public void notificarError(final String mensaje) {
            errores.add(mensaje);
        }
    }
}
