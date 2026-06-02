/**
 * IDevolucionPort - Port para procesar devoluciones de ventas
 * 
 * Permite anular/devolver ventas completadas.
 * Actualmente no implementado, pero documentado para expansión futura.
 */

export interface ProcesarDevolucionPayload {
  ventaId: string;
  motivo: string;
  itemsADevolver?: Array<{
    productoId: string;
    cantidad: number;
  }>;
  devolucionTotal: boolean;
}

export interface ResultadoDevolucion {
  devolucionId: string;
  montoDevuelto: number;
  estado: 'PROCESADA' | 'PENDIENTE_APROBACION';
  mensaje: string;
}

export interface IDevolucionPort {
  /**
   * Procesar devolución total o parcial de una venta
   */
  procesarDevolucion(payload: ProcesarDevolucionPayload): Promise<ResultadoDevolucion>;

  /**
   * Consultar si una venta puede ser devuelta
   * (verificar tiempo transcurrido, estado, etc.)
   */
  puedeDevolver(ventaId: string): Promise<{ permitido: boolean; razon?: string }>;
}
