package com.pos.domain.model;

public record SesionToken(
        String token,
        String usuario,
        Rol rol,
        long expiresIn
) {}
