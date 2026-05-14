import type { IReportePort } from '@domain/ports/IReportePort';
import type { ReporteCierre } from '@domain/types/POSState';
import { ventasCompletadasMock } from './venta.mock';
import { historialMock } from './historial.mock';

export class ReporteMock implements IReportePort {
  async generarCierre(fechaDesde: string, fechaHasta: string): Promise<ReporteCierre> {
    await new Promise((r) => setTimeout(r, 400));

    // Obtener historial filtrado por fechas
    const historial = await historialMock.listar(fechaDesde, fechaHasta);

    if (historial.length === 0) {
      return {
        fechaDesde,
        fechaHasta,
        totalVentas: 0,
        totalDevueltas: 0,
        montoTotal: 0,
        montoDevuelto: 0,
        montoNeto: 0,
        ventasPorCajero: [],
      };
    }

    const totalVentas = historial.length;
    const totalDevueltas = historial.filter(
      v => v.estado === 'DEVUELTA' || v.estado === 'PARCIAL'
    ).length;
    const montoTotal = historial.reduce((acc, v) => acc + v.total, 0);
    const montoDevuelto = historial.reduce((acc, v) => acc + (v.montoDevuelto ?? 0), 0);
    const montoNeto = montoTotal - montoDevuelto;

    // Agrupar por cajero usando los datos de las ventas completadas
    const porCajero = new Map<string, { ventas: number; monto: number }>();
    for (const v of historial) {
      const venta = ventasCompletadasMock.get(v.ventaId);
      const cajero = venta?.usuarioCajero ?? 'desconocido';
      const actual = porCajero.get(cajero) ?? { ventas: 0, monto: 0 };
      porCajero.set(cajero, {
        ventas: actual.ventas + 1,
        monto: actual.monto + v.total - (v.montoDevuelto ?? 0),
      });
    }

    return {
      fechaDesde,
      fechaHasta,
      totalVentas,
      totalDevueltas,
      montoTotal,
      montoDevuelto,
      montoNeto,
      ventasPorCajero: Array.from(porCajero.entries()).map(([usuario, d]) => ({
        usuario,
        ventas: d.ventas,
        monto: d.monto,
      })),
    };
  }

  async exportarCSV(reporte: ReporteCierre): Promise<Blob> {
    await new Promise((r) => setTimeout(r, 200));
    const lineas = [
      'Desde,Hasta,Total Ventas,Total Devueltas,Monto Total,Monto Devuelto,Monto Neto',
      `${reporte.fechaDesde},${reporte.fechaHasta},${reporte.totalVentas},${reporte.totalDevueltas},${reporte.montoTotal},${reporte.montoDevuelto},${reporte.montoNeto}`,
      '',
      'Cajero,Ventas,Monto',
      ...reporte.ventasPorCajero.map((v) => `${v.usuario},${v.ventas},${v.monto}`),
    ];
    return new Blob([lineas.join('\n')], { type: 'text/csv;charset=utf-8;' });
  }
}

export const reporteMock = new ReporteMock();
