# System Design — Frontend POS
**Version:** 1.3
**Technology:** React 18 + TypeScript 5
**Architecture:** Hexagonal (Ports & Adapters)

## 1. Layered Architecture

```
UI LAYER         → React Components (SearchBar, Cart, PaymentPanel, etc.)
APPLICATION LAYER → usePOSStore (Zustand) + hooks (useSearch, useCart, usePayment, ...)
DOMAIN LAYER     → types/, ports/, calculadora.ts
INFRA LAYER      → adapters/ (production) + mocks/ (tests)
```

## 2. State Machine (UIState)

Valid transitions:
- LOGIN → IDLE (successful login)
- IDLE → BUSCANDO → RESULTADOS → CARRITO_ACTIVO
- CARRITO_ACTIVO → CALCULANDO_PAGO → PROCESANDO → VENTA_COMPLETA → IDLE
- CARRITO_ACTIVO → RESULTADOS (empty cart)
- IDLE/RESULTADOS → HISTORIAL → previous state
- VENTA_COMPLETA → DEVOLUCION → IDLE
- IDLE → INVENTARIO → IDLE (ADMIN only)
- IDLE → REPORTES → IDLE (ADMIN only)
- any authenticated state → ERROR → IDLE
- IDLE → LOGIN (logout)

## 3. Centralized State Model (POSState)

```typescript
export type EstadoUI = 'LOGIN'|'IDLE'|'BUSCANDO'|'RESULTADOS'|'CARRITO_ACTIVO'
  |'CALCULANDO_PAGO'|'PROCESANDO'|'VENTA_COMPLETA'|'HISTORIAL'
  |'DEVOLUCION'|'INVENTARIO'|'REPORTES'|'ERROR';

export type Rol = 'CAJERO' | 'ADMIN';
export type MetodoPago = 'EFECTIVO'|'TARJETA_DEBITO'|'TARJETA_CREDITO'|'TRANSFERENCIA'|'MIXTO';

export interface PagoItem { metodo: Exclude<MetodoPago,'MIXTO'>; monto: number; referencia?: string; }
export interface ItemCarrito { productoId: number; nombre: string; cantidad: number; precioUnitario: number; subtotal: number; }
export interface Resumen { subtotal: number; iva: number; total: number; }
export interface ErrorUI { codigo: string; mensaje: string; }
export interface Sesion { usuario: string; rol: Rol; token: string; }
export interface Producto { id: number; nombre: string; precio: number; stock: number; activo?: boolean; }
export interface ResumenVentaHistorial { ventaId: string; fechaHora: string; total: number; cantidadItems: number; }

export interface POSState {
  estado: EstadoUI;
  sesion: Sesion | null;
  query: string;
  productos: Producto[];
  carrito: ItemCarrito[];
  resumen: Resumen;
  metodoPago: MetodoPago | null;
  pagos: PagoItem[];
  montoPagado: number;
  cambio: number;
  historial: ResumenVentaHistorial[];
  estadoPrevio: EstadoUI | null;
  error: ErrorUI | null;
  ventaIdActual: string | null;
}
```

## 4. Calculator (Pure Domain)

```typescript
export const IVA_RATE = 0.19;
export function calcularResumen(carrito: ItemCarrito[]): Resumen
export function calcularCambio(montoPagado: number, total: number): number
export function calcularSubtotal(precio: number, cantidad: number): number
```

## 5. Domain Ports

| Port | Methods |
|---|---|
| IProductoPort | buscar(query) |
| IVentaPort | confirmar(carrito, total, pagos) |
| IVentaHistorialPort | listar() |
| IAuthPort | login(usuario, contrasena), logout(token) |
| IDevolucionPort | procesar(ventaId) |
| IInventarioPort | listar(), crear(), actualizar(), toggleActivo() |
| IReportePort | generarCierre(desde, hasta), exportarCSV(reporte) |
| IImpresionPort | imprimir(ventaId) |

## 6. Component Tree

```
POSApp
├── [LOGIN]     LoginForm
└── [authenticated]
    ├── ErrorBanner
    ├── Header (CartBadge, HistorialButton, UserBadge, LogoutButton)
    ├── [ADMIN] NavAdmin (InventarioButton, ReportesButton)
    ├── [INVENTARIO]  InventoryPanel > ProductTable, ProductFormModal
    ├── [REPORTES]    ReportsPanel > DateRangePicker, ReportSummary, ExportCSVButton
    ├── [HISTORIAL]   SalesHistory > SalesHistoryRow[], BackButton
    ├── [DEVOLUCION]  RefundPanel > RefundSummary, ConfirmRefundButton
    ├── [VENTA_COMPLETA] SuccessMessage, ReceiptButton, RefundButton
    └── [sale flow]
        ├── SearchBar > LoadingSpinner
        ├── ProductList > ProductCard[]
        ├── Cart > CartRow[]
        ├── OrderSummary
        └── PaymentPanel > PaymentMethodSelector, MontoInput, CambioDisplay, ConfirmButton
```

## 7. Directory Structure

```
src/
├── domain/
│   ├── types/POSState.ts
│   ├── ports/ (8 interfaces)
│   └── calculadora.ts
├── application/
│   ├── store/usePOSStore.ts
│   └── hooks/ (useSearch, useCart, usePayment, useAuth, useHistory, useRefund, useInventory, useReports, useReceipt)
├── infrastructure/
│   ├── adapters/ (8 adaptadores)
│   └── mocks/ (7 mocks)
└── ui/
    ├── components/ (13 componentes)
    └── POSApp.tsx
```

## 8. Key Decisions

- **Zustand** for reactive global state
- **JWT in memory** (never localStorage) — XSS security
- **Interchangeable adapters** — mocks in tests, production at runtime
- **Pure Calculator** — candidate for Property-Based Testing with fast-check
- **estadoPrevio** in store — allows restoring state when leaving HISTORIAL


## 9. Keyboard Shortcuts

The system supports complete keyboard navigation to maximize cashier efficiency and simulate professional POS systems.

| Shortcut | Action | Context | Priority |
|----------|--------|---------|----------|
| **F3** or **Ctrl+F** | Focus search input | Any screen | High |
| **↑ / ↓** | Navigate products | Product list | High |
| **Enter** | Add selected product | Product focused | High |
| **F9** | Confirm sale | Payment screen | High |
| **Escape** | Cancel/back | Any modal | High |
| **Tab / Shift+Tab** | Navigate sections | Sale flow | Medium |
| **F10** | Open history | Main screen | Medium |
| **+** | Increment quantity | Cart item | Medium |
| **-** | Decrement quantity | Cart item | Medium |
| **Delete** | Remove from cart | Cart item | Medium |
| **F1** or **?** | Show shortcuts help | Any screen | Low |

### Implementation Notes

- Use `useKeyboardShortcuts` hook in POSApp root component
- Prevent default browser behavior for F1-F12 keys
- Show visual feedback for selected items (highlight, border)
- Maintain focus management across state transitions
- Support both function keys and Ctrl combinations for accessibility

## 10. Barcode Scanner Integration

The system detects barcode scans and auto-adds products to cart, simulating real POS hardware.

### Detection Strategy

1. **Paste Event Detection**: Listen to `paste` events on SearchBar input
2. **Pattern Validation**: Validate format `/^[A-Z]{3}-\d{3}$/` (e.g., "LAP-001", "MON-042")
3. **Auto-Add Logic**: If exactly 1 product found with stock > 0, add to cart automatically
4. **User Feedback**: Show success/error notifications

### Behavior Matrix

| Scenario | Search Results | Action |
|----------|---------------|--------|
| Manual typing "LAP-001" | 1 product | Show in ProductList, wait for click |
| Paste "LAP-001" | 1 product, stock > 0 | Auto-add to cart, clear search, notify |
| Paste "LAP-001" | 1 product, stock = 0 | Show error "Out of stock" |
| Paste "LAP-001" | 0 products | Show error "Not found" |
| Paste "LAP-001" | 2+ products | Show in ProductList, manual selection |
| Paste "invalid" | N/A | Normal search behavior |

### Implementation Notes

- Differentiate paste vs. typing using `event.type === 'paste'`
- Clear search input after successful auto-add
- Use toast notifications for feedback
- Support hardware barcode scanners (simulate keyboard + Enter)

