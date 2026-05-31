import type { IProductoPort } from '@domain/ports/IProductoPort';
import type { Producto } from '@domain/types/POSState';
import { toPosApiError } from './toPosApiError';
import { API_BASE_URL } from '../../config';

/**
 * Adaptador que conecta al API Gateway de AWS.
 * Endpoint: GET /api/v1/products?type=name&q={query}
 *
 * La Lambda retorna:
 * { success: true, data: [{ id, code, name, price, stock_level, low_stock_threshold }] }
 *
 * El store espera Producto: { id, nombre, precio, stock }
 */

interface LambdaProducto {
  id: string;
  code: string;
  producto: {
    name: string;
    price: number;
    stock_level: number;
    low_stock_threshold: number;
  };
}

export class ProductoAdapter implements IProductoPort {
  async buscar(query: string): Promise<Producto[]> {
    // Si la query está vacía, traer todos
    const url = query.trim()
      ? `${API_BASE_URL}/products?type=name&q=${encodeURIComponent(query)}`
      : `${API_BASE_URL}/products?type=all`;

    const res = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
    });

    if (!res.ok) throw await toPosApiError(res);

    const data = await res.json() as { success: boolean; data: LambdaProducto[] };

    // Mapear al formato que espera el store
    return data.data.map((p) => ({
      id: parseInt(p.id, 10) || hashCode(p.id), // Lambda usa UUID, store espera number
      nombre: p.producto.name,
      precio: p.producto.price,
      stock: p.producto.stock_level,
      activo: true,
      // Guardamos el id original como string en un campo extra para la venta
      _uuid: p.id,
    } as Producto & { _uuid: string }));
  }
}

/** Convierte un UUID string a un número entero estable para el store. */
function hashCode(str: string): number {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) - hash) + str.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

export const productoAdapter = new ProductoAdapter();
