package com.pos.infrastructure.adapter.in.web.dto;

public record ProductoResponse(
        Long id,
        String nombre,
        long precio,
        int stock,
        String categoria,
        boolean activo
) {}
