import type { IInventarioPort, NuevoProducto } from '@domain/ports/IInventarioPort';
import type { Producto } from '@domain/types/POSState';
import { httpFetch } from './httpClient';
import { toPosApiError } from './toPosApiError';

const API_BASE = import.meta.env.VITE_API_BASE_URL as string;

export class InventarioAdapter implements IInventarioPort {
  async listar(): Promise<Producto[]> {
    const res = await httpFetch(`${API_BASE}/admin/productos`);
    if (!res.ok) throw await toPosApiError(res);
    const json = await res.json() as { data: Producto[] };
    return json.data ?? [];
  }

  async crear(producto: NuevoProducto): Promise<Producto> {
    const res = await httpFetch(`${API_BASE}/admin/productos`, {
      method: 'POST',
      body: JSON.stringify(producto),
    });
    if (!res.ok) throw await toPosApiError(res);
    const json = await res.json() as { data: Producto };
    return json.data;
  }

  async actualizar(id: number, cambios: Partial<Producto>): Promise<Producto> {
    const res = await httpFetch(`${API_BASE}/admin/productos/${id}`, {
      method: 'PUT',
      body: JSON.stringify(cambios),
    });
    if (!res.ok) throw await toPosApiError(res);
    const json = await res.json() as { data: Producto };
    return json.data;
  }

  async toggleActivo(id: number): Promise<Producto> {
    const res = await httpFetch(`${API_BASE}/admin/productos/${id}/toggle`, {
      method: 'PATCH',
    });
    if (!res.ok) throw await toPosApiError(res);
    const json = await res.json() as { data: Producto };
    return json.data;
  }
}

export const inventarioAdapter = new InventarioAdapter();
