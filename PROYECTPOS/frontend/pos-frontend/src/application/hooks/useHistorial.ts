import { useState, useCallback } from 'react';
import type { IHistorialPort, VentaHistorial } from '@domain/ports/IHistorialPort';

/**
 * useHistorial - Hook para consultar historial de ventas
 * 
 * Permite obtener ventas del turno, buscar por ID, filtrar por fechas.
 * Actualmente es un placeholder para expansión futura.
 */
export function useHistorial(historialPort: IHistorialPort) {
  const [ventas, setVentas] = useState<VentaHistorial[]>([]);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const obtenerVentasTurno = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const ventasTurno = await historialPort.getVentasTurno();
      setVentas(ventasTurno);
      return ventasTurno;
    } catch (err) {
      const mensaje = err instanceof Error ? err.message : 'Error al obtener ventas del turno';
      setError(mensaje);
      return [];
    } finally {
      setCargando(false);
    }
  }, [historialPort]);

  const buscarVentaPorId = useCallback(
    async (ventaId: string): Promise<VentaHistorial | null> => {
      setCargando(true);
      setError(null);
      try {
        const venta = await historialPort.getVentaPorId(ventaId);
        return venta;
      } catch (err) {
        const mensaje = err instanceof Error ? err.message : 'Error al buscar venta';
        setError(mensaje);
        return null;
      } finally {
        setCargando(false);
      }
    },
    [historialPort]
  );

  const obtenerVentasPorFecha = useCallback(
    async (fechaInicio: string, fechaFin: string) => {
      setCargando(true);
      setError(null);
      try {
        const ventasFiltradas = await historialPort.getVentasPorFecha(fechaInicio, fechaFin);
        setVentas(ventasFiltradas);
        return ventasFiltradas;
      } catch (err) {
        const mensaje =
          err instanceof Error ? err.message : 'Error al obtener ventas por fecha';
        setError(mensaje);
        return [];
      } finally {
        setCargando(false);
      }
    },
    [historialPort]
  );

  return {
    ventas,
    cargando,
    error,
    obtenerVentasTurno,
    buscarVentaPorId,
    obtenerVentasPorFecha,
  };
}
