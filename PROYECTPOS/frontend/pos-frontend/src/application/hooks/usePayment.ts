import { useState } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import type { IVentaPort } from '@domain/ports/IVentaPort';
import { PosApiError } from '@domain/errors/PosApiError';
import { mensajeErrorApi } from '@domain/errors/errorMessages';

/** Genera un UUID v4 simple para idempotencia */
function generarIdempotencyKey(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

export function usePayment(ventaPort: IVentaPort) {
  const [procesando, setProcesando] = useState(false);

  const resumen = usePOSStore((s) => s.resumen);
  const carrito = usePOSStore((s) => s.carrito);
  const metodoPago = usePOSStore((s) => s.metodoPago);
  const pagos = usePOSStore((s) => s.pagos);
  const montoPagado = usePOSStore((s) => s.montoPagado);
  const setEstado = usePOSStore((s) => s.setEstado);
  const setError = usePOSStore((s) => s.setError);
  const setVentaIdActual = usePOSStore((s) => s.setVentaIdActual);
  const setDatosRecibo = usePOSStore((s) => s.setDatosRecibo);
  const guardarRecibo = usePOSStore((s) => s.guardarRecibo);
  const resetVenta = usePOSStore((s) => s.resetVenta);

  async function confirmarVenta() {
    if (procesando) return;
    if (!metodoPago) return;

    setProcesando(true);
    setEstado('PROCESANDO');

    try {
      const result = await ventaPort.confirmar({
        carrito,
        total: resumen.total,
        metodoPago,
        pagos,
        idempotencyKey: generarIdempotencyKey(),
      });

      if (result.ok) {
        setVentaIdActual(result.ventaId);

        // Calcular monto pagado real y cambio según método
        let montoPagadoReal: number;
        let cambioCalculado: number;

        if (metodoPago === 'EFECTIVO') {
          montoPagadoReal = montoPagado;
          cambioCalculado = montoPagado > resumen.total ? montoPagado - resumen.total : 0;
        } else if (metodoPago === 'MIXTO') {
          montoPagadoReal = pagos.reduce((s, p) => s + p.monto, 0);
          cambioCalculado = montoPagadoReal > resumen.total ? montoPagadoReal - resumen.total : 0;
        } else {
          // débito / crédito / transferencia: cobro exacto, sin cambio
          montoPagadoReal = resumen.total;
          cambioCalculado = 0;
        }

        const recibo: import('@domain/types/POSState').DatosRecibo = {
          ventaId: result.ventaId,
          fechaHora: new Date().toISOString(),
          cajero: 'cajero',
          items: carrito.map(i => ({
            nombre: i.nombre,
            cantidad: i.cantidad,
            subtotal: i.subtotal,
          })),
          subtotal: resumen.subtotal,
          iva: resumen.iva,
          total: resumen.total,
          metodoPago: metodoPago ?? 'EFECTIVO',
          montoPagado: montoPagadoReal,
          cambio: cambioCalculado,
        };

        setDatosRecibo(recibo);
        guardarRecibo(recibo);
        setEstado('VENTA_COMPLETA');
      } else {
        throw new Error('CONFIRMACION_FALLIDA');
      }
    } catch (err) {
      if (err instanceof PosApiError) {
        setError({
          codigo: err.codigo,
          mensaje: mensajeErrorApi(err.codigo, err.message),
        });
      } else {
        const mensaje = err instanceof Error ? err.message : 'Error al confirmar la venta';
        setError({
          codigo: 'CONFIRMACION_FALLIDA',
          mensaje: mensajeErrorApi('CONFIRMACION_FALLIDA', mensaje),
        });
      }
    } finally {
      setProcesando(false);
    }
  }

  function nuevaVenta() {
    resetVenta();
  }

  const puedeConfirmar =
    !procesando &&
    metodoPago !== null &&
    (metodoPago === 'EFECTIVO'
      ? montoPagado >= resumen.total
      : metodoPago === 'MIXTO'
      ? pagos.reduce((s, p) => s + p.monto, 0) >= resumen.total
      : true); // débito, crédito, transferencia: siempre habilitado (cobro exacto)

  return { confirmarVenta, nuevaVenta, procesando, puedeConfirmar };
}
