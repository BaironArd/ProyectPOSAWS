import { useEffect, useRef } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import type { IProductoPort } from '@domain/ports/IProductoPort';
import { PosApiError } from '@domain/errors/PosApiError';
import { mensajeErrorApi } from '@domain/errors/errorMessages';

const DEBOUNCE_MS = 300;
const MIN_CHARS = 2;

export function useSearch(productoPort: IProductoPort) {
  const query = usePOSStore((s) => s.query);
  const setEstado = usePOSStore((s) => s.setEstado);
  const setProductos = usePOSStore((s) => s.setProductos);
  const setError = usePOSStore((s) => s.setError);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (timerRef.current) clearTimeout(timerRef.current);

    if (query.length < MIN_CHARS) {
      setProductos([]);
      setEstado('IDLE');
      return;
    }

    setEstado('BUSCANDO');

    timerRef.current = setTimeout(async () => {
      try {
        const productos = await productoPort.buscar(query);
        setProductos(productos);
        setEstado('RESULTADOS');
      } catch (err) {
        if (err instanceof PosApiError) {
          setError({
            codigo: err.codigo,
            mensaje: mensajeErrorApi(err.codigo, err.message),
          });
        } else {
          setError({
            codigo: 'LOAD_FAILED',
            mensaje: mensajeErrorApi('LOAD_FAILED', 'Sin detalle'),
          });
        }
      }
    }, DEBOUNCE_MS);

    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, [query, productoPort, setEstado, setProductos, setError]);
}
