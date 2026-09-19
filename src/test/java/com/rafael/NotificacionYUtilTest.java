package com.rafael;

import com.rafael.propina.adaptadores.notificacion.AdaptadorNotificacionCliente;
import com.rafael.propina.adaptadores.notificacion.ObservadorCliente;
import com.rafael.propina.adaptadores.red.util.RedUtil;
import com.rafael.propina.dominio.enums.EstadoConexion;
import com.rafael.propina.dominio.modelos.EventoCliente;
import com.rafael.propina.dominio.modelos.ResultadoPropina;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificacionYUtilTest {
    @Test
    void publicaUnaVezYPermiteRemover() {
        // Arrange
        final AdaptadorNotificacionCliente adaptador = new AdaptadorNotificacionCliente();
        final AtomicInteger llamadas = new AtomicInteger();
        final ObservadorCliente observador = new ObservadorCliente() {
            @Override
            public void onEstado(final EstadoConexion estado, final String endpoint) {
                llamadas.incrementAndGet();
            }

            @Override
            public void onEvento(final EventoCliente evento) {
                llamadas.incrementAndGet();
            }

            @Override
            public void onResultado(final ResultadoPropina resultado) {
                llamadas.incrementAndGet();
            }

            @Override
            public void onError(final String mensaje) {
                llamadas.incrementAndGet();
            }
        };
        // Act
        adaptador.registrar(observador);
        adaptador.registrar(observador);
        adaptador.notificarEstado(EstadoConexion.CONECTADO, "host:9876");
        adaptador.notificarEvento(new EventoCliente("TEST", "evento"));
        adaptador.notificarResultado(new ResultadoPropina(15000.0, 115000.0, "15000", "115000"));
        adaptador.notificarError("error");
        // Assert
        assertThat(llamadas).hasValue(4);
        adaptador.remover(observador);
        adaptador.notificarError("ignorado");
        assertThat(llamadas).hasValue(4);
    }

    @Test
    void rechazaArgumentosNulosEnSusContratosPublicos() {
        // Arrange
        final AdaptadorNotificacionCliente adaptador = new AdaptadorNotificacionCliente();

        // Act y Assert
        assertThrows(NullPointerException.class, () -> adaptador.registrar(null));
        assertThrows(NullPointerException.class,
                () -> adaptador.notificarEstado(null, "servidor:9876"));
        assertThrows(NullPointerException.class,
                () -> adaptador.notificarEstado(EstadoConexion.CONECTADO, null));
        assertThrows(NullPointerException.class, () -> adaptador.notificarEvento(null));
        assertThrows(NullPointerException.class, () -> adaptador.notificarResultado(null));
        assertThrows(NullPointerException.class, () -> adaptador.notificarError(null));
    }

    @Test
    void obtieneIpLocal() {
        // Act y Assert
        assertFalse(RedUtil.obtenerIpLocal().isBlank());
    }
}
