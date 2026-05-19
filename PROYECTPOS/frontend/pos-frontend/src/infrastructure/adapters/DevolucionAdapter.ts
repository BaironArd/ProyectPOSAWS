import type { IDevolucionPort, ItemDevolucionRequest } from '@domain/ports/IDevolucionPort';
import type { Devolucion, ItemDevolucion } from '@domain/types/POSState';
import { httpFetch } from './httpClient';
import { toPosApiError } from './toPosApiError';

const API_BASE = import.meta.env.VITE_API_BASE_URL as string;

export class DevolucionAdapter implements IDevolucionPort {
  /** Obtiene los ítems de una venta para mostrarlos en el panel de devolución */
  async obtenerItems(ventaId: string): Promise<ItemDevolucion[]> {
    const res = await httpFetch(`${API_BASE}/ventas/${ventaId}`);
    if (!res.ok) throw await toPosApiError(res);
    const json = await res.json() as {
      data: {
        items: Array<{
          productoId: number;
          nombre: string;
          cantidad: number;
          precioUnitario: number;
          subtotal: number;
        }>;
      };
    };
    return (json.data.items ?? []).map(i => ({
      productoId: i.productoId,
      nombre: i.nombre,
      cantidad: i.cantidad,
      cantidadDevolver: i.cantidad,
      precioUnitario: i.precioUnitario,
      subtotal: i.subtotal,
    }));
  }

  /** Procesa la devolución — parcial o total según los ítems enviados */
  async procesar(ventaId: string, items: ItemDevolucionRequest[]): Promise<Devolucion> {
    const res = await httpFetch(`${API_BASE}/ventas/${ventaId}/devolucion`, {
      method: 'POST',
      body: JSON.stringify({ items }),
    });
    if (!res.ok) {
      throw await toPosApiError(res);
    }
    const json = await res.json() as {
      data: { ventaId: string; montoDevuelto: number; estado: string };
    };
    return {
      ventaId: json.data.ventaId,
      montoDevuelto: json.data.montoDevuelto,
      estado: json.data.estado as 'DEVUELTA' | 'PARCIAL' | 'PENDIENTE',
    };
  }
}

export const devolucionAdapter = new DevolucionAdapter();
