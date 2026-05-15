import type { IDevolucionPort, ItemDevolucionRequest } from '@domain/ports/IDevolucionPort';
import type { Devolucion, ItemDevolucion } from '@domain/types/POSState';
import { IVA_RATE } from '@domain/calculadora';
import { ventasCompletadasMock } from './venta.mock';
import { inventarioMock } from './inventario.mock';
import { actualizarVentaEnHistorial } from './historial.mock';

export class DevolucionMock implements IDevolucionPort {
  async obtenerItems(ventaId: string): Promise<ItemDevolucion[]> {
    await new Promise((r) => setTimeout(r, 300));

    const venta = ventasCompletadasMock.get(ventaId);
    if (!venta) throw new Error('VENTA_NO_ENCONTRADA');

    return venta.carrito
      .filter((i) => i.cantidad > 0)
      .map(i => ({
        productoId: i.productoId,
        nombre: i.nombre,
        cantidad: i.cantidad,
        cantidadDevolver: i.cantidad,
        precioUnitario: i.precioUnitario,
        subtotal: i.subtotal,
      }));
  }

  async procesar(ventaId: string, items: ItemDevolucionRequest[]): Promise<Devolucion> {
    await new Promise((r) => setTimeout(r, 400));

    const venta = ventasCompletadasMock.get(ventaId);
    if (!venta) throw new Error('VENTA_NO_ENCONTRADA');

    // Calcular subtotal devuelto (sin IVA) y aplicar IVA
    const subtotalDevuelto = items.reduce((acc, req) => {
      const item = venta.carrito.find(i => i.productoId === req.productoId);
      return acc + (item ? item.precioUnitario * req.cantidad : 0);
    }, 0);
    const ivaDevuelto = Math.round(subtotalDevuelto * IVA_RATE);
    const montoDevuelto = subtotalDevuelto + ivaDevuelto;

    // Actualizar cantidades en el carrito
    items.forEach(req => {
      const item = venta.carrito.find(i => i.productoId === req.productoId);
      if (item) {
        item.cantidad -= req.cantidad;
        item.subtotal = item.cantidad * item.precioUnitario;
      }
    });

    // Restaurar stock en el inventario mock
    for (const req of items) {
      const productos = await inventarioMock.listar();
      const producto = productos.find(p => p.id === req.productoId);
      if (producto) {
        await inventarioMock.actualizar(req.productoId, {
          stock: producto.stock + req.cantidad,
        });
      }
    }

    const estadoFinal = venta.carrito.every((i) => i.cantidad === 0) ? 'DEVUELTA' : 'PARCIAL';

    // Actualizar el historial sin cerrar la venta
    actualizarVentaEnHistorial(ventaId, {
      estado: estadoFinal,
      montoDevuelto,
    });

    return {
      ventaId,
      montoDevuelto,
      estado: estadoFinal,
    };
  }
}

export const devolucionMock = new DevolucionMock();
