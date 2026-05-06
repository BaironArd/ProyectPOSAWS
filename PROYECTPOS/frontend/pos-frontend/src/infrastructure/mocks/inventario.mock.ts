import type { IInventarioPort, NuevoProducto } from '@domain/ports/IInventarioPort';
import type { Producto } from '@domain/types/POSState';

let productos: Producto[] = [
  { id: 1, nombre: 'Mouse Óptico USB', precio: 30000, stock: 15, activo: true },
  { id: 2, nombre: 'Teclado Mecánico', precio: 55000, stock: 8, activo: true },
  { id: 3, nombre: 'Monitor 24"', precio: 450000, stock: 3, activo: true },
  { id: 4, nombre: 'Audífonos Bluetooth', precio: 85000, stock: 12, activo: true },
  { id: 5, nombre: 'Cable HDMI 2m', precio: 15000, stock: 25, activo: true },
  { id: 6, nombre: 'Hub USB 4 puertos', precio: 22000, stock: 0, activo: false },
];

let nextId = 7;

export class InventarioMock implements IInventarioPort {
  async listar(): Promise<Producto[]> {
    await new Promise((r) => setTimeout(r, 300));
    return [...productos];
  }

  async crear(nuevo: NuevoProducto): Promise<Producto> {
    await new Promise((r) => setTimeout(r, 300));
    const duplicado = productos.find(
      (p) => p.nombre.toLowerCase() === nuevo.nombre.toLowerCase()
    );
    if (duplicado) throw new Error('PRODUCTO_DUPLICADO');

    const producto: Producto = { id: nextId++, ...nuevo, activo: true };
    productos = [...productos, producto];
    return producto;
  }

  async actualizar(id: number, cambios: Partial<Producto>): Promise<Producto> {
    await new Promise((r) => setTimeout(r, 300));
    const idx = productos.findIndex((p) => p.id === id);
    if (idx === -1) throw new Error('PRODUCTO_NO_ENCONTRADO');
    const actualizado = { ...productos[idx]!, ...cambios };
    productos = productos.map((p) => (p.id === id ? actualizado : p));
    return actualizado;
  }

  async toggleActivo(id: number): Promise<Producto> {
    await new Promise((r) => setTimeout(r, 300));
    const idx = productos.findIndex((p) => p.id === id);
    if (idx === -1) throw new Error('PRODUCTO_NO_ENCONTRADO');
    const actualizado = { ...productos[idx]!, activo: !productos[idx]!.activo };
    productos = productos.map((p) => (p.id === id ? actualizado : p));
    return actualizado;
  }
}

export const inventarioMock = new InventarioMock();
