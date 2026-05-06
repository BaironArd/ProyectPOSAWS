import type { IDevolucionPort } from '@domain/ports/IDevolucionPort';
import type { Devolucion } from '@domain/types/POSState';

const VENTAS_COMPLETADAS: Record<string, number> = {
  'VNT-20250115-001': 136850,
  'VNT-20250115-002': 85000,
  'VNT-20250115-003': 52000,
};

const DEVUELTAS = new Set<string>();

export class DevolucionMock implements IDevolucionPort {
  async procesar(ventaId: string): Promise<Devolucion> {
    await new Promise((r) => setTimeout(r, 400));

    if (DEVUELTAS.has(ventaId)) {
      throw new Error('VENTA_YA_DEVUELTA');
    }

    const monto = VENTAS_COMPLETADAS[ventaId];
    if (monto === undefined) {
      throw new Error('VENTA_NO_ENCONTRADA');
    }

    DEVUELTAS.add(ventaId);
    return { ventaId, montoDevuelto: monto, estado: 'DEVUELTA' };
  }
}

export const devolucionMock = new DevolucionMock();
