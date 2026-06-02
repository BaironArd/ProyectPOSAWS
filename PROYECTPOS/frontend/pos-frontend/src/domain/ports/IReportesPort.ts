/**
 * IReportesPort - Port para generación de reportes de ventas
 * 
 * Permite obtener reportes diarios, resúmenes financieros, top productos, etc.
 * Actualmente no implementado, pero documentado para expansión futura.
 */

export interface ReporteResumen {
  totalVentas: number;
  subtotal: number;
  iva: number;
  total: number;
  ventasEfectivo: number;
  ventasTarjeta: number;
  cantidadVentas: number;
}

export interface ReporteDiario {
  fecha: string;
  ventas: Array<{
    ventaId: string;
    hora: string;
    total: number;
    metodoPago: string;
  }>;
  resumen: ReporteResumen;
}

export interface TopProducto {
  productoId: string;
  nombre: string;
  cantidadVendida: number;
  totalIngresos: number;
}

export interface IReportesPort {
  /**
   * Obtener reporte de ventas del día especificado
   */
  getReporteDiario(fecha: string): Promise<ReporteDiario>;

  /**
   * Obtener resumen financiero (totales, IVA, por método de pago)
   */
  getResumenFinanciero(): Promise<ReporteResumen>;

  /**
   * Obtener top 10 productos más vendidos
   */
  getTopProductos(limite?: number): Promise<TopProducto[]>;
}
