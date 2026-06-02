/**
 * IInventarioPort - Port para gestión de inventario (solo Admin)
 * 
 * Permite actualizar stock, agregar productos, desactivar productos, etc.
 * Actualmente no implementado, pero documentado para expansión futura.
 */

export interface ActualizarStockPayload {
  productoId: string;
  cantidad: number;
  tipo: 'ENTRADA' | 'SALIDA' | 'AJUSTE';
  motivo?: string;
}

export interface CrearProductoPayload {
  codigo: string;
  codigoBarras?: string;
  nombre: string;
  precio: number;
  stock: number;
  categoria?: string;
  umbralBajoStock?: number;
}

export interface IInventarioPort {
  /**
   * Actualizar nivel de stock de un producto
   */
  actualizarStock(payload: ActualizarStockPayload): Promise<void>;

  /**
   * Crear nuevo producto en el sistema
   */
  crearProducto(payload: CrearProductoPayload): Promise<{ id: string }>;

  /**
   * Desactivar producto (no se elimina, solo se marca como inactivo)
   */
  desactivarProducto(productoId: string): Promise<void>;

  /**
   * Obtener productos con stock bajo
   */
  getProductosBajoStock(): Promise<Array<{ id: string; nombre: string; stock: number }>>;
}
