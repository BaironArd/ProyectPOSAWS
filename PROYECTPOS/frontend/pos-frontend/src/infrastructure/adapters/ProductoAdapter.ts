import type { IProductoPort } from '@domain/ports/IProductoPort';
import type { Producto } from '@domain/types/POSState';

const API_BASE = import.meta.env.VITE_API_BASE_URL as string;

export class ProductoAdapter implements IProductoPort {
  private token: string | null = null;

  setToken(token: string) {
    this.token = token;
  }

  async buscar(query: string): Promise<Producto[]> {
    const headers: Record<string, string> = {};
    if (this.token) headers['Authorization'] = `Bearer ${this.token}`;

    const res = await fetch(
      `${API_BASE}/productos?q=${encodeURIComponent(query)}`,
      { headers }
    );
    if (!res.ok) throw new Error('LOAD_FAILED');
    return res.json() as Promise<Producto[]>;
  }
}

export const productoAdapter = new ProductoAdapter();
