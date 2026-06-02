/**
 * IHistorialPort - Port para consultar historial de ventas
 * 
 * Permite obtener ventas del turno actual, buscar ventas por ID, etc.
 * Actualmente no implementado, pero documentado para expansión futura.
 */

export interface VentaHistorial {
  ventaId: string;
  fecha: string;
  hora: string;
  total: number;
  metodoPago: string;
  cajero: string;
  items: Array<{
    nombre: string;
    cantidad: number;
    subtotal: number;
  }>;
}

export interface IHistorialPort {
  /**
   * Obtener todas las ventas del turno actual
   */
  getVentasTurno(): Promise<VentaHistorial[]>;

  /**
   * Buscar venta por ID
   */
  getVentaPorId(ventaId: string): Promise<VentaHistorial>;

  /**
   * Obtener ventas por rango de fechas
   */
  getVentasPorFecha(fechaInicio: string, fechaFin: string): Promise<VentaHistorial[]>;
}
