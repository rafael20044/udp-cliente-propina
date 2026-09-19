package com.rafael;

import com.rafael.propina.adaptadores.red.ProtocoloUdpMapper;
import com.rafael.propina.aplicacion.dto.CalcularPropinaCommand;
import com.rafael.propina.aplicacion.dto.ConectarCommand;
import com.rafael.propina.aplicacion.mapper.ClienteMapper;
import com.rafael.propina.dominio.excepciones.PorcentajePropinaIncorrectoException;
import com.rafael.propina.dominio.excepciones.DestinoIncorrectoException;
import com.rafael.propina.dominio.excepciones.ValorCuentaIncorrectoException;
import com.rafael.propina.dominio.excepciones.RespuestaServidorException;
import com.rafael.propina.dominio.modelos.DatosPropina;
import com.rafael.propina.dominio.modelos.ResultadoPropina;
import com.rafael.propina.dominio.vo.PorcentajePropina;
import com.rafael.propina.dominio.vo.ValorCuenta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DominioYProtocoloTest {
    @Test
    void validaValores() {
        assertThrows(ValorCuentaIncorrectoException.class, () -> new ValorCuenta(0));
        assertThrows(ValorCuentaIncorrectoException.class, () -> new ValorCuenta(Double.NaN));
        assertThrows(PorcentajePropinaIncorrectoException.class, () -> new PorcentajePropina(-1));
        assertThrows(PorcentajePropinaIncorrectoException.class, () -> new PorcentajePropina(Double.POSITIVE_INFINITY));
        assertThrows(DestinoIncorrectoException.class,
                () -> new ClienteMapper().toDestino(new ConectarCommand(" ", 9876)));
        assertThrows(DestinoIncorrectoException.class,
                () -> new ClienteMapper().toDestino(new ConectarCommand("host", 80)));
    }

    @Test
    void validaIntegridadDelResultado() {
        assertThrows(RespuestaServidorException.class, () -> new ResultadoPropina(Double.NaN, 115000.0, "15000", "115000"));
        assertThrows(RespuestaServidorException.class, () -> new ResultadoPropina(15000.0, Double.NaN, "15000", "115000"));
        assertThrows(RespuestaServidorException.class, () -> new ResultadoPropina(15000.0, 115000.0, " ", "115000"));
        assertThrows(RespuestaServidorException.class, () -> new ResultadoPropina(15000.0, 115000.0, "15000", null));
    }

    @Test
    void transformaComandos() {
        final ClienteMapper mapper = new ClienteMapper();
        assertEquals("localhost:9876", mapper.toDestino(new ConectarCommand(" localhost ", 9876)).endpoint());
        final DatosPropina datos = mapper.toDomain(new CalcularPropinaCommand(100000.0, 15.0));
        assertEquals("CALCULAR;100000.00;15.00", new ProtocoloUdpMapper().serializarCalculo(datos));
    }

    @Test
    void parseaResultadoYErrores() {
        final ProtocoloUdpMapper protocolo = new ProtocoloUdpMapper();
        protocolo.validarConexion("CONECTADO_OK;listo");
        final var resultado = protocolo.parsearCalculo("OK_CALCULO;15000;115000");
        assertEquals(15000.0, resultado.valorPropina());
        assertEquals(115000.0, resultado.totalPagar());
        assertThrows(RespuestaServidorException.class, () -> protocolo.validarConexion("ERROR"));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo(null));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("  "));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("ERROR;fallo"));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("OTRA"));
        assertThrows(RespuestaServidorException.class, () -> protocolo.parsearCalculo("OK_CALCULO;x;115000"));
    }
}
