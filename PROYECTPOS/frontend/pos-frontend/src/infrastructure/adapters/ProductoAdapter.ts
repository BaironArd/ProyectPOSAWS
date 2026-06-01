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
    stockLevel: number;        // Jackson serializa el getter getStockLevel() → stockLevel
    lowStockThreshold: number; // Jackson serializa getLowStockThreshold() → lowStockThreshold
  };
}

export class ProductoAdapter implements IProductoPort {
  async buscar(query: string): Promise<Producto[]> {
    // Si la query está vacía, traer todos
    // Si contiene '-' es un código de producto (ej. LAP-001), usar type=code
    const trimmed = query.trim();
    let url: string;
    if (!trimmed) {
      url = `${API_BASE_URL}/products?type=all`;
    } else if (trimmed.includes('-')) {
      url = `${API_BASE_URL}/products?type=code&q=${encodeURIComponent(trimmed)}`;
    } else {
      url = `${API_BASE_URL}/products?type=name&q=${encodeURIComponent(trimmed)}`;
    }

    const res = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
    });

    if (!res.ok) throw await toPosApiError(res);

    const data = await res.json() as { success: boolean; data: LambdaProducto[] };

    // Mapear al formato que espera el store
    return data.data.map((p) => ({
      id: p.id,  // Usar UUID directamente sin conversión
      nombre: p.producto.name,
      precio: p.producto.price,
      stock: p.producto.stockLevel,
      activo: true,
    } as Producto));
  }
}

export const productoAdapter = new ProductoAdapter();
