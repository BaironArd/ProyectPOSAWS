import type { IVentaHistorialPort } from '@domain/ports/IVentaHistorialPort';
import type { ResumenVentaHistorial } from '@domain/types/POSState';

const HISTORIAL_MOCK: ResumenVentaHistorial[] = [
  { ventaId: 'VNT-20250115-001', fechaHora: '2025-01-15T10:30:00Z', total: 136850, cantidadItems: 3 },
  { ventaId: 'VNT-20250115-002', fechaHora: '2025-01-15T11:15:00Z', total: 85000, cantidadItems: 1 },
  { ventaId: 'VNT-20250115-003', fechaHora: '2025-01-15T12:00:00Z', total: 52000, cantidadItems: 2 },
];

export class HistorialMock implements IVentaHistorialPort {
  async listar(): Promise<ResumenVentaHistorial[]> {
    await new Promise((r) => setTimeout(r, 300));
    return [...HISTORIAL_MOCK];
  }
}

export const historialMock = new HistorialMock();
