import type { IReportePort } from '@domain/ports/IReportePort';
import type { ReporteCierre } from '@domain/types/POSState';

export class ReporteMock implements IReportePort {
  async generarCierre(fechaDesde: string, fechaHasta: string): Promise<ReporteCierre> {
    await new Promise((r) => setTimeout(r, 500));
    return {
      fechaDesde,
      fechaHasta,
      totalVentas: 5,
      totalDevueltas: 1,
      montoTotal: 684250,
      montoDevuelto: 136850,
      montoNeto: 547400,
      ventasPorCajero: [
        { usuario: 'cajero01', ventas: 3, monto: 410000 },
        { usuario: 'cajero02', ventas: 2, monto: 274250 },
      ],
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
