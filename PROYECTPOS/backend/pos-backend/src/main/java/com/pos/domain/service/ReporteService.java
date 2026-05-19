package com.pos.domain.service;

import com.pos.domain.exception.ReporteValidacionException;
import com.pos.domain.model.ReporteCierre;
import com.pos.domain.port.in.GenerarReporteUseCase;
import com.pos.domain.port.out.VentaRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ReporteService implements GenerarReporteUseCase {

    private static final DateTimeFormatter ISO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE;

    private final VentaRepository ventaRepository;

    public ReporteService(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Override
    public ReporteCierre generar(String fechaDesde, String fechaHasta) {
        if (fechaDesde == null || fechaDesde.isBlank()
                || fechaHasta == null || fechaHasta.isBlank()) {
            throw new ReporteValidacionException("fechaDesde y fechaHasta son obligatorias.");
        }

        String desdeStr = fechaDesde.trim();
        String hastaStr = fechaHasta.trim();

        LocalDate desde;
        LocalDate hasta;
        try {
            desde = LocalDate.parse(desdeStr, ISO_FECHA);
            hasta = LocalDate.parse(hastaStr, ISO_FECHA);
        } catch (DateTimeParseException e) {
            throw new ReporteValidacionException(
                    "Las fechas deben estar en formato ISO (yyyy-MM-dd).");
        }

        if (desde.isAfter(hasta)) {
            throw new ReporteValidacionException(
                    "fechaDesde no puede ser posterior a fechaHasta.");
        }

        return ventaRepository.generarReporte(desdeStr, hastaStr);
    }
}
