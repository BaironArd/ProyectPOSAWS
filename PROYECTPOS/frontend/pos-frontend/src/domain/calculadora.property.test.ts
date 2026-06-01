import { describe, it } from 'vitest';
import * as fc from 'fast-check';
import { calcularResumen, calcularCambio, calcularSubtotal, IVA_RATE } from './calculadora';
import type { ItemCarrito } from './types/POSState';

const itemArb = fc.record({
  productoId: fc.uuid(),  // Usar UUID string
  nombre: fc.string({ minLength: 1, maxLength: 20 }),
  cantidad: fc.integer({ min: 1, max: 100 }),
  precioUnitario: fc.integer({ min: 0, max: 10_000_000 }),
}).map((r): ItemCarrito => ({ ...r, subtotal: r.precioUnitario * r.cantidad, stockDisponible: 100 }));

describe('Propiedades — calcularResumen', () => {
  it('P1: iva = Math.round(subtotal × IVA_RATE) para cualquier carrito', () => {
    fc.assert(
      fc.property(fc.array(itemArb, { minLength: 0, maxLength: 20 }), (carrito) => {
        const r = calcularResumen(carrito);
        return r.iva === Math.round(r.subtotal * IVA_RATE);
      }),
      { numRuns: 100 }
    );
  });

  it('P2: total = subtotal + iva para cualquier carrito', () => {
    fc.assert(
      fc.property(fc.array(itemArb, { minLength: 0, maxLength: 20 }), (carrito) => {
        const r = calcularResumen(carrito);
        return r.total === r.subtotal + r.iva;
      }),
      { numRuns: 100 }
    );
  });

  it('P3: carrito vacío siempre produce { subtotal:0, iva:0, total:0 }', () => {
    const r = calcularResumen([]);
    return r.subtotal === 0 && r.iva === 0 && r.total === 0;
  });

  it('P4: subtotal = suma de subtotales de ítems', () => {
    fc.assert(
      fc.property(fc.array(itemArb, { minLength: 1, maxLength: 20 }), (carrito) => {
        const r = calcularResumen(carrito);
        const esperado = carrito.reduce((acc, i) => acc + i.subtotal, 0);
        return r.subtotal === esperado;
      }),
      { numRuns: 100 }
    );
  });
});

describe('Propiedades — calcularCambio', () => {
  it('P5: cambio = montoPagado - total para cualquier par de valores', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 0, max: 10_000_000 }),
        fc.integer({ min: 0, max: 10_000_000 }),
        (montoPagado, total) => {
          return calcularCambio(montoPagado, total) === montoPagado - total;
        }
      ),
      { numRuns: 100 }
    );
  });
});

describe('Propiedades — calcularSubtotal', () => {
  it('P6: calcularSubtotal(precio, cantidad) = precio × cantidad', () => {
    fc.assert(
      fc.property(
        fc.integer({ min: 0, max: 10_000_000 }),
        fc.integer({ min: 0, max: 1000 }),
        (precio, cantidad) => {
          return calcularSubtotal(precio, cantidad) === precio * cantidad;
        }
      ),
      { numRuns: 100 }
    );
  });
});
