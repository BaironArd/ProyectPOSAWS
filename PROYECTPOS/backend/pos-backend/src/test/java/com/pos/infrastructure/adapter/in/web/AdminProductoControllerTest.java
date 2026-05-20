package com.pos.infrastructure.adapter.in.web;

import com.pos.domain.exception.ProductoDuplicadoException;
import com.pos.domain.exception.ProductoNotFoundException;
import com.pos.domain.model.Dinero;
import com.pos.domain.model.Producto;
import com.pos.domain.port.out.TokenRepository;
import com.pos.domain.service.InventarioService;
import com.pos.infrastructure.config.SecurityConfig;
import com.pos.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminProductoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@SuppressWarnings("null")
class AdminProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventarioService inventarioService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenRepository tokenRepository;

    private Producto productoEjemplo() {
        return new Producto(42L, "Teclado", Dinero.dePesos(89000), 8, "Periféricos", true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listar_comAdmin_retorna200() throws Exception {
        when(inventarioService.listarTodos()).thenReturn(List.of(productoEjemplo()));

        mockMvc.perform(get("/api/v1/admin/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(42))
                .andExpect(jsonPath("$.data[0].nombre").value("Teclado"))
                .andExpect(jsonPath("$.data[0].activo").value(true));
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void listar_sinRolAdmin_retorna403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/productos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_comAdmin_retorna201() throws Exception {
        Producto creado = productoEjemplo();
        when(inventarioService.crear(any())).thenReturn(creado);

        String body = """
                {"nombre":"Teclado","precio":89000,"stock":8,"categoria":"Periféricos"}
                """;

        mockMvc.perform(post("/api/v1/admin/productos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(42));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_duplicadoActivo_retorna409() throws Exception {
        when(inventarioService.crear(any()))
                .thenThrow(new ProductoDuplicadoException("Teclado"));

        String body = """
                {"nombre":"Teclado","precio":89000,"stock":8,"categoria":"Periféricos"}
                """;

        mockMvc.perform(post("/api/v1/admin/productos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.codigo").value("PRODUCTO_DUPLICADO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void crear_precioNoPositivo_retorna400() throws Exception {
        String body = """
                {"nombre":"Mal","precio":0,"stock":1,"categoria":"X"}
                """;

        mockMvc.perform(post("/api/v1/admin/productos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.codigo").value("VALIDACION_FALLIDA"));
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void crear_sinAdmin_retorna403() throws Exception {
        String body = """
                {"nombre":"Teclado","precio":89000,"stock":8,"categoria":"Periféricos"}
                """;

        mockMvc.perform(post("/api/v1/admin/productos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_comAdmin_retorna200() throws Exception {
        Producto actualizado = new Producto(42L, "Teclado RGB", Dinero.dePesos(99000), 10, "Periféricos", true);
        when(inventarioService.actualizar(eq(42L), any())).thenReturn(actualizado);

        String body = """
                {"nombre":"Teclado RGB","precio":99000,"stock":10,"categoria":"Periféricos"}
                """;

        mockMvc.perform(put("/api/v1/admin/productos/42")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value("Teclado RGB"))
                .andExpect(jsonPath("$.data.precio").value(99000));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void actualizar_productoInexistente_retorna404() throws Exception {
        when(inventarioService.actualizar(eq(999L), any()))
                .thenThrow(new ProductoNotFoundException(999L));

        String body = """
                {"nombre":"X","precio":1000,"stock":1,"categoria":"Y"}
                """;

        mockMvc.perform(put("/api/v1/admin/productos/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.codigo").value("PRODUCTO_NO_ENCONTRADO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggle_comAdmin_retorna200() throws Exception {
        Producto desactivado = new Producto(42L, "Teclado", Dinero.dePesos(89000), 8, "Periféricos", false);
        when(inventarioService.toggleActivo(eq(42L))).thenReturn(desactivado);

        mockMvc.perform(patch("/api/v1/admin/productos/42/toggle").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activo").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggle_productoInexistente_retorna404() throws Exception {
        when(inventarioService.toggleActivo(eq(777L)))
                .thenThrow(new ProductoNotFoundException(777L));

        mockMvc.perform(patch("/api/v1/admin/productos/777/toggle").with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.codigo").value("PRODUCTO_NO_ENCONTRADO"));
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void actualizar_sinAdmin_retorna403() throws Exception {
        mockMvc.perform(put("/api/v1/admin/productos/42")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"X","precio":1000,"stock":1,"categoria":"Y"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void toggle_sinAdmin_retorna403() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/productos/42/toggle").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
