import { describe, it, expect } from 'vitest';
import { calcularResumen, calcularCambio, calcularSubtotal, IVA_RATE } from './calculadora';
import type { ItemCarrito } from './types/POSState';

function item(productoId: string, precio: number, cantidad: number): ItemCarrito {
  return { productoId, nombre: `P${productoId}`, cantidad, precioUnitario: precio, subtotal: precio * cantidad, stockDisponible: 100 };
}

describe('calcularSubtotal', () => {
  it('calcula precio × cantidad', () => {
    expect(calcularSubtotal(30000, 2)).toBe(60000);
  });

  it('retorna 0 cuando cantidad es 0', () => {
    expect(calcularSubtotal(30000, 0)).toBe(0);
  });

  it('retorna 0 cuando precio es 0', () => {
    expect(calcularSubtotal(0, 5)).toBe(0);
  });
});

describe('calcularResumen', () => {
  it('retorna ceros para carrito vacío', () => {
    expect(calcularResumen([])).toEqual({ subtotal: 0, iva: 0, total: 0 });
  });

  it('calcula correctamente con un ítem', () => {
    const r = calcularResumen([item('p-1', 100000, 1)]);
    expect(r.subtotal).toBe(100000);
    expect(r.iva).toBe(Math.round(100000 * IVA_RATE));
    expect(r.total).toBe(r.subtotal + r.iva);
  });

  it('calcula correctamente con múltiples ítems', () => {
    const carrito = [item('p-1', 30000, 2), item('p-2', 55000, 1)];
    const r = calcularResumen(carrito);
    expect(r.subtotal).toBe(115000);
    expect(r.iva).toBe(Math.round(115000 * IVA_RATE));
    expect(r.total).toBe(r.subtotal + r.iva);
  });

  it('redondea el IVA al peso', () => {
    // subtotal = 1 → iva = Math.round(0.19) = 0
    expect(calcularResumen([item('p-1', 1, 1)]).iva).toBe(0);
    // subtotal = 10 → iva = Math.round(1.9) = 2
    expect(calcularResumen([item('p-1', 10, 1)]).iva).toBe(2);
  });
});

describe('calcularCambio', () => {
  it('calcula cambio exacto', () => {
    expect(calcularCambio(119000, 119000)).toBe(0);
  });

  it('calcula cambio positivo', () => {
    expect(calcularCambio(120000, 119000)).toBe(1000);
  });

  it('retorna negativo cuando monto es insuficiente', () => {
    expect(calcularCambio(100000, 119000)).toBe(-19000);
  });
});
