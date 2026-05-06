import type { IInventarioPort, NuevoProducto } from '@domain/ports/IInventarioPort';
import type { Producto } from '@domain/types/POSState';

const API_BASE = import.meta.env.VITE_API_BASE_URL as string;

export class InventarioAdapter implements IInventarioPort {
  private token: string | null = null;
  setToken(t: string) { this.token = t; }

  private headers(extra: Record<string, string> = {}) {
    return { 'Content-Type': 'application/json', ...(this.token ? { Authorization: `Bearer ${this.token}` } : {}), ...extra };
  }

  async listar(): Promise<Producto[]> {
    const res = await fetch(`${API_BASE}/productos/admin`, { headers: this.headers() });
    if (!res.ok) throw new Error('INVENTARIO_ERROR');
    return res.json() as Promise<Producto[]>;
  }

  async crear(producto: NuevoProducto): Promise<Producto> {
    const res = await fetch(`${API_BASE}/productos`, { method: 'POST', headers: this.headers(), body: JSON.stringify(producto) });
    if (!res.ok) throw new Error('PRODUCTO_DUPLICADO');
    return res.json() as Promise<Producto>;
  }

  async actualizar(id: number, cambios: Partial<Producto>): Promise<Producto> {
    const res = await fetch(`${API_BASE}/productos/${id}`, { method: 'PATCH', headers: this.headers(), body: JSON.stringify(cambios) });
    if (!res.ok) throw new Error('PRODUCTO_NO_ENCONTRADO');
    return res.json() as Promise<Producto>;
  }

  async toggleActivo(id: number): Promise<Producto> {
    const res = await fetch(`${API_BASE}/productos/${id}/toggle`, { method: 'PATCH', headers: this.headers() });
    if (!res.ok) throw new Error('PRODUCTO_NO_ENCONTRADO');
    return res.json() as Promise<Producto>;
  }
}

export const inventarioAdapter = new InventarioAdapter();
