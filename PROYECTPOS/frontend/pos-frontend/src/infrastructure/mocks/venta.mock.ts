import type { IVentaPort, ConfirmarVentaPayload, ConfirmarVentaResult } from '@domain/ports/IVentaPort';
import { registrarVentaEnHistorial } from './historial.mock';
import { calcularResumen } from '@domain/calculadora';

let ventaCounter = 1;

/** Almacén de ventas completas — accesible por el mock de devolución */
export const ventasCompletadasMock = new Map<string, ConfirmarVentaPayload & { ventaId: string; fechaHora: string }>();

export function obtenerVentaParaRecibo(ventaId: string) {
  return ventasCompletadasMock.get(ventaId);
}

export class VentaMock implements IVentaPort {
  async confirmar(payload: ConfirmarVentaPayload): Promise<ConfirmarVentaResult> {
    await new Promise((r) => setTimeout(r, 500));
    const fecha = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    const ventaId = `VNT-${fecha}-${String(ventaCounter++).padStart(3, '0')}`;
    const fechaHora = new Date().toISOString();

    // Guardar venta completa para el recibo y devoluciones
    ventasCompletadasMock.set(ventaId, { ...payload, ventaId, fechaHora });

    // Registrar en historial dinámico
    const resumen = calcularResumen(payload.carrito);
    registrarVentaEnHistorial({
      ventaId,
      fechaHora,
      total: resumen.total,
      cantidadItems: payload.carrito.length,
    });

    return { ok: true, ventaId };
  }
}

export const ventaMock = new VentaMock();
