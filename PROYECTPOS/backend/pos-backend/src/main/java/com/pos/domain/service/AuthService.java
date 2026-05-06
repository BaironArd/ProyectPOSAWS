package com.pos.domain.service;

import com.pos.domain.exception.CredencialesInvalidasException;
import com.pos.domain.model.SesionToken;
import com.pos.domain.port.in.LoginUseCase;
import com.pos.domain.port.in.LogoutUseCase;
import com.pos.domain.port.out.TokenRepository;
import com.pos.domain.port.out.UsuarioRepository;

/**
 * Servicio de dominio — POJO sin anotaciones de Spring.
 * La generación del JWT se delega a JwtService (infraestructura) via callback.
 */
public class AuthService implements LoginUseCase, LogoutUseCase {

    private final UsuarioRepository usuarioRepository;
    private final TokenRepository tokenRepository;
    private final JwtTokenGenerator jwtGenerator;

    public AuthService(UsuarioRepository usuarioRepository,
                       TokenRepository tokenRepository,
                       JwtTokenGenerator jwtGenerator) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.jwtGenerator = jwtGenerator;
    }

    @Override
    public SesionToken login(String usuario, String contrasena) {
        var user = usuarioRepository.findByUsuario(usuario)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!jwtGenerator.verificarPassword(contrasena, user.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        String token = jwtGenerator.generarToken(user.getUsuario(), user.getRol().name());
        return new SesionToken(token, user.getUsuario(), user.getRol(), 28800L);
    }

    @Override
    public void logout(String token) {
        tokenRepository.invalidar(token);
    }

    /** Puerto de salida para generación de JWT — implementado en infraestructura. */
    public interface JwtTokenGenerator {
        String generarToken(String usuario, String rol);
        boolean verificarPassword(String rawPassword, String encodedPassword);
    }
}
