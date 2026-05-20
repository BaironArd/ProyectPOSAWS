package com.pos.infrastructure.adapter.in.web;

import com.pos.domain.exception.*;
import com.pos.domain.model.*;
import com.pos.domain.port.in.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentaController.class)
@Import(GlobalExceptionHandler.class)
@SuppressWarnings("null")
class VentaControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ConfirmarVentaUseCase confirmarVenta;
    @MockBean private ObtenerVentaUseCase obtenerVenta;
    @MockBean private ListarVentasUseCase listarVentas;
    @MockBean private DevolverVentaUseCase devolverVenta;

    private Venta ventaMock() {
        ResumenVenta resumen = new ResumenVenta(
                Dinero.dePesos(115000), Dinero.dePesos(21850),
                Dinero.dePesos(136850), Dinero.dePesos(150000),
                Dinero.dePesos(13150));
        ItemVenta item = new ItemVenta(1L, "Mouse Óptico USB", 2, Dinero.dePesos(30000));
        return new Venta("VNT-20250115-001", List.of(item), resumen,
                EstadoVenta.COMPLETADA, Instant.now(), "key-001", "cajero01", List.of());
    }

    private String requestBody() throws Exception {
        return """
            {
              "items": [{"productoId": 1, "cantidad": 2}],
              "montoPagado": 150000,
              "idempotencyKey": "key-001",
              "metodoPago": "EFECTIVO"
            }
            """;
    }

    // ---- SPEC-BE-003: Confirmar venta ----

    @Test
    @WithMockUser
    void confirmar_exitoso_retorna201ConVentaId() throws Exception {
        when(confirmarVenta.confirmar(any())).thenReturn(ventaMock());

        mockMvc.perform(post("/api/v1/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ventaId").value("VNT-20250115-001"))
                .andExpect(jsonPath("$.data.resumen.cambio").value(13150))
                .andExpect(jsonPath("$.data.estado").value("COMPLETADA"));
    }

    @Test
    @WithMockUser
    void confirmar_carritoVacio_retorna422() throws Exception {
        when(confirmarVenta.confirmar(any())).thenThrow(new CarritoVacioException());

        mockMvc.perform(post("/api/v1/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.codigo").value("CARRITO_VACIO"));
    }

    @Test
    @WithMockUser
    void confirmar_montoInsuficiente_retorna422() throws Exception {
        when(confirmarVenta.confirmar(any()))
                .thenThrow(new MontoInsuficienteException(Dinero.dePesos(136850), Dinero.dePesos(50000)));

        mockMvc.perform(post("/api/v1/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.codigo").value("VENTA_MONTO_INSUFICIENTE"));
    }

    @Test
    @WithMockUser
    void confirmar_productoInexistente_retorna404() throws Exception {
        when(confirmarVenta.confirmar(any()))
                .thenThrow(new ProductoNotFoundException(99L));

        mockMvc.perform(post("/api/v1/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.codigo").value("PRODUCTO_NO_ENCONTRADO"));
    }

    @Test
    @WithMockUser
    void confirmar_stockInsuficiente_retorna422() throws Exception {
        when(confirmarVenta.confirmar(any()))
                .thenThrow(new StockInsuficienteException(1L, 5, 2));

        mockMvc.perform(post("/api/v1/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.codigo").value("STOCK_INSUFICIENTE"));
    }

    @Test
    @WithMockUser
    void confirmar_cantidadInvalida_retorna400() throws Exception {
        String bodyInvalido = """
            {
              "items": [{"productoId": 1, "cantidad": -1}],
              "montoPagado": 150000,
              "idempotencyKey": "key-001"
            }
            """;

        mockMvc.perform(post("/api/v1/ventas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.codigo").value("VALIDACION_FALLIDA"));
    }

    // ---- SPEC-BE-006: Listar ventas ----

    @Test
    @WithMockUser
    void listar_retorna200ConPagina() throws Exception {
        var resumen = new ResumenVentaSimple(
                "VNT-LIST-001",
                Instant.parse("2025-01-15T15:30:00Z"),
                Dinero.dePesos(136850),
                Dinero.dePesos(0),
                3,
                EstadoVenta.COMPLETADA,
                Dinero.dePesos(115000),
                Dinero.dePesos(21850),
                Dinero.dePesos(150000),
                Dinero.dePesos(13150),
                null,
                null,
                List.of());
        when(listarVentas.listar(0, 20, null, null))
                .thenReturn(PageResponse.of(List.of(resumen), 1, 0, 20));

        mockMvc.perform(get("/api/v1/ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].ventaId").value("VNT-LIST-001"))
                .andExpect(jsonPath("$.data.items[0].total").value(136850))
                .andExpect(jsonPath("$.data.items[0].estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20));
    }

    // ---- SPEC-BE-009: Devolución ----

    @Test
    @WithMockUser
    void devolucion_sinItems_retorna200DevolucionTotal() throws Exception {
        Instant ahora = Instant.parse("2025-01-15T18:00:00Z");
        when(devolverVenta.devolver("VNT-DEV-001"))
                .thenReturn(new Devolucion("VNT-DEV-001", Dinero.dePesos(50000), ahora));

        mockMvc.perform(post("/api/v1/ventas/VNT-DEV-001/devolucion")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ventaId").value("VNT-DEV-001"))
                .andExpect(jsonPath("$.data.montoDevuelto").value(50000))
                .andExpect(jsonPath("$.data.estado").value("DEVUELTA"));
    }

    @Test
    @WithMockUser
    void devolucion_conItems_retorna200Parcial() throws Exception {
        Instant ahora = Instant.parse("2025-01-15T18:00:00Z");
        when(devolverVenta.devolverParcial(eq("VNT-PARC"), anyList()))
                .thenReturn(new Devolucion("VNT-PARC", Dinero.dePesos(12000), ahora, "PARCIAL"));

        String body = """
                {"items":[{"productoId":1,"cantidad":1}]}
                """;

        mockMvc.perform(post("/api/v1/ventas/VNT-PARC/devolucion")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("PARCIAL"))
                .andExpect(jsonPath("$.data.montoDevuelto").value(12000));
    }

    // ---- SPEC-BE-004: Obtener venta ----

    @Test
    @WithMockUser
    void obtener_conIdExistente_retorna200() throws Exception {
        when(obtenerVenta.obtener("VNT-20250115-001")).thenReturn(ventaMock());

        mockMvc.perform(get("/api/v1/ventas/VNT-20250115-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ventaId").value("VNT-20250115-001"));
    }

    @Test
    @WithMockUser
    void obtener_conIdInexistente_retorna404() throws Exception {
        when(obtenerVenta.obtener("FALSO"))
                .thenThrow(new VentaNotFoundException("FALSO"));

        mockMvc.perform(get("/api/v1/ventas/FALSO"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.codigo").value("VENTA_NO_ENCONTRADA"));
    }

    // ---- SPEC-BE-005: Formato de error uniforme ----

    @Test
    @WithMockUser
    void error_siempreTieneFormatoUniforme() throws Exception {
        when(obtenerVenta.obtener(any()))
                .thenThrow(new VentaNotFoundException("X"));

        mockMvc.perform(get("/api/v1/ventas/X"))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.codigo").exists())
                .andExpect(jsonPath("$.error.mensaje").exists())
                .andExpect(jsonPath("$.error.timestamp").exists());
    }
}
