// Estados UI — sin LOGIN, INVENTARIO, REPORTES, DEVOLUCION, HISTORIAL (solo cajero)
export type EstadoUI =
  | 'IDLE'
  | 'BUSCANDO'
  | 'RESULTADOS'
  | 'CARRITO_ACTIVO'
  | 'CALCULANDO_PAGO'
  | 'PROCESANDO'
  | 'VENTA_COMPLETA'
  | 'ERROR';

export type MetodoPago =
  | 'EFECTIVO'
  | 'TARJETA_DEBITO'
  | 'TARJETA_CREDITO'
  | 'TRANSFERENCIA'
  | 'MIXTO';

export interface PagoItem {
  metodo: Exclude<MetodoPago, 'MIXTO'>;
  monto: number;
  referencia?: string;
}

export interface ItemCarrito {
  productoId: number;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  stockDisponible: number;
}

export interface Resumen {
  subtotal: number;
  iva: number;
  total: number;
}

export interface ErrorUI {
  codigo: string;
  mensaje: string;
}

export interface Producto {
  id: number;
  nombre: string;
  precio: number;
  stock: number;
  activo?: boolean;
}

export interface DatosRecibo {
  ventaId: string;
  fechaHora: string;
  cajero: string;
  items: Array<{ nombre: string; cantidad: number; subtotal: number }>;
  subtotal: number;
  iva: number;
  total: number;
  metodoPago: string;
  montoPagado: number;
  cambio: number;
}

export interface POSState {
  estado: EstadoUI;
  query: string;
  productos: Producto[];
  carrito: ItemCarrito[];
  resumen: Resumen;
  metodoPago: MetodoPago | null;
  pagos: PagoItem[];
  montoPagado: number;
  cambio: number;
  estadoPrevio: EstadoUI | null;
  error: ErrorUI | null;
  ventaIdActual: string | null;
  datosRecibo: DatosRecibo | null;
  recibosGuardados: Record<string, DatosRecibo>;
}
