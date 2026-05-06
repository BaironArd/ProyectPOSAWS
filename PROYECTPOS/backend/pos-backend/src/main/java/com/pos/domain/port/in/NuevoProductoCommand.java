package com.pos.domain.port.in;

public record NuevoProductoCommand(
        String nombre,
        long precio,
        int stock,
        String categoria
) {}
