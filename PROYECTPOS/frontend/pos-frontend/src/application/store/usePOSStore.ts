import { create } from 'zustand';
import type {
  POSState,
  EstadoUI,
  ItemCarrito,
  MetodoPago,
  PagoItem,
  ErrorUI,
  Producto,
  DatosRecibo,
} from '@domain/types/POSState';
import { calcularResumen, calcularCambio, calcularSubtotal } from '@domain/calculadora';

// ---------------------------------------------------------------------------
// Transiciones válidas — solo flujo de cajero
// ---------------------------------------------------------------------------
const TRANSICIONES_VALIDAS: Partial<Record<EstadoUI, EstadoUI[]>> = {
  IDLE:             ['BUSCANDO', 'ERROR'],
  BUSCANDO:         ['RESULTADOS', 'IDLE', 'ERROR'],
  RESULTADOS:       ['CARRITO_ACTIVO', 'BUSCANDO', 'IDLE', 'ERROR'],
  CARRITO_ACTIVO:   ['CALCULANDO_PAGO', 'RESULTADOS', 'ERROR'],
  CALCULANDO_PAGO:  ['PROCESANDO', 'CARRITO_ACTIVO', 'VENTA_COMPLETA', 'ERROR'],
  PROCESANDO:       ['VENTA_COMPLETA', 'ERROR'],
  VENTA_COMPLETA:   ['IDLE', 'ERROR'],
  ERROR:            ['IDLE', 'BUSCANDO', 'RESULTADOS', 'CARRITO_ACTIVO', 'CALCULANDO_PAGO'],
};

function transicionValida(desde: EstadoUI, hacia: EstadoUI): boolean {
  return TRANSICIONES_VALIDAS[desde]?.includes(hacia) ?? false;
}

// ---------------------------------------------------------------------------
// Estado inicial — arranca directo en IDLE (sin login)
// ---------------------------------------------------------------------------
const estadoInicial: POSState = {
  estado:          'IDLE',
  query:           '',
  productos:       [],
  carrito:         [],
  resumen:         { subtotal: 0, iva: 0, total: 0 },
  metodoPago:      null,
  pagos:           [],
  montoPagado:     0,
  cambio:          0,
  estadoPrevio:    null,
  error:           null,
  ventaIdActual:   null,
  datosRecibo:     null,
  recibosGuardados:{},
  resetCount:      0,
};

// ---------------------------------------------------------------------------
// Acciones
// ---------------------------------------------------------------------------
interface POSActions {
  setEstado:         (estado: EstadoUI) => void;
  setQuery:          (query: string) => void;
  setProductos:      (productos: Producto[]) => void;
  agregarAlCarrito:  (producto: Producto) => void;
  modificarCantidad: (productoId: string, cantidad: number) => void;
  eliminarDelCarrito:(productoId: string) => void;
  setMontoPagado:    (monto: number) => void;
  setMetodoPago:     (metodo: MetodoPago) => void;
  agregarPago:       (pago: PagoItem) => void;
  eliminarPago:      (index: number) => void;
  actualizarPago:    (index: number, pago: PagoItem) => void;
  resetPagos:        () => void;
  setVentaIdActual:  (ventaId: string) => void;
  setDatosRecibo:    (datos: DatosRecibo) => void;
  guardarRecibo:     (datos: DatosRecibo) => void;
  resetVenta:        () => void;
  irAIdle:           () => void;
  setError:          (error: ErrorUI) => void;
  clearError:        () => void;
}

// ---------------------------------------------------------------------------
// Store
// ---------------------------------------------------------------------------
export const usePOSStore = create<POSState & POSActions>((set, get) => ({
  ...estadoInicial,

  setEstado: (nuevoEstado) => {
    const { estado } = get();
    if (!transicionValida(estado, nuevoEstado)) return;
    set({ estado: nuevoEstado });
  },

  setQuery:    (query)    => set({ query }),
  setProductos:(productos)=> set({ productos }),

  agregarAlCarrito: (producto) => {
    const { carrito, estado } = get();
    if (producto.stock === 0) return;

    const existente = carrito.find((i) => i.productoId === producto.id);
    let nuevoCarrito: ItemCarrito[];

    if (existente) {
      if (existente.cantidad >= producto.stock) return;
      nuevoCarrito = carrito.map((i) =>
        i.productoId === producto.id
          ? { ...i, cantidad: i.cantidad + 1, subtotal: calcularSubtotal(i.precioUnitario, i.cantidad + 1) }
          : i
      );
    } else {
      nuevoCarrito = [
        ...carrito,
        {
          productoId:      producto.id,  // Ya es UUID string
          nombre:          producto.nombre,
          cantidad:        1,
          precioUnitario:  producto.precio,
          subtotal:        producto.precio,
          stockDisponible: producto.stock,
        },
      ];
    }

    const resumen = calcularResumen(nuevoCarrito);
    const nuevoEstado: EstadoUI = estado === 'RESULTADOS' ? 'CARRITO_ACTIVO' : estado;
    set({ carrito: nuevoCarrito, resumen, estado: nuevoEstado });
  },

  modificarCantidad: (productoId, cantidad) => {
    const { carrito } = get();
    const item = carrito.find((i) => i.productoId === productoId);

    if (cantidad <= 0) {
      const nuevoCarrito = carrito.filter((i) => i.productoId !== productoId);
      const resumen = calcularResumen(nuevoCarrito);
      const nuevoEstado: EstadoUI = nuevoCarrito.length === 0 ? 'RESULTADOS' : 'CARRITO_ACTIVO';
      set({ carrito: nuevoCarrito, resumen, estado: nuevoEstado });
      return;
    }

    const stockMax = item?.stockDisponible ?? Infinity;
    const cantidadFinal = Math.min(cantidad, stockMax);
    const nuevoCarrito = carrito.map((i) =>
      i.productoId === productoId
        ? { ...i, cantidad: cantidadFinal, subtotal: calcularSubtotal(i.precioUnitario, cantidadFinal) }
        : i
    );
    set({ carrito: nuevoCarrito, resumen: calcularResumen(nuevoCarrito) });
  },

  eliminarDelCarrito: (productoId) => {
    const { carrito } = get();
    const nuevoCarrito = carrito.filter((i) => i.productoId !== productoId);
    const resumen = calcularResumen(nuevoCarrito);
    const nuevoEstado: EstadoUI = nuevoCarrito.length === 0 ? 'RESULTADOS' : 'CARRITO_ACTIVO';
    set({ carrito: nuevoCarrito, resumen, estado: nuevoEstado });
  },

  setMontoPagado: (monto) => {
    const { resumen } = get();
    set({ montoPagado: monto, cambio: calcularCambio(monto, resumen.total) });
  },

  setMetodoPago:  (metodo) => set({ metodoPago: metodo, pagos: [] }),
  agregarPago:    (pago)   => set((s) => ({ pagos: [...s.pagos, pago] })),
  eliminarPago:   (index)  => set((s) => ({ pagos: s.pagos.filter((_, i) => i !== index) })),
  actualizarPago: (index, pago) => set((s) => ({ pagos: s.pagos.map((p, i) => i === index ? pago : p) })),
  resetPagos:     ()       => set({ pagos: [], metodoPago: null, montoPagado: 0, cambio: 0 }),

  setVentaIdActual: (ventaId) => set({ ventaIdActual: ventaId }),
  setDatosRecibo:   (datos)   => set({ datosRecibo: datos }),
  guardarRecibo:    (datos)   => set((s) => ({ recibosGuardados: { ...s.recibosGuardados, [datos.ventaId]: datos } })),

  resetVenta: () => set((s) => ({
    carrito:     [],
    resumen:     { subtotal: 0, iva: 0, total: 0 },
    query:       '',
    productos:   [],
    metodoPago:  null,
    pagos:       [],
    montoPagado: 0,
    cambio:      0,
    ventaIdActual: null,
    datosRecibo:   null,
    estado:        'IDLE',
    resetCount:    s.resetCount + 1,  // fuerza re-ejecución de useSearch
  })),

  irAIdle: () => set({
    carrito:     [],
    resumen:     { subtotal: 0, iva: 0, total: 0 },
    query:       '',
    productos:   [],
    metodoPago:  null,
    pagos:       [],
    montoPagado: 0,
    cambio:      0,
    ventaIdActual: null,
    estado:        'IDLE',
    // datosRecibo se conserva para mostrar el cambio
  }),

  setError:   (error) => set((s) => ({ error, estado: 'ERROR', estadoPrevio: s.estado })),
  clearError: () => set((s) => {
    // Si el estado previo era PROCESANDO, volver a CALCULANDO_PAGO para poder reintentar
    const destino: EstadoUI =
      s.estadoPrevio === 'PROCESANDO' ? 'CALCULANDO_PAGO'
      : s.estadoPrevio ?? 'IDLE';
    return { error: null, estado: destino, estadoPrevio: null };
  }),
}));
