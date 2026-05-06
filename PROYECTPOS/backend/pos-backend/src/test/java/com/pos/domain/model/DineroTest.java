package com.pos.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DineroTest {

    @Test
    void mas_sumaCorrectamente() {
        assertThat(Dinero.dePesos(100).mas(Dinero.dePesos(50)).toPesos()).isEqualTo(150);
    }

    @Test
    void menos_restaCorrectamente() {
        assertThat(Dinero.dePesos(100).menos(Dinero.dePesos(30)).toPesos()).isEqualTo(70);
    }

    @Test
    void por_multiplicaCorrectamente() {
        assertThat(Dinero.dePesos(30000).por(2).toPesos()).isEqualTo(60000);
    }

    @Test
    void iva_calcula19PorCiento() {
        assertThat(Dinero.dePesos(100000).iva().toPesos()).isEqualTo(19000);
    }

    @Test
    void iva_redondea() {
        // 10 * 0.19 = 1.9 → round = 2
        assertThat(Dinero.dePesos(10).iva().toPesos()).isEqualTo(2);
    }

    @Test
    void esMenorQue_retornaTrueCuandoEsMenor() {
        assertThat(Dinero.dePesos(50).esMenorQue(Dinero.dePesos(100))).isTrue();
    }

    @Test
    void esMenorQue_retornaTrueCuandoEsNegativo() {
        assertThat(Dinero.dePesos(-1).esMenorQue(Dinero.CERO)).isTrue();
    }

    @Test
    void esInmutable_masNoModificaThis() {
        Dinero original = Dinero.dePesos(100);
        original.mas(Dinero.dePesos(50));
        assertThat(original.toPesos()).isEqualTo(100);
    }

    @Test
    void cero_esElNeutroDeAdicion() {
        assertThat(Dinero.dePesos(500).mas(Dinero.CERO).toPesos()).isEqualTo(500);
    }
}
