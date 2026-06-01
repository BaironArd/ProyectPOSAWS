import { useEffect, useRef } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import type { IProductoPort } from '@domain/ports/IProductoPort';
import { PosApiError } from '@domain/errors/PosApiError';
import { mensajeErrorApi } from '@domain/errors/errorMessages';

const DEBOUNCE_MS = 300;
const MIN_CHARS   = 2;

export function useSearch(productoPort: IProductoPort) {
  const query = usePOSStore((s) => s.query);
  const resetCount = usePOSStore((s) => s.resetCount);
  const setEstado = usePOSStore((s) => s.setEstado);
  const setProductos = usePOSStore((s) => s.setProductos);
  const setError = usePOSStore((s) => s.setError);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Mantener referencias estables a las acciones del store para que no
  // provoquen re-ejecuciones del efecto cuando Zustand las recrea.
  const setEstadoRef   = useRef(setEstado);
  const setProductosRef = useRef(setProductos);
  const setErrorRef    = useRef(setError);
  setEstadoRef.current   = setEstado;
  setProductosRef.current = setProductos;
  setErrorRef.current    = setError;

  // Mantener referencia estable al port para que no dispare el efecto
  // si el componente padre pasa una nueva instancia en cada render.
  const productoPortRef = useRef(productoPort);
  productoPortRef.current = productoPort;

  useEffect(() => {
    if (timerRef.current) clearTimeout(timerRef.current);

    // Con menos de MIN_CHARS limpiamos resultados
    if (query.length > 0 && query.length < MIN_CHARS) {
      setProductosRef.current([]);
      setEstadoRef.current('IDLE');
      return;
    }

    // Query vacía o >= MIN_CHARS → buscar (vacía trae todos)
    setEstadoRef.current('BUSCANDO');

    timerRef.current = setTimeout(async () => {
      try {
        const productos = await productoPortRef.current.buscar(query);
        setProductosRef.current(productos);
        setEstadoRef.current('RESULTADOS');
      } catch (err) {
        if (err instanceof PosApiError) {
          setErrorRef.current({
            codigo: err.codigo,
            mensaje: mensajeErrorApi(err.codigo, err.message),
          });
        } else {
          setErrorRef.current({
            codigo: 'LOAD_FAILED',
            mensaje: mensajeErrorApi('LOAD_FAILED', 'Sin detalle'),
          });
        }
      }
    }, DEBOUNCE_MS);

    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  // Solo query y resetCount como dependencias reales — el resto se accede por ref
  }, [query, resetCount]);
}
