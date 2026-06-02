import { useState, useCallback } from 'react';
import type {
  IInventarioPort,
  ActualizarStockPayload,
  CrearProductoPayload,
} from '@domain/ports/IInventarioPort';

/**
 * useInventario - Hook para gestión de inventario (solo Admin)
 * 
 * Permite actualizar stock, crear productos, desactivar productos.
 * Actualmente es un placeholder para expansión futura.
 */
export function useInventario(inventarioPort: IInventarioPort) {
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const actualizarStock = useCallback(
    async (payload: ActualizarStockPayload) => {
      setCargando(true);
      setError(null);
      try {
        await inventarioPort.actualizarStock(payload);
        return true;
      } catch (err) {
        const mensaje = err instanceof Error ? err.message : 'Error al actualizar stock';
        setError(mensaje);
        return false;
      } finally {
        setCargando(false);
      }
    },
    [inventarioPort]
  );

  const crearProducto = useCallback(
    async (payload: CrearProductoPayload): Promise<string | null> => {
      setCargando(true);
      setError(null);
      try {
        const resultado = await inventarioPort.crearProducto(payload);
        return resultado.id;
      } catch (err) {
        const mensaje = err instanceof Error ? err.message : 'Error al crear producto';
        setError(mensaje);
        return null;
      } finally {
        setCargando(false);
      }
    },
    [inventarioPort]
  );

  const desactivarProducto = useCallback(
    async (productoId: string) => {
      setCargando(true);
      setError(null);
      try {
        await inventarioPort.desactivarProducto(productoId);
        return true;
      } catch (err) {
        const mensaje = err instanceof Error ? err.message : 'Error al desactivar producto';
        setError(mensaje);
        return false;
      } finally {
        setCargando(false);
      }
    },
    [inventarioPort]
  );

  const obtenerProductosBajoStock = useCallback(async () => {
    setCargando(true);
    setError(null);
    try {
      const productos = await inventarioPort.getProductosBajoStock();
      return productos;
    } catch (err) {
      const mensaje =
        err instanceof Error ? err.message : 'Error al obtener productos con bajo stock';
      setError(mensaje);
      return [];
    } finally {
      setCargando(false);
    }
  }, [inventarioPort]);

  return {
    cargando,
    error,
    actualizarStock,
    crearProducto,
    desactivarProducto,
    obtenerProductosBajoStock,
  };
}
