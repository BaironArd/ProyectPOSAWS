import { useState, useEffect } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import type { IInventarioPort, NuevoProducto } from '@domain/ports/IInventarioPort';
import type { Producto } from '@domain/types/POSState';
import { PosApiError } from '@domain/errors/PosApiError';
import { mensajeErrorApi } from '@domain/errors/errorMessages';

export function useInventory(inventarioPort: IInventarioPort) {
  const [productos, setProductos] = useState<Producto[]>([]);
  const [cargando, setCargando] = useState(false);
  const estado = usePOSStore((s) => s.estado);
  const sesion = usePOSStore((s) => s.sesion);
  const setError = usePOSStore((s) => s.setError);

  useEffect(() => {
    if (estado !== 'INVENTARIO') return;
    if (sesion?.rol !== 'ADMIN') {
      setError({ codigo: 'ACCESO_DENEGADO', mensaje: 'No tienes permisos para acceder al inventario.' });
      return;
    }
    setCargando(true);
    inventarioPort.listar()
      .then(setProductos)
      .catch((err) => {
        if (err instanceof PosApiError) {
          setError({
            codigo: err.codigo,
            mensaje: mensajeErrorApi(err.codigo, err.message),
          });
        } else {
          setError({
            codigo: 'INVENTARIO_ERROR',
            mensaje: mensajeErrorApi('INVENTARIO_ERROR', 'Sin detalle'),
          });
        }
      })
      .finally(() => setCargando(false));
  }, [estado, sesion, inventarioPort, setError]);

  async function crear(nuevo: NuevoProducto) {
    const p = await inventarioPort.crear(nuevo);
    setProductos((prev) => [...prev, p]);
    return p;
  }

  async function actualizar(id: number, cambios: Partial<Producto>) {
    const p = await inventarioPort.actualizar(id, cambios);
    setProductos((prev) => prev.map((x) => (x.id === id ? p : x)));
    return p;
  }

  async function toggleActivo(id: number) {
    const p = await inventarioPort.toggleActivo(id);
    setProductos((prev) => prev.map((x) => (x.id === id ? p : x)));
    return p;
  }

  return { productos, cargando, crear, actualizar, toggleActivo };
}
