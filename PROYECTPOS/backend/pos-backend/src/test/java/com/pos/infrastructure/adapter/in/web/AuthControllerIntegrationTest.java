package com.pos.infrastructure.adapter.in.web;

import com.pos.domain.exception.CredencialesInvalidasException;
import com.pos.domain.port.out.TokenRepository;
import com.pos.domain.service.AuthService;
import com.pos.infrastructure.config.SecurityConfig;
import com.pos.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@SuppressWarnings("null")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenRepository tokenRepository;

    @Test
    void login_conCredencialesValidas_retornaToken() throws Exception {
        // Given
        String usuario = "cajero01";
        String contrasena = "1234";
        String loginRequest = """
            {
                "usuario": "%s",
                "contrasena": "%s"
            }
            """.formatted(usuario, contrasena);

        when(authService.login(anyString(), anyString()))
            .thenReturn(new com.pos.domain.model.SesionToken(
                "jwt-token-123",
                usuario,
                com.pos.domain.model.Rol.CAJERO,
                28800L
            ));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.token").value("jwt-token-123"))
            .andExpect(jsonPath("$.data.usuario").value(usuario))
            .andExpect(jsonPath("$.data.rol").value("CAJERO"))
            .andExpect(jsonPath("$.data.expiresIn").value(28800));
    }

    @Test
    void login_conCredencialesInvalidas_retorna401() throws Exception {
        // Given
        String loginRequest = """
            {
                "usuario": "usuario_invalido",
                "contrasena": "contrasena_invalida"
            }
            """;

        when(authService.login(anyString(), anyString()))
            .thenThrow(new CredencialesInvalidasException());

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
                .with(csrf()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    @WithMockUser
    void logout_conTokenValido_retorna204() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("cajero01");
        when(claims.get(eq("rol"), eq(String.class))).thenReturn("CAJERO");
        when(tokenRepository.esValido("jwt-token-123")).thenReturn(true);
        when(jwtService.validarToken("jwt-token-123")).thenReturn(claims);

        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer jwt-token-123")
                .with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    void login_sinUsuario_retorna400() throws Exception {
        // Given
        String loginRequest = """
            {
                "contrasena": "1234"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void login_sinContrasena_retorna400() throws Exception {
        // Given
        String loginRequest = """
            {
                "usuario": "cajero01"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest)
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }
}