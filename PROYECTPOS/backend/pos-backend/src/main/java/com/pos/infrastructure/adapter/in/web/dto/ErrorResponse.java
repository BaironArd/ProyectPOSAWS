package com.pos.infrastructure.adapter.in.web.dto;

import java.time.Instant;

public record ErrorResponse(ErrorDetail error) {

    public record ErrorDetail(String codigo, String mensaje, Instant timestamp) {}

    public static ErrorResponse of(String codigo, String mensaje) {
        return new ErrorResponse(new ErrorDetail(codigo, mensaje, Instant.now()));
    }
}
