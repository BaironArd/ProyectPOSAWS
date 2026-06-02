import { useState, useCallback } from 'react';
import type {
  IReportesPort,
  ReporteDiario,
  ReporteResumen,
  TopProducto,
} from '@domain/ports/IReportesPort';

/**
 * useReportes - Hook para obtener reportes de ventas
 * 
 * Permite consultar reportes diarios, resúmenes financieros y top productos.
 * Actualmente es un placeholder para expansión futura.
 */
export function useReportes(reportesPort: IReportesPort) {
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const obtenerReporteDiario = useCallback(
    async (fecha: string): Promise<ReporteDiario | null> => {
      setCargando(true);
      setError(null);
      try {
        const reporte = await reportesPort.getReporteDiario(fecha);
        return reporte;
      } catch (err) {
        const mensaje = err instanceof Error ? err.message : 'Error al obtener reporte diario';
        setError(mensaje);
        return null;
      } finally {
        setCargando(false);
      }
    },
    [reportesPort]
  );

  const obtenerResumen = useCallback(async (): Promise<ReporteResumen | null> => {
    setCargando(true);
    setError(null);
    try {
      const resumen = await reportesPort.getResumenFinanciero();
      return resumen;
    } catch (err) {
      const mensaje = err instanceof Error ? err.message : 'Error al obtener resumen financiero';
      setError(mensaje);
      return null;
    } finally {
      setCargando(false);
    }
  }, [reportesPort]);

  const obtenerTopProductos = useCallback(
    async (limite = 10): Promise<TopProducto[]> => {
      setCargando(true);
      setError(null);
      try {
        const productos = await reportesPort.getTopProductos(limite);
        return productos;
      } catch (err) {
        const mensaje = err instanceof Error ? err.message : 'Error al obtener top productos';
        setError(mensaje);
        return [];
      } finally {
        setCargando(false);
      }
    },
    [reportesPort]
  );

  return {
    cargando,
    error,
    obtenerReporteDiario,
    obtenerResumen,
    obtenerTopProductos,
  };
}
