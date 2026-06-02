import type {
  IInventarioPort,
  ActualizarStockPayload,
  CrearProductoPayload,
} from '@domain/ports/IInventarioPort';

/**
 * InventarioAdapter - Implementación mock del port de inventario
 * 
 * NOTA: Esta es una implementación placeholder.
 * En producción, esto debería conectarse a endpoints de administración
 * como POST /api/v1/inventory, PATCH /api/v1/products/{id}/stock, etc.
 */
export class InventarioAdapter implements IInventarioPort {
  async actualizarStock(_payload: ActualizarStockPayload): Promise<void> {
    // Mock: No hace nada real
    // TODO: Implementar llamada a POST /api/v1/inventory/adjust
    console.info('[InventarioAdapter] actualizarStock - Mock implementation');
  }

  async crearProducto(_payload: CrearProductoPayload): Promise<{ id: string }> {
    // Mock: Retorna ID aleatorio
    // TODO: Implementar llamada a POST /api/v1/products
    const mockId = `prod-${Date.now()}`;
    console.info('[InventarioAdapter] crearProducto - Mock implementation', mockId);
    return { id: mockId };
  }

  async desactivarProducto(_productoId: string): Promise<void> {
    // Mock: No hace nada real
    // TODO: Implementar llamada a PATCH /api/v1/products/{id} (set activo=false)
    console.info('[InventarioAdapter] desactivarProducto - Mock implementation');
  }

  async getProductosBajoStock(): Promise<Array<{ id: string; nombre: string; stock: number }>> {
    // Mock: Retorna array vacío
    // TODO: Implementar llamada a GET /api/v1/products?filter=lowStock
    console.info('[InventarioAdapter] getProductosBajoStock - Mock implementation');
    return [];
  }
}
