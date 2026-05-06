import type { IVentaHistorialPort } from '@domain/ports/IVentaHistorialPort';
import type { ResumenVentaHistorial } from '@domain/types/POSState';

/**
 * Historial dinámico — acumula las ventas confirmadas durante la sesión.
 * Se actualiza cada vez que VentaMock confirma una venta.
 */
const historialDinamico: ResumenVentaHistorial[] = [];

/** Llamado por VentaMock al confirmar una venta para registrarla en el historial */
export function registrarVentaEnHistorial(entrada: ResumenVentaHistorial): void {
  historialDinamico.unshift(entrada); // más reciente primero
}

export class HistorialMock implements IVentaHistorialPort {
  async listar(): Promise<ResumenVentaHistorial[]> {
    await new Promise((r) => setTimeout(r, 300));
    return [...historialDinamico];
  }
}

export const historialMock = new HistorialMock();
