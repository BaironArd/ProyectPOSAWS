import type {
  IReportesPort,
  ReporteDiario,
  ReporteResumen,
  TopProducto,
} from '@domain/ports/IReportesPort';

/**
 * ReportesAdapter - Implementación mock del port de reportes
 * 
 * NOTA: Esta es una implementación placeholder.
 * En producción, esto debería conectarse a endpoints como:
 * - GET /api/v1/reports/daily?date=YYYY-MM-DD
 * - GET /api/v1/reports/summary
 * - GET /api/v1/reports/top-products
 */
export class ReportesAdapter implements IReportesPort {
  async getReporteDiario(_fecha: string): Promise<ReporteDiario> {
    // Mock: Retorna reporte vacío
    // TODO: Implementar llamada a GET /api/v1/reports/daily
    console.info('[ReportesAdapter] getReporteDiario - Mock implementation');
    return {
      fecha: _fecha,
      ventas: [],
      resumen: {
        totalVentas: 0,
        subtotal: 0,
        iva: 0,
        total: 0,
        ventasEfectivo: 0,
        ventasTarjeta: 0,
        cantidadVentas: 0,
      },
    };
  }

  async getResumenFinanciero(): Promise<ReporteResumen> {
    // Mock: Retorna resumen vacío
    // TODO: Implementar llamada a GET /api/v1/reports/summary
    console.info('[ReportesAdapter] getResumenFinanciero - Mock implementation');
    return {
      totalVentas: 0,
      subtotal: 0,
      iva: 0,
      total: 0,
      ventasEfectivo: 0,
      ventasTarjeta: 0,
      cantidadVentas: 0,
    };
  }

  async getTopProductos(_limite = 10): Promise<TopProducto[]> {
    // Mock: Retorna array vacío
    // TODO: Implementar llamada a GET /api/v1/reports/top-products
    console.info('[ReportesAdapter] getTopProductos - Mock implementation');
    return [];
  }
}
