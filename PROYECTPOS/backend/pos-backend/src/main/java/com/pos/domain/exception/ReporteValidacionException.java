package com.pos.domain.exception;

/**
 * Validación de parámetros del reporte de cierre (SPEC-BE-011) — fechaDesde/fechaHasta.
 * Se traduce a HTTP 400 {@code VALIDACION_FALLIDA}.
 */
public class ReporteValidacionException extends RuntimeException {

    public ReporteValidacionException(String message) {
        super(message);
    }
}
