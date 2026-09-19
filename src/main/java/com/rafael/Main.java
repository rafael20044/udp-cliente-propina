package com.rafael;

import com.rafael.propina.adaptadores.notificacion.AdaptadorNotificacionCliente;
import com.rafael.propina.adaptadores.red.AdaptadorClienteUdp;
import com.rafael.propina.adaptadores.red.CanalUdp;
import com.rafael.propina.adaptadores.red.ProtocoloUdpMapper;
import com.rafael.propina.aplicacion.mapper.ClienteMapper;
import com.rafael.propina.aplicacion.servicios.ClientePropinaService;
import com.rafael.propina.entrypoint.gui.ClienteFrame;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
        // Clase de entrada estática
    }

    public static void main(final String[] args) {
        final AdaptadorNotificacionCliente notificador = new AdaptadorNotificacionCliente();
        final AdaptadorClienteUdp udp = new AdaptadorClienteUdp(new CanalUdp(3000), new ProtocoloUdpMapper());
        final ExecutorService executor = Executors.newSingleThreadExecutor(tarea -> {
            final Thread hilo = new Thread(tarea, "cliente-udp");
            hilo.setDaemon(true);
            return hilo;
        });
        final ClientePropinaService servicio = new ClientePropinaService(
                udp, notificador, new ClienteMapper(), executor);
        SwingUtilities.invokeLater(() -> {
            final ClienteFrame frame = new ClienteFrame(servicio, servicio);
            notificador.registrar(frame);
            frame.setVisible(true);
        });
    }
}
