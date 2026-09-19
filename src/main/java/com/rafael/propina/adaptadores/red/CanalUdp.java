package com.rafael.propina.adaptadores.red;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CanalUdp {
    private static final int BUFFER_SIZE = 2048;
    private final int timeoutMs;
    private DatagramSocket socket;

    public CanalUdp(final int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public synchronized void abrir() throws IOException {
        cerrar();
        final DatagramSocket nuevoSocket = new DatagramSocket();
        try {
            nuevoSocket.setSoTimeout(timeoutMs);
            socket = nuevoSocket;
        } catch (final IOException | RuntimeException excepcion) {
            nuevoSocket.close();
            throw excepcion;
        }
    }

    public synchronized void cerrar() {
        if (Objects.nonNull(socket) && !socket.isClosed()) {
            socket.close();
        }
        socket = null;
    }

    public synchronized boolean estaAbierto() {
        return Objects.nonNull(socket) && !socket.isClosed();
    }

    public synchronized String intercambiar(final String mensaje, final InetAddress host, final int puerto)
            throws IOException {
        if (!estaAbierto()) {
            throw new IOException("El canal UDP está cerrado.");
        }
        final byte[] salida = mensaje.getBytes(StandardCharsets.UTF_8);
        final DatagramSocket socketAbierto = socket;
        socketAbierto.send(new DatagramPacket(salida, salida.length, host, puerto));
        final DatagramPacket entrada = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        socketAbierto.receive(entrada);
        return new String(entrada.getData(), 0, entrada.getLength(), StandardCharsets.UTF_8).trim();
    }

    public synchronized void enviar(final String mensaje, final InetAddress host, final int puerto)
            throws IOException {
        if (!estaAbierto()) {
            return;
        }
        final byte[] salida = mensaje.getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(salida, salida.length, host, puerto));
    }
}
