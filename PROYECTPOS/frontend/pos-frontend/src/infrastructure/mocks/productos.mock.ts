import type { IProductoPort } from '@domain/ports/IProductoPort';
import type { Producto } from '@domain/types/POSState';

const PRODUCTOS_MOCK: Producto[] = [
  { id: 1, nombre: 'Mouse Óptico USB', precio: 30000, stock: 15, activo: true },
  { id: 2, nombre: 'Teclado Mecánico', precio: 55000, stock: 8, activo: true },
  { id: 3, nombre: 'Monitor 24"', precio: 450000, stock: 3, activo: true },
  { id: 4, nombre: 'Audífonos Bluetooth', precio: 85000, stock: 12, activo: true },
  { id: 5, nombre: 'Cable HDMI 2m', precio: 15000, stock: 25, activo: true },
  { id: 6, nombre: 'Hub USB 4 puertos', precio: 22000, stock: 0, activo: true },
  { id: 7, nombre: 'Webcam HD 1080p', precio: 120000, stock: 5, activo: true },
  { id: 8, nombre: 'Mousepad XL', precio: 18000, stock: 20, activo: true },
];

export class ProductoMock implements IProductoPort {
  async buscar(query: string): Promise<Producto[]> {
    await new Promise((r) => setTimeout(r, 300));
    const q = query.toLowerCase();
    // SPEC-BE-001: solo productos activos Y con stock > 0 aparecen en búsqueda del cajero
    return PRODUCTOS_MOCK.filter((p) => {
      const coincidePorNombre = p.nombre.toLowerCase().includes(q);
      const coincidePorCodigo = String(p.id).includes(q);
      return p.activo && p.stock > 0 && (coincidePorNombre || coincidePorCodigo);
    });
  }
}

export const productoMock = new ProductoMock();
