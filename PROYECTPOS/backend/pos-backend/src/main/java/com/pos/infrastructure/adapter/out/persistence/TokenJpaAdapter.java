package com.pos.infrastructure.adapter.out.persistence;

import com.pos.domain.port.out.TokenRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria de la blacklist de tokens JWT.
 * En producción se reemplazaría por Redis o una tabla en BD.
 */
@Repository
public class TokenJpaAdapter implements TokenRepository {

    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    @Override
    public void invalidar(String token) {
        blacklist.add(token);
    }

    @Override
    public boolean esValido(String token) {
        return !blacklist.contains(token);
    }
}
