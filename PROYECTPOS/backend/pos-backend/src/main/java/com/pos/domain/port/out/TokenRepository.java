package com.pos.domain.port.out;

public interface TokenRepository {
    void invalidar(String token);
    boolean esValido(String token);
}
