package com.pos.domain.port.in;

public record ActualizarProductoCommand(
        String nombre,
        long precio,
        int stock,
        String categoria
) {}
