package com.pos.domain.service;

import com.pos.domain.exception.ReporteValidacionException;
import com.pos.domain.model.Dinero;
import com.pos.domain.model.ReporteCierre;
import com.pos.domain.port.out.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteService = new ReporteService(ventaRepository);
    }

    @Test
    void generar_cuandoDesdeEsPosteriorAHasta_lanzaExcepcion() {
        assertThatThrownBy(() -> reporteService.generar("2025-06-10", "2025-06-01"))
                .isInstanceOf(ReporteValidacionException.class)
                .hasMessageContaining("fechaDesde");
    }

    @Test
    void generar_cuandoFormatoInvalido_lanzaExcepcion() {
        assertThatThrownBy(() -> reporteService.generar("31/05/2025", "2025-06-01"))
                .isInstanceOf(ReporteValidacionException.class)
                .hasMessageContaining("yyyy-MM-dd");
    }

    @Test
    void generar_cuandoFechaDesdeEsBlanca_lanzaExcepcion() {
        assertThatThrownBy(() -> reporteService.generar("   ", "2025-06-01"))
                .isInstanceOf(ReporteValidacionException.class)
                .hasMessageContaining("obligatorias");
    }

    @Test
    void generar_cuandoRangoValido_delegaAlRepositorio() {
        ReporteCierre esperado = new ReporteCierre(
                "2025-01-01",
                "2025-01-05",
                0,
                0,
                Dinero.CERO,
                Dinero.CERO,
                Dinero.CERO,
                List.of());
        when(ventaRepository.generarReporte(eq("2025-01-01"), eq("2025-01-05")))
                .thenReturn(esperado);

        ReporteCierre r = reporteService.generar("2025-01-01", "2025-01-05");

        verify(ventaRepository).generarReporte("2025-01-01", "2025-01-05");
        assertThat(r).isSameAs(esperado);
    }
}
