package com.pos.infrastructure.security;

import com.pos.domain.port.out.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    public JwtAuthFilter(JwtService jwtService, TokenRepository tokenRepository) {
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (!tokenRepository.esValido(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":{\"codigo\":\"TOKEN_INVALIDO\",\"mensaje\":\"Token invalidado.\"}}");
                return;
            }

            Claims claims = jwtService.validarToken(token);
            String usuario = claims.getSubject();
            String rol = claims.get("rol", String.class);

            var auth = new UsernamePasswordAuthenticationToken(
                    usuario, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":{\"codigo\":\"TOKEN_INVALIDO\",\"mensaje\":\"El token JWT es inválido o ha expirado.\"}}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
