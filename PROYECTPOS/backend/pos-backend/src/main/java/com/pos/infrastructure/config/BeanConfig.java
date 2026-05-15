package com.pos.infrastructure.config;

import com.pos.domain.port.in.*;
import com.pos.domain.port.out.*;
import com.pos.domain.service.*;
import com.pos.infrastructure.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Punto de ensamblaje de la arquitectura hexagonal.
 *
 * Estrategia: cada servicio de dominio se registra UNA SOLA VEZ con el tipo
 * concreto. Los controllers inyectan las interfaces — Spring resuelve
 * automáticamente porque cada interfaz tiene exactamente una implementación
 * en el contexto.
 */
@Configuration
public class BeanConfig {

    @Bean
    public CalculadoraVenta calculadoraVenta() {
        return new CalculadoraVenta();
    }

    /**
     * ProductoService implementa BuscarProductosUseCase + ObtenerProductoUseCase.
     * Al registrarlo como ProductoService, Spring lo usa para ambas interfaces.
     */
    @Bean
    public ProductoService productoService(ProductoRepository productoRepository) {
        return new ProductoService(productoRepository);
    }

    /**
     * InventarioService implementa GestionarProductoUseCase (solo ADMIN).
     */
    @Bean
    public InventarioService inventarioService(ProductoRepository productoRepository) {
        return new InventarioService(productoRepository);
    }

    /**
     * VentaService implementa ConfirmarVentaUseCase + ObtenerVentaUseCase.
     */
    @Bean
    public VentaService ventaService(ProductoRepository productoRepository,
                                     VentaRepository ventaRepository,
                                     CalculadoraVenta calculadora) {
        return new VentaService(productoRepository, ventaRepository, calculadora);
    }

    @Bean
    public ListarVentasService listarVentasService(VentaRepository ventaRepository) {
        return new ListarVentasService(ventaRepository);
    }

    /**
     * AuthService implementa LoginUseCase + LogoutUseCase.
     */
    @Bean
    public AuthService authService(UsuarioRepository usuarioRepository,
                                   TokenRepository tokenRepository,
                                   JwtService jwtService) {
        return new AuthService(usuarioRepository, tokenRepository, jwtService);
    }

    @Bean
    public DevolucionService devolucionService(VentaRepository ventaRepository,
                                               ProductoRepository productoRepository,
                                               CalculadoraVenta calculadora) {
        return new DevolucionService(ventaRepository, productoRepository, calculadora);
    }

    @Bean
    public ReporteService reporteService(VentaRepository ventaRepository) {
        return new ReporteService(ventaRepository);
    }
}
