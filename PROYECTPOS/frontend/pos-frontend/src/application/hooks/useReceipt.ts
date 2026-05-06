import { usePOSStore } from '@application/store/usePOSStore';
import type { IImpresionPort } from '@domain/ports/IImpresionPort';

export function useReceipt(impresionPort: IImpresionPort) {
  const ventaIdActual = usePOSStore((s) => s.ventaIdActual);
  const setError = usePOSStore((s) => s.setError);

  async function imprimir() {
    if (!ventaIdActual) return;
    try {
      await impresionPort.imprimir(ventaIdActual);
    } catch {
      setError({ codigo: 'IMPRESION_ERROR', mensaje: 'No se pudo imprimir el recibo.' });
    }
  }

  return { imprimir };
}
