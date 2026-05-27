package com.pos.aws.ventas.service;

import com.pos.aws.ventas.model.ItemVentaRequest;
import com.pos.aws.ventas.model.RegistrarVentaRequest;
import com.pos.aws.ventas.model.VentaResponse;
import com.pos.aws.ventas.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository repository;

    private VentaService service;

    @BeforeEach
    void setUp() {
        service = new VentaService(repository);
    }

    @Test
    void registrar_ventaValida_calculaTotalesCorrectamente() {
        // 2 items: 45000 x 2 = 90000, 80000 x 1 = 80000 → subtotal = 170000
        // IVA 19% = 32300 → total = 202300 → cambio = 297700
        RegistrarVentaRequest request = new RegistrarVentaRequest(
                "cajero1", "EFECTIVO", 500000L,
                List.of(
                        new ItemVentaRequest("prod-001", "Mouse", 2, 45000L),
                        new ItemVentaRequest("prod-002", "Teclado", 1, 80000L)
                )
        );

        doNothing().when(repository).guardar(any());

        VentaResponse response = service.registrar(request);

        assertNotNull(response.getVentaId());
        assertEquals(170000L, response.getSubtotal());
        assertEquals(32300L, response.getIva());
        assertEquals(202300L, response.getTotal());
        assertEquals(297700L, response.getCambio());
        assertEquals("COMPLETADA", response.getEstado());
        assertEquals(2, response.getDetalle().size());
        verify(repository).guardar(any());
    }

    @Test
    void registrar_montoPagadoInsuficiente_lanzaExcepcion() {
        RegistrarVentaRequest request = new RegistrarVentaRequest(
                "cajero1", "EFECTIVO", 1000L,  // muy poco
                List.of(new ItemVentaRequest("prod-001", "Mouse", 1, 45000L))
        );

        assertThrows(IllegalArgumentException.class, () -> service.registrar(request));
        verify(repository, never()).guardar(any());
    }

    @Test
    void registrar_itemsSinProductos_lanzaExcepcion() {
        RegistrarVentaRequest request = new RegistrarVentaRequest(
                "cajero1", "EFECTIVO", 100000L, List.of()
        );

        assertThrows(IllegalArgumentException.class, () -> service.registrar(request));
    }

    @Test
    void registrar_cantidadCero_lanzaExcepcion() {
        RegistrarVentaRequest request = new RegistrarVentaRequest(
                "cajero1", "EFECTIVO", 100000L,
                List.of(new ItemVentaRequest("prod-001", "Mouse", 0, 45000L))
        );

        assertThrows(IllegalArgumentException.class, () -> service.registrar(request));
    }

    @Test
    void registrar_precioUnitarioCero_lanzaExcepcion() {
        RegistrarVentaRequest request = new RegistrarVentaRequest(
                "cajero1", "EFECTIVO", 100000L,
                List.of(new ItemVentaRequest("prod-001", "Mouse", 1, 0L))
        );

        assertThrows(IllegalArgumentException.class, () -> service.registrar(request));
    }

    @Test
    void registrar_requestNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.registrar(null));
    }

    @Test
    void registrar_productoIdVacio_lanzaExcepcion() {
        RegistrarVentaRequest request = new RegistrarVentaRequest(
                "cajero1", "EFECTIVO", 100000L,
                List.of(new ItemVentaRequest("", "Mouse", 1, 45000L))
        );

        assertThrows(IllegalArgumentException.class, () -> service.registrar(request));
    }

    @Test
    void registrar_cambioExacto_retornaCambiosCero() {
        // subtotal = 100000, IVA = 19000, total = 119000, pagado = 119000 → cambio = 0
        RegistrarVentaRequest request = new RegistrarVentaRequest(
                "cajero1", "EFECTIVO", 119000L,
                List.of(new ItemVentaRequest("prod-001", "Producto", 1, 100000L))
        );

        doNothing().when(repository).guardar(any());

        VentaResponse response = service.registrar(request);

        assertEquals(0L, response.getCambio());
    }
}
