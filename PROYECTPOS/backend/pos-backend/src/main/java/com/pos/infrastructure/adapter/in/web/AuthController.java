package com.pos.infrastructure.adapter.in.web;

import com.pos.domain.model.SesionToken;
import com.pos.domain.service.AuthService;
import com.pos.infrastructure.adapter.in.web.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<SesionTokenResponse>> login(
            @RequestBody @Valid LoginRequest request) {
        SesionToken token = authService.login(request.usuario(), request.contrasena());
        return ResponseEntity.ok(ApiResponse.of(new SesionTokenResponse(
                token.token(), token.usuario(), token.rol().name(), token.expiresIn())));
    }

    @PostMapping("/logout")
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
