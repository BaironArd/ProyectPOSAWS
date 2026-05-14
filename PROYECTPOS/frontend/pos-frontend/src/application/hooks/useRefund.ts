import { useState, useEffect } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import type { IDevolucionPort, ItemDevolucionRequest } from '@domain/ports/IDevolucionPort';
import type { Devolucion, ItemDevolucion } from '@domain/types/POSState';
import { IVA_RATE } from '@domain/calculadora';

export function useRefund(devolucionPort: IDevolucionPort) {
  const [procesando, setProcesando] = useState(false);
  const [cargandoItems, setCargandoItems] = useState(false);
  const [items, setItems] = useState<ItemDevolucion[]>([]);
  const [devolucion, setDevolucion] = useState<Devolucion | null>(null);

  const ventaIdActual = usePOSStore((s) => s.ventaIdActual);
  const estado = usePOSStore((s) => s.estado);
  const setError = usePOSStore((s) => s.setError);

  // Cargar ítems de la venta cuando se entra al estado DEVOLUCION
  useEffect(() => {
    if (estado !== 'DEVOLUCION' || !ventaIdActual) return;
    setDevolucion(null);
    setItems([]);
    setCargandoItems(true);
    devolucionPort.obtenerItems(ventaIdActual)
      .then((its) => setItems(its.map(i => ({ ...i, cantidadDevolver: i.cantidad }))))
      .catch(() => setError({ codigo: 'DEVOLUCION_ERROR', mensaje: 'No se pudieron cargar los ítems de la venta.' }))
      .finally(() => setCargandoItems(false));
  }, [estado, ventaIdActual]);

  function setCantidadDevolver(productoId: number, cantidad: number) {
    setItems(prev => prev.map(i =>
      i.productoId === productoId
        ? { ...i, cantidadDevolver: Math.max(0, Math.min(cantidad, i.cantidad)) }
        : i
    ));
  }

  async function procesar() {
    if (procesando || !ventaIdActual) return;
    const itemsADevolver: ItemDevolucionRequest[] = items
      .filter(i => i.cantidadDevolver > 0)
      .map(i => ({ productoId: i.productoId, cantidad: i.cantidadDevolver }));

    if (itemsADevolver.length === 0) return;

    setProcesando(true);
    try {
      const result = await devolucionPort.procesar(ventaIdActual, itemsADevolver);
      setDevolucion(result);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Error al procesar la devolución';
      setError({ codigo: 'DEVOLUCION_FALLIDA', mensaje: `No se pudo procesar la devolución: ${msg}` });
    } finally {
      setProcesando(false);
    }
  }

  // Subtotal de los ítems a devolver (sin IVA)
  const subtotalDevolucion = items.reduce(
    (acc, i) => acc + i.precioUnitario * i.cantidadDevolver,
    0
  );
  // IVA sobre el subtotal devuelto
  const ivaDevolucion = Math.round(subtotalDevolucion * IVA_RATE);
  // Total a devolver al cliente (incluye IVA)
  const montoADevolver = subtotalDevolucion + ivaDevolucion;

  const hayItemsSeleccionados = items.some(i => i.cantidadDevolver > 0);

  return {
    procesar,
    procesando,
    cargandoItems,
    items,
    devolucion,
    setCantidadDevolver,
    subtotalDevolucion,
    ivaDevolucion,
    montoADevolver,
    hayItemsSeleccionados,
  };
}
