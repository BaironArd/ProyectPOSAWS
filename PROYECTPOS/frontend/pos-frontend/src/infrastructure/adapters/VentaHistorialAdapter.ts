import type { IVentaHistorialPort } from '@domain/ports/IVentaHistorialPort';
import type { ResumenVentaHistorial } from '@domain/types/POSState';
import { httpFetch } from './httpClient';
import { toPosApiError } from './toPosApiError';

const API_BASE = import.meta.env.VITE_API_BASE_URL as string;

export class VentaHistorialAdapter implements IVentaHistorialPort {
  async listar(fechaDesde?: string, fechaHasta?: string): Promise<ResumenVentaHistorial[]> {
    const params = new URLSearchParams({ page: '0', size: '0' });
    if (fechaDesde) params.set('fechaDesde', fechaDesde);
    if (fechaHasta) params.set('fechaHasta', fechaHasta);

    const res = await httpFetch(`${API_BASE}/ventas?${params.toString()}`);
    if (!res.ok) throw await toPosApiError(res);
    const json = await res.json() as { data: { items: ResumenVentaHistorial[] } };
    return json.data.items ?? [];
  }
}

export const ventaHistorialAdapter = new VentaHistorialAdapter();
