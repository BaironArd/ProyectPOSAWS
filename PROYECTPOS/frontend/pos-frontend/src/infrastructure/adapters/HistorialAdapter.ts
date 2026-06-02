import type { IHistorialPort, VentaHistorial } from '@domain/ports/IHistorialPort';

/**
 * HistorialAdapter - Implementación mock del port de historial
 * 
 * NOTA: Esta es una implementación placeholder.
 * En producción, esto debería conectarse a endpoints como:
 * - GET /api/v1/sales (todas las ventas del turno)
 * - GET /api/v1/sales/{id} (venta específica)
 * - GET /api/v1/sales?from=YYYY-MM-DD&to=YYYY-MM-DD
 */
export class HistorialAdapter implements IHistorialPort {
  async getVentasTurno(): Promise<VentaHistorial[]> {
    // Mock: Retorna array vacío
    // TODO: Implementar llamada a GET /api/v1/sales?turno=actual
    console.info('[HistorialAdapter] getVentasTurno - Mock implementation');
    return [];
  }

  async getVentaPorId(ventaId: string): Promise<VentaHistorial> {
    // Mock: Retorna venta mock
    // TODO: Implementar llamada a GET /api/v1/sales/{id}
    console.info('[HistorialAdapter] getVentaPorId - Mock implementation', ventaId);
    return {
      ventaId,
      fecha: new Date().toISOString().split('T')[0],
      hora: new Date().toLocaleTimeString(),
      total: 0,
      metodoPago: 'EFECTIVO',
      cajero: 'Usuario',
      items: [],
    };
  }

  async getVentasPorFecha(_fechaInicio: string, _fechaFin: string): Promise<VentaHistorial[]> {
    // Mock: Retorna array vacío
    // TODO: Implementar llamada a GET /api/v1/sales?from=...&to=...
    console.info('[HistorialAdapter] getVentasPorFecha - Mock implementation');
    return [];
  }
}
