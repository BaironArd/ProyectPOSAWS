import type { Devolucion, ItemDevolucion } from '../types/POSState';

export interface ItemDevolucionRequest {
  productoId: number;
  cantidad: number;
}

export interface IDevolucionPort {
  /** Obtiene los ítems de una venta para mostrarlos en el panel de devolución */
  obtenerItems(ventaId: string): Promise<ItemDevolucion[]>;
  /** Procesa la devolución — puede ser total (todos los ítems) o parcial */
  procesar(ventaId: string, items: ItemDevolucionRequest[]): Promise<Devolucion>;
}
