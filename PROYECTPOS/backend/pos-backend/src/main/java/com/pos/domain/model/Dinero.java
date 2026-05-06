package com.pos.domain.model;

/**
 * Value Object inmutable que representa un valor monetario en pesos enteros.
 * Cada operación retorna una nueva instancia — nunca modifica this.
 * No tiene imports de Spring ni de JPA.
 */
public record Dinero(long centavos) {

    public static final Dinero CERO = new Dinero(0);
    public static final double IVA_RATE = 0.19;

    public Dinero {
        // centavos puede ser negativo (para representar cambio negativo / monto insuficiente)
    }

    public Dinero mas(Dinero otro) {
        return new Dinero(this.centavos + otro.centavos);
    }

    public Dinero menos(Dinero otro) {
        return new Dinero(this.centavos - otro.centavos);
    }

    public Dinero por(int factor) {
        return new Dinero(this.centavos * factor);
    }

    public Dinero iva() {
        return new Dinero(Math.round(this.centavos * IVA_RATE));
    }

    public boolean esMenorQue(Dinero otro) {
        return this.centavos < otro.centavos;
    }

    public boolean esMayorOIgualQue(Dinero otro) {
        return this.centavos >= otro.centavos;
    }

    /** Serializa a pesos enteros para la API. */
    public long toPesos() {
        return this.centavos;
    }

    public static Dinero dePesos(long pesos) {
        return new Dinero(pesos);
    }

    @Override
    public String toString() {
        return "$" + centavos;
    }
}
