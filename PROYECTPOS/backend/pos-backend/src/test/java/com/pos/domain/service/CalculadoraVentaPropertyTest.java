package com.pos.domain.service;

import com.pos.domain.model.Dinero;
import com.pos.domain.model.ItemVenta;
import com.pos.domain.model.ResumenVenta;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-Based Tests para CalculadoraVenta usando jqwik.
 * Verifica invariantes matemáticas para cualquier entrada válida.
 */
class CalculadoraVentaPropertyTest {

    private final CalculadoraVenta calculadora = new CalculadoraVenta();

    @Property(tries = 200)
    void iva_siempreEs19PorCientoDelSubtotalRedondeado(
            @ForAll @LongRange(min = 0, max = 10_000_000) long precio,
            @ForAll @IntRange(min = 1, max = 100) int cantidad) {

        ItemVenta item = new ItemVenta(1L, "P", cantidad, Dinero.dePesos(precio));
        ResumenVenta r = calculadora.calcular(List.of(item), Dinero.dePesos(Long.MAX_VALUE / 2));

        long ivaEsperado = Math.round(r.subtotal().toPesos() * 0.19);
        assertThat(r.iva().toPesos()).isEqualTo(ivaEsperado);
    }

    @Property(tries = 200)
    void total_siempreEsSubtotalMasIva(
            @ForAll @LongRange(min = 0, max = 10_000_000) long precio,
            @ForAll @IntRange(min = 1, max = 100) int cantidad) {

        ItemVenta item = new ItemVenta(1L, "P", cantidad, Dinero.dePesos(precio));
        ResumenVenta r = calculadora.calcular(List.of(item), Dinero.dePesos(Long.MAX_VALUE / 2));

        assertThat(r.total().toPesos()).isEqualTo(r.subtotal().toPesos() + r.iva().toPesos());
    }

    @Property(tries = 200)
    void cambio_siempreEsMontoPagadoMenosTotal(
            @ForAll @LongRange(min = 0, max = 10_000_000) long montoPagado,
            @ForAll @LongRange(min = 0, max = 10_000_000) long precio) {

        ItemVenta item = new ItemVenta(1L, "P", 1, Dinero.dePesos(precio));
        ResumenVenta r = calculadora.calcular(List.of(item), Dinero.dePesos(montoPagado));

        assertThat(r.cambio().toPesos()).isEqualTo(montoPagado - r.total().toPesos());
    }
}
