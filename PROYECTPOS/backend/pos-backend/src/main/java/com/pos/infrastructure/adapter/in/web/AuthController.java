package com.pos.infrastructure.adapter.in.web;

import com.pos.domain.model.SesionToken;
import com.pos.domain.service.AuthService;
import com.pos.infrastructure.adapter.in.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints para autenticación de usuarios")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
               description = "Autentica un usuario y retorna un token JWT")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso",
                        content = @Content(mediaType = "application/json",
                                         schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                        content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ApiResponse<SesionTokenResponse>> login(
            @RequestBody @Valid LoginRequest request) {
        SesionToken token = authService.login(request.usuario(), request.contrasena());
        return ResponseEntity.ok(ApiResponse.of(new SesionTokenResponse(
                token.token(), token.usuario(), token.rol().name(), token.expiresIn())));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión",
               description = "Invalida el token JWT actual")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Logout exitoso")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7));
        }
        return ResponseEntity.noContent().build();
    }

    public record LoginRequest(
            @NotBlank String usuario,
            @NotBlank String contrasena
    ) {}

    public record SesionTokenResponse(
            String token,
            String usuario,
            String rol,
            long expiresIn
    ) {}
}
