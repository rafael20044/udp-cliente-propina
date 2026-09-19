package com.rafael.propina.adaptadores.red;

import com.rafael.propina.dominio.excepciones.RespuestaServidorException;
import com.rafael.propina.dominio.modelos.DatosPropina;
import com.rafael.propina.dominio.modelos.ResultadoPropina;

import java.util.Locale;
import java.util.Objects;

public final class ProtocoloUdpMapper {
    public static final String CONECTAR = "CONECTAR";
    public static final String DESCONECTAR = "DESCONECTAR";

    public String serializarCalculo(final DatosPropina datos) {
        Objects.requireNonNull(datos, "Los datos de la propina son obligatorios.");
        return String.format(Locale.US, "CALCULAR;%.2f;%.2f", datos.valorCuenta().valor(), datos.porcentajePropina().valor());
    }

    public void validarConexion(final String respuesta) {
        if (Objects.isNull(respuesta) || !respuesta.startsWith("CONECTADO_OK;")) {
            throw new RespuestaServidorException("Respuesta de conexión inesperada: " + respuesta);
        }
    }

    public ResultadoPropina parsearCalculo(final String respuesta) {
        if (Objects.isNull(respuesta) || respuesta.isBlank()) {
            throw new RespuestaServidorException("El servidor envió una respuesta vacía.");
        }
        if (respuesta.startsWith("ERROR;")) {
            throw new RespuestaServidorException("Error del servidor: " + respuesta.substring(6));
        }
        final String[] partes = respuesta.split(";", -1);
        if (partes.length != 3 || !"OK_CALCULO".equals(partes[0])) {
            throw new RespuestaServidorException("Respuesta de cálculo no estructurada: " + respuesta);
        }
        try {
            final double valorPropina = Double.parseDouble(partes[1].replace(',', '.'));
            final double totalPagar = Double.parseDouble(partes[2].replace(',', '.'));
            return new ResultadoPropina(valorPropina, totalPagar, partes[1], partes[2]);
        } catch (final NumberFormatException excepcion) {
            throw new RespuestaServidorException("Los valores recibidos no son numéricos.");
        }
    }
}
