import { useState, useCallback } from 'react';
import type {
  IDevolucionPort,
  ProcesarDevolucionPayload,
  ResultadoDevolucion,
} from '@domain/ports/IDevolucionPort';

/**
 * useDevolucion - Hook para procesar devoluciones
 * 
 * Permite anular/devolver ventas completadas.
 * Actualmente es un placeholder para expansión futura.
 */
export function useDevolucion(devolucionPort: IDevolucionPort) {
  const [procesando, setProcesando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const procesarDevolucion = useCallback(
    async (payload: ProcesarDevolucionPayload): Promise<ResultadoDevolucion | null> => {
      setProcesando(true);
      setError(null);
      try {
        const resultado = await devolucionPort.procesarDevolucion(payload);
        return resultado;
      } catch (err) {
        const mensaje = err instanceof Error ? err.message : 'Error al procesar devolución';
        setError(mensaje);
        return null;
      } finally {
        setProcesando(false);
      }
    },
    [devolucionPort]
  );

  const verificarPuedeDevolver = useCallback(
    async (ventaId: string): Promise<{ permitido: boolean; razon?: string }> => {
      setProcesando(true);
      setError(null);
      try {
        const resultado = await devolucionPort.puedeDevolver(ventaId);
        return resultado;
      } catch (err) {
        const mensaje = err instanceof Error ? err.message : 'Error al verificar devolución';
        setError(mensaje);
        return { permitido: false, razon: mensaje };
      } finally {
        setProcesando(false);
      }
    },
    [devolucionPort]
  );

  return {
    procesando,
    error,
    procesarDevolucion,
    verificarPuedeDevolver,
  };
}
