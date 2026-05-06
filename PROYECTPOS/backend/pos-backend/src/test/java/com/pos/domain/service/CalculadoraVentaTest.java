package com.pos.domain.service;

import com.pos.domain.model.Dinero;
import com.pos.domain.model.ItemVenta;
import com.pos.domain.model.ResumenVenta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CalculadoraVentaTest {

    private CalculadoraVenta calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraVenta();
    }

    private ItemVenta item(long precio, int cantidad) {
        return new ItemVenta(1L, "Producto", cantidad, Dinero.dePesos(precio));
    }

    @Test
    void carritoVacio_retornaTodosCeros() {
        ResumenVenta r = calculadora.calcular(List.of(), Dinero.dePesos(0));
        assertThat(r.subtotal().toPesos()).isZero();
        assertThat(r.iva().toPesos()).isZero();
        assertThat(r.total().toPesos()).isZero();
    }

    @Test
    void unItem_calculaCorrectamente() {
        ResumenVenta r = calculadora.calcular(
                List.of(item(100000, 1)), Dinero.dePesos(119000));
        assertThat(r.subtotal().toPesos()).isEqualTo(100000);
        assertThat(r.iva().toPesos()).isEqualTo(19000);
        assertThat(r.total().toPesos()).isEqualTo(119000);
        assertThat(r.cambio().toPesos()).isZero();
    }

    @Test
    void multiplesItems_sumaSubtotales() {
        // Mouse x2 = 60000, Teclado x1 = 55000 → subtotal = 115000
        ResumenVenta r = calculadora.calcular(
                List.of(item(30000, 2), item(55000, 1)),
                Dinero.dePesos(150000));
        assertThat(r.subtotal().toPesos()).isEqualTo(115000);
        assertThat(r.iva().toPesos()).isEqualTo(Math.round(115000 * 0.19));
        assertThat(r.total().toPesos()).isEqualTo(r.subtotal().toPesos() + r.iva().toPesos());
        assertThat(r.cambio().toPesos()).isEqualTo(150000 - r.total().toPesos());
    }

    @Test
    void cambioExacto_cuandoMontoPagadoIgualTotal() {
        ResumenVenta r = calculadora.calcular(
                List.of(item(100000, 1)), Dinero.dePesos(119000));
        assertThat(r.cambio().toPesos()).isZero();
    }

    @Test
    void cambioNegativo_cuandoMontoInsuficiente() {
        ResumenVenta r = calculadora.calcular(
                List.of(item(100000, 1)), Dinero.dePesos(50000));
        assertThat(r.cambio().esMenorQue(Dinero.CERO)).isTrue();
    }

    @Test
    void cambioPositivo_cuandoHayVuelto() {
        ResumenVenta r = calculadora.calcular(
                List.of(item(100000, 1)), Dinero.dePesos(120000));
        assertThat(r.cambio().toPesos()).isEqualTo(120000 - 119000);
    }
}
