package com.rafael.propina.aplicacion.puertos.entrada;

import com.rafael.propina.aplicacion.dto.CalcularPropinaCommand;
import com.rafael.propina.dominio.modelos.ResultadoPropina;
import java.util.concurrent.CompletableFuture;

public interface CalcularPropinaInputPort {
    CompletableFuture<ResultadoPropina> calcular(CalcularPropinaCommand comando);
}
