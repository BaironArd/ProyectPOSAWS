package com.pos.infrastructure.adapter.in.web;

import com.pos.domain.model.Dinero;
import com.pos.domain.model.ReporteCierre;
import com.pos.domain.model.VentasPorCajero;
import com.pos.domain.port.out.TokenRepository;
import com.pos.domain.port.out.VentaRepository;
import com.pos.domain.service.ReporteService;
import com.pos.infrastructure.config.SecurityConfig;
import com.pos.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReporteController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, ReporteService.class})
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VentaRepository ventaRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenRepository tokenRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void cierre_comAdmin_retorna200() throws Exception {
        ReporteCierre reporte = new ReporteCierre(
                "2025-01-01",
                "2025-01-31",
                12,
                1,
                Dinero.dePesos(500000),
                Dinero.dePesos(40000),
                Dinero.dePesos(460000),
                List.of(new VentasPorCajero("cajero01", 12, Dinero.dePesos(500000))));
        when(ventaRepository.generarReporte(eq("2025-01-01"), eq("2025-01-31"))).thenReturn(reporte);

        mockMvc.perform(get("/api/v1/reportes/cierre")
                        .param("fechaDesde", "2025-01-01")
                        .param("fechaHasta", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalVentas").value(12))
                .andExpect(jsonPath("$.data.montoNeto").value(460000))
                .andExpect(jsonPath("$.data.ventasPorCajero[0].usuario").value("cajero01"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cierre_sinOperaciones_retorna200YMontosEnCero() throws Exception {
        ReporteCierre vacio = new ReporteCierre(
                "2025-06-01",
                "2025-06-02",
                0,
                0,
                Dinero.CERO,
                Dinero.CERO,
                Dinero.CERO,
                List.of());
        when(ventaRepository.generarReporte(eq("2025-06-01"), eq("2025-06-02"))).thenReturn(vacio);

        mockMvc.perform(get("/api/v1/reportes/cierre")
                        .param("fechaDesde", "2025-06-01")
                        .param("fechaHasta", "2025-06-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalVentas").value(0))
                .andExpect(jsonPath("$.data.totalDevueltas").value(0))
                .andExpect(jsonPath("$.data.montoTotal").value(0))
                .andExpect(jsonPath("$.data.montoDevuelto").value(0))
                .andExpect(jsonPath("$.data.montoNeto").value(0))
                .andExpect(jsonPath("$.data.ventasPorCajero").isArray())
                .andExpect(jsonPath("$.data.ventasPorCajero").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cierre_fechaDesdePosteriorAHasta_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/cierre")
                        .param("fechaDesde", "2025-06-10")
                        .param("fechaHasta", "2025-06-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.codigo").value("VALIDACION_FALLIDA"));

        verify(ventaRepository, never()).generarReporte(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cierre_formatoFechaInvalido_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/cierre")
                        .param("fechaDesde", "01-06-2025")
                        .param("fechaHasta", "2025-06-02"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.codigo").value("VALIDACION_FALLIDA"));

        verify(ventaRepository, never()).generarReporte(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cierre_recortaEspaciosEnFechas() throws Exception {
        ReporteCierre vacio = new ReporteCierre(
                "2025-06-01",
                "2025-06-02",
                0,
                0,
                Dinero.CERO,
                Dinero.CERO,
                Dinero.CERO,
                List.of());
        when(ventaRepository.generarReporte(eq("2025-06-01"), eq("2025-06-02"))).thenReturn(vacio);

        mockMvc.perform(get("/api/v1/reportes/cierre")
                        .param("fechaDesde", " 2025-06-01 ")
                        .param("fechaHasta", " 2025-06-02 "))
                .andExpect(status().isOk());

        verify(ventaRepository).generarReporte("2025-06-01", "2025-06-02");
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void cierre_sinAdmin_retorna403() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/cierre")
                        .param("fechaDesde", "2025-01-01")
                        .param("fechaHasta", "2025-01-31"))
                .andExpect(status().isForbidden());
    }
}
