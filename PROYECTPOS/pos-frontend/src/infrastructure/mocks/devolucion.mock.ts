import type { ResultadoDevolucion } from '@domain/ports/IDevolucionPort';

/**
 * Mock de datos de devoluciones para testing
 */

export const mockResultadoDevolucionExitosa: ResultadoDevolucion = {
  devolucionId: 'dev-001',
  montoDevuelto: 107100,
  estado: 'PROCESADA',
  mensaje: 'Devolución procesada exitosamente',
};

export const mockResultadoDevolucionPendiente: ResultadoDevolucion = {
  devolucionId: 'dev-002',
  montoDevuelto: 85000,
  estado: 'PENDIENTE_APROBACION',
  mensaje: 'Devolución requiere aprobación del supervisor',
};

export const mockPuedeDevolver = {
  permitido: true,
};

export const mockNoPuedeDevolver = {
  permitido: false,
  razon: 'Han pasado más de 30 días desde la compra',
};
