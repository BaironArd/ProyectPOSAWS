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

/** Llamado por DevolucionMock para actualizar el estado y monto de una venta devuelta */
export function actualizarVentaEnHistorial(
  ventaId: string,
  cambios: { estado: string; montoDevuelto: number }
): void {
  const idx = historialDinamico.findIndex(v => v.ventaId === ventaId);
  if (idx === -1) return;
  const venta = historialDinamico[idx]!;
  const montoDevueltoPrevio = venta.montoDevuelto ?? 0;
  const montoDevueltoTotal = montoDevueltoPrevio + cambios.montoDevuelto;
  const totalNeto = Math.max(0, venta.total - montoDevueltoTotal);
  const estadoActualizado = totalNeto === 0 ? 'DEVUELTA' : 'PARCIAL';

  historialDinamico[idx] = {
    ...venta,
    estado: estadoActualizado,
    totalNeto,
    montoDevuelto: montoDevueltoTotal,
  };
}

export class HistorialMock implements IVentaHistorialPort {
  async listar(fechaDesde?: string, fechaHasta?: string): Promise<ResumenVentaHistorial[]> {
    await new Promise((r) => setTimeout(r, 300));
    let resultado = [...historialDinamico];

    if (fechaDesde || fechaHasta) {
      const desde = fechaDesde ? new Date(fechaDesde).getTime() : 0;
      const hasta = fechaHasta ? new Date(fechaHasta).getTime() + 86400000 : Infinity;

      resultado = resultado.filter((v) => {
        const fecha = new Date(v.fechaHora).getTime();
        return fecha >= desde && fecha < hasta;
      });
    }

    return resultado;
  }
}

export const historialMock = new HistorialMock();
