import type {
  IDevolucionPort,
  ProcesarDevolucionPayload,
  ResultadoDevolucion,
} from '@domain/ports/IDevolucionPort';

/**
 * DevolucionAdapter - Implementación mock del port de devoluciones
 * 
 * NOTA: Esta es una implementación placeholder.
 * En producción, esto debería conectarse a endpoints como:
 * - POST /api/v1/returns (procesar devolución)
 * - GET /api/v1/sales/{id}/can-return (validar si se puede devolver)
 */
export class DevolucionAdapter implements IDevolucionPort {
  async procesarDevolucion(_payload: ProcesarDevolucionPayload): Promise<ResultadoDevolucion> {
    // Mock: Simula devolución exitosa
    // TODO: Implementar llamada a POST /api/v1/returns
    console.info('[DevolucionAdapter] procesarDevolucion - Mock implementation');
    return {
      devolucionId: `dev-${Date.now()}`,
      montoDevuelto: 0,
      estado: 'PROCESADA',
      mensaje: 'Devolución procesada exitosamente (MOCK)',
    };
  }

  async puedeDevolver(_ventaId: string): Promise<{ permitido: boolean; razon?: string }> {
    // Mock: Siempre permite devolución
    // TODO: Implementar validación real (tiempo límite, estado, etc.)
    console.info('[DevolucionAdapter] puedeDevolver - Mock implementation');
    return {
      permitido: true,
    };
  }
}
