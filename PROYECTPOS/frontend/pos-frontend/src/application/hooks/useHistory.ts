import { useEffect } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import type { IVentaHistorialPort } from '@domain/ports/IVentaHistorialPort';
import { PosApiError } from '@domain/errors/PosApiError';
import { mensajeErrorApi } from '@domain/errors/errorMessages';

export function useHistory(historialPort: IVentaHistorialPort) {
  const estado = usePOSStore((s) => s.estado);
  const setHistorial = usePOSStore((s) => s.setHistorial);
  const guardarRecibo = usePOSStore((s) => s.guardarRecibo);
  const setError = usePOSStore((s) => s.setError);

  async function cargarHistorial(fechaDesde?: string, fechaHasta?: string) {
    try {
      const historial = await historialPort.listar(fechaDesde, fechaHasta);
      setHistorial(historial);

      historial.forEach((venta) => {
        if (!venta.items || venta.items.length === 0) return;

        guardarRecibo({
          ventaId: venta.ventaId,
          fechaHora: venta.fechaHora,
          cajero: venta.cajero ?? 'Desconocido',
          items: venta.items.map((item) => ({
            nombre: item.nombre,
            cantidad: item.cantidad,
            subtotal: item.subtotal,
          })),
          subtotal: venta.subtotal ?? 0,
          iva: venta.iva ?? 0,
          total: venta.total,
          metodoPago: venta.metodoPago ?? 'EFECTIVO',
          montoPagado: venta.montoPagado ?? venta.total,
          cambio: venta.cambio ?? 0,
        });
      });
    } catch (err) {
      if (err instanceof PosApiError) {
        setError({
          codigo: err.codigo,
          mensaje: mensajeErrorApi(err.codigo, err.message),
        });
      } else {
        setError({
          codigo: 'HISTORIAL_NO_DISPONIBLE',
          mensaje: mensajeErrorApi('HISTORIAL_NO_DISPONIBLE', 'Sin detalle'),
        });
      }
    }
  }

  useEffect(() => {
    if (estado !== 'HISTORIAL') return;

    cargarHistorial();
  }, [estado, historialPort, setHistorial, setError]);

  return { cargarHistorial };
}
