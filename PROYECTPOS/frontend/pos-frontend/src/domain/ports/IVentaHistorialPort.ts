import type { ResumenVentaHistorial } from '../types/POSState';

export interface IVentaHistorialPort {
  listar(fechaDesde?: string, fechaHasta?: string): Promise<ResumenVentaHistorial[]>;
}
