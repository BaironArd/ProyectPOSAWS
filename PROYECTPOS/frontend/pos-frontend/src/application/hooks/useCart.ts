import { usePOSStore } from '@application/store/usePOSStore';
import type { Producto } from '@domain/types/POSState';

/**
 * useCart - Hook para gestionar operaciones del carrito
 * 
 * Encapsula la lógica de agregar, remover, actualizar cantidades
 * y limpiar el carrito.
 */
export function useCart() {
  const carrito = usePOSStore((s) => s.carrito);
  const agregarAlCarrito = usePOSStore((s) => s.agregarAlCarrito);
  const removerDelCarrito = usePOSStore((s) => s.removerDelCarrito);
  const actualizarCantidad = usePOSStore((s) => s.actualizarCantidad);
  const limpiarCarrito = usePOSStore((s) => s.limpiarCarrito);

  /**
   * Agregar producto al carrito (o incrementar cantidad si ya existe)
   */
  const agregar = (producto: Producto) => {
    agregarAlCarrito(producto);
  };

  /**
   * Remover producto del carrito completamente
   */
  const remover = (productoId: string) => {
    removerDelCarrito(productoId);
  };

  /**
   * Actualizar cantidad de un producto en el carrito
   */
  const actualizarCantidadItem = (productoId: string, cantidad: number) => {
    if (cantidad <= 0) {
      removerDelCarrito(productoId);
    } else {
      actualizarCantidad(productoId, cantidad);
    }
  };

  /**
   * Incrementar cantidad de un producto
   */
  const incrementar = (productoId: string) => {
    const item = carrito.find((i) => i.id === productoId);
    if (item) {
      actualizarCantidad(productoId, item.cantidad + 1);
    }
  };

  /**
   * Decrementar cantidad de un producto
   */
  const decrementar = (productoId: string) => {
    const item = carrito.find((i) => i.id === productoId);
    if (item) {
      if (item.cantidad === 1) {
        removerDelCarrito(productoId);
      } else {
        actualizarCantidad(productoId, item.cantidad - 1);
      }
    }
  };

  /**
   * Vaciar todo el carrito
   */
  const limpiar = () => {
    limpiarCarrito();
  };

  /**
   * Obtener cantidad total de items en el carrito
   */
  const cantidadTotal = carrito.reduce((acc, item) => acc + item.cantidad, 0);

  /**
   * Verificar si el carrito está vacío
   */
  const estaVacio = carrito.length === 0;

  return {
    carrito,
    agregar,
    remover,
    actualizarCantidadItem,
    incrementar,
    decrementar,
    limpiar,
    cantidadTotal,
    estaVacio,
  };
}
