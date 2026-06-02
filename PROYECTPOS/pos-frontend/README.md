# Frontend POS — React + TypeScript

## Arquitectura Cliente-Servidor

El frontend es una **Single Page Application (SPA)** construida con React 18 que consume el backend serverless AWS SAM a través de API Gateway.

```
┌─────────────┐         HTTPS          ┌──────────────┐
│   Browser   │ ◄──────────────────► │ API Gateway  │
│  (React 18) │    JSON REST API      │              │
│             │                        │              │
│  - Search   │                        │  /products   │
│  - Cart     │                        │  /sales      │
│  - Payment  │                        │  /reports    │
└─────────────┘                        └──────────────┘
                                              │
                                              ▼
                                       ┌──────────────┐
                                       │   Lambda     │
                                       │  Functions   │
                                       │  (Java 21)   │
                                       └──────────────┘
                                              │
                                              ▼
                                       ┌──────────────┐
                                       │  DynamoDB    │
                                       │   Tables     │
                                       └──────────────┘
```

## Framework Elegido: React 18 + TypeScript 5

### Justificación

| Criterio | Ventaja de React + TypeScript |
|----------|-------------------------------|
| **Type Safety** | TypeScript previene errores en tiempo de compilación, detecta bugs antes de ejecutar |
| **Hooks** | useState, useEffect permiten lógica reutilizable sin class components |
| **Ecosystem** | Zustand para state management, Vitest para testing, Vite para build rápido |
| **Performance** | Virtual DOM optimiza renderizado, solo actualiza lo necesario |
| **Developer Experience** | Hot reload instantáneo, debugging tools en Chrome DevTools |
| **Community** | Documentación extensa, librerías maduras, soporte activo |

### Alternativas Consideradas

| Framework | Por qué NO se eligió |
|-----------|----------------------|
| **Vue 3** | Menos experiencia del equipo, curva de aprendizaje |
| **Angular** | Overhead excesivo para un POS, bundle size grande |
| **Svelte** | Ecosystem menos maduro, menos librerías disponibles |
| **Vanilla JS** | Falta de estructura para aplicación compleja, difícil de mantener |

**Decisión final:** React 18 + TypeScript 5 con **arquitectura hexagonal** para desacoplar dominio de infraestructura y facilitar testing.

## Arquitectura Hexagonal (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────┐
│              UI LAYER (React Components)                │
│  SearchBar, Cart, PaymentPanel, OrderSummary, etc.      │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│         APPLICATION LAYER (Zustand Store)               │
│  usePOSStore + hooks (useSearch, useCart, usePayment)   │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│            DOMAIN LAYER (Pure TypeScript)               │
│  types/, ports/ (interfaces), calculadora.ts            │
│  - IProductoPort, IVentaPort, IImpresionPort            │
│  - IAuthPort, IInventarioPort, etc. (placeholders)      │
│  - calcularResumen, calcularCambio, calcularSubtotal    │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│       INFRASTRUCTURE LAYER (Adapters)                   │
│  ProductoAdapter, VentaAdapter (fetch API Gateway)      │
│  AuthAdapter, InventarioAdapter, etc. (mocks)           │
│  mocks/ (para testing sin backend)                      │
└─────────────────────────────────────────────────────────┘
```

**Ventajas de esta arquitectura:**
- ✅ **Testabilidad:** Dominio se prueba sin infraestructura
- ✅ **Flexibilidad:** Cambiar API REST por GraphQL sin tocar dominio
- ✅ **Claridad:** Separación clara de responsabilidades
- ✅ **Mantenibilidad:** Cambios en UI no afectan lógica de negocio

## Funcionalidades

### Vista de Productos
- **Búsqueda en tiempo real** por nombre o código
- **Listado dinámico** con precio formateado ($X.XXX)
- **Indicador de stock** (disponible/agotado)
- **Botón "Agregar"** para añadir al carrito

### Flujo de Ventas
- **Carrito interactivo** con modificación de cantidades (+/-)
- **Cálculo automático de IVA** (19% Colombia)
- **Resumen en tiempo real** (Subtotal, IVA, Total)
- **Múltiples métodos de pago:**
  - Efectivo (con cálculo de cambio)
  - Tarjeta débito/crédito
  - Transferencia bancaria
  - Mixto (combinación de métodos)
- **Confirmación de venta** con mensaje de éxito

### Manejo de Respuestas
- ✅ **Éxito:** "Venta completada! Cambio: $X.XXX"
- ❌ **Error:** "No se pudo procesar la venta. Intente nuevamente."
- ⚠️ **Validación:** "El monto pagado es menor al total"

### Funcionalidades Adicionales (según specs)
- **Historial de ventas** del turno
- **Devoluciones** de ventas completadas
- **Gestión de inventario** (solo Admin)
- **Reportes de cierre** (solo Admin)
- **Impresión de recibos** (80mm)

## Configuración del API Gateway

### Archivo: `.env`

```env
VITE_API_BASE_URL=https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod
```

**⚠️ Importante:** No subir `.env` a Git. Usar `.env.example` como plantilla.

### Archivo: `.env.example`

```env
# URL base del API Gateway desplegado en AWS
VITE_API_BASE_URL=https://your-api-gateway-url.amazonaws.com/Prod
```

**Instrucciones:**
1. Copiar `.env.example` a `.env`
2. Reemplazar `your-api-gateway-url` con la URL real del API Gateway
3. Reiniciar el servidor de desarrollo (`npm run dev`)

## Instrucciones de Ejecución

### Prerrequisitos

- **Node.js 18+** (verificar con `node -v`)
- **npm 9+** (verificar con `npm -v`)

### Paso 1: Instalar dependencias

```bash
cd PROYECTPOS/frontend/pos-frontend
npm install
```

**Dependencias principales:**
- `react@18` + `react-dom@18`
- `typescript@5`
- `zustand` (state management)
- `vitest` + `@testing-library/react` (testing)
- `fast-check` (property-based testing)

### Paso 2: Configurar API Gateway

```bash
cp .env.example .env
# Editar .env con la URL real del API Gateway
```

**Ejemplo:**
```env
VITE_API_BASE_URL=https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod
```

### Paso 3: Ejecutar en desarrollo

```bash
npm run dev
```

**Salida esperada:**
```
  VITE v5.0.0  ready in 500 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
  ➜  press h to show help
```

Abrir en navegador: **http://localhost:5173**

### Paso 4: Build para producción

```bash
npm run build
```

**Salida:** Archivos optimizados en `dist/`

### Paso 5: Preview de producción

```bash
npm run preview
```

Abrir: **http://localhost:4173**

## Capturas de Pantalla

### Listado de Productos Cargado desde el API
![Productos](./docs/screenshots/productos-list.png)

**Funcionalidad mostrada:**
- SearchBar con query "mouse"
- Lista de productos con nombre, precio y botón "Agregar"
- Indicador de stock disponible

### Registro de Venta Exitosa con Respuesta del API
![Venta exitosa](./docs/screenshots/venta-exitosa.png)

**Funcionalidad mostrada:**
- Carrito con 2 items
- OrderSummary con Subtotal, IVA (19%) y Total
- PaymentPanel con monto pagado y cambio calculado
- Mensaje de éxito: "Venta completada! Cambio: $5.000"

### Manejo de Error (API Caído o Respuesta Inválida)
![Error](./docs/screenshots/error-api.png)

**Funcionalidad mostrada:**
- ErrorBanner visible en la parte superior
- Mensaje: "No se pudo cargar los productos. Intente nuevamente."
- Botón "Reintentar" para volver a invocar el API
- Resto de la interfaz sigue funcional

## Proceso SDD (Spec-Driven Development)

### 1. Specs Primero

Antes de escribir cualquier línea de código, creamos tres documentos en `.kiro/specs/pos-frontend/`:

- **requirements.md**: 19 requisitos funcionales con acceptance criteria detallados
- **design.md**: State machine (13 estados), componentes, ports, arquitectura hexagonal
- **tasks.md**: 18 fases de implementación con subtareas

**Evidencia:** Timestamps de commits muestran specs antes que código.

### 2. Implementación Guiada

Cada componente se implementó siguiendo:

1. **Requisito funcional** en requirements.md
2. **Diseño de componente** en design.md
3. **Tarea específica** en tasks.md

**Ejemplo de trazabilidad:**

| Requisito | Componente | Tarea |
|-----------|------------|-------|
| SPEC-001 (Product Search) | SearchBar.tsx | Task 5.1: Implement SearchBar |
| SPEC-002 (Add to Cart) | Cart.tsx | Task 6.2: Implement Cart |
| SPEC-004 (VAT Calculation) | calculadora.ts | Task 1.5: Create calculadora |
| SPEC-006 (Sale Confirmation) | PaymentPanel.tsx | Task 9.1: Implement confirmarVenta |

### 3. Validación

- **Acceptance criteria** → casos de prueba con Vitest
- **State machine** → property-based testing con fast-check
- **Calculator** → 100+ casos generados aleatoriamente

**Ejemplo:**

Requirements.md dice:
```
WHEN the Cashier types fewer than 2 characters in the SearchBar,
THEN THE System SHALL keep the UIState in IDLE without invoking IProductoPort.buscar
```

Test verifica:
```typescript
test('SearchBar does not search with < 2 characters', () => {
  render(<SearchBar />);
  const input = screen.getByRole('searchbox');
  
  fireEvent.change(input, { target: { value: 'a' } });
  
  expect(mockProductoPort.buscar).not.toHaveBeenCalled();
});
```

### 4. Beneficios del SDD

- ✅ **Claridad:** Todos saben qué construir antes de empezar
- ✅ **Trazabilidad:** Cada componente tiene un "por qué" documentado
- ✅ **Testabilidad:** Acceptance criteria son casos de prueba
- ✅ **Documentación:** Specs son documentación viva
- ✅ **Colaboración:** Equipo revisa specs antes de implementar
- ✅ **Calidad:** Menos bugs porque el diseño se valida antes de codificar

## Fundamentos Demostrados

### HTML5 Semántico

```tsx
<header>
  <nav aria-label="Navegación principal">
    <button>Productos</button>
    <button>Historial</button>
  </nav>
</header>

<main>
  <section aria-label="Búsqueda de productos">
    <input type="search" placeholder="Buscar producto..." />
  </section>
  
  <article>
    <h2>Carrito de Compras</h2>
    <table>
      <thead>
        <tr>
          <th>Producto</th>
          <th>Cantidad</th>
          <th>Subtotal</th>
        </tr>
      </thead>
      <tbody>
        {/* CartRows */}
      </tbody>
    </table>
  </article>
</main>

<footer>
  <p>&copy; 2024 POS System</p>
</footer>
```

**No todo es `<div>`:** Usamos etiquetas semánticas apropiadas (`<header>`, `<nav>`, `<main>`, `<section>`, `<article>`, `<table>`).

### CSS (Flexbox y Grid)

```css
/* Grid para layout principal */
.pos-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1rem;
  height: 100vh;
}

/* Flexbox para payment panel */
.payment-panel {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
}

/* Box model */
.cart-item {
  margin: 0.5rem 0;
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

/* Flexbox para botones */
.button-group {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
```

**Comprensión demostrada:**
- ✅ Grid para layouts complejos (2 columnas)
- ✅ Flexbox para alineación y distribución
- ✅ Box model (margin, padding, border)
- ✅ Estilos propios (no solo clases de framework)

### JavaScript (Async/Await y Fetch)

```typescript
// Adapter que consume API Gateway
export class ProductoAdapter implements IProductoPort {
  private baseUrl: string;

  constructor() {
    this.baseUrl = import.meta.env.VITE_API_BASE_URL;
  }

  async buscar(query: string): Promise<Producto[]> {
    try {
      const response = await fetch(
        `${this.baseUrl}/api/v1/products?q=${encodeURIComponent(query)}`
      );
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      const data = await response.json();
      return data.products;
      
    } catch (error) {
      console.error('Error fetching products:', error);
      throw new Error('No se pudo cargar los productos. Intente nuevamente.');
    }
  }
}
```

**Comprensión demostrada:**
- ✅ `async/await` para operaciones asíncronas
- ✅ `fetch` para consumo de APIs REST
- ✅ `try/catch` para manejo de errores
- ✅ Validación de respuesta (`response.ok`)
- ✅ Parsing de JSON (`response.json()`)
- ✅ Propagación de errores con `throw`

## Estructura del Repositorio

```
pos-frontend/
├── .kiro/specs/pos-frontend/
│   ├── requirements.md      # 19 requisitos funcionales
│   ├── design.md            # State machine, componentes, ports
│   └── tasks.md             # 18 fases de implementación
│
├── src/
│   ├── domain/              # Lógica de negocio pura
│   │   ├── types/
│   │   │   └── POSState.ts
│   │   ├── ports/           # Interfaces (contratos)
│   │   │   ├── IProductoPort.ts
│   │   │   ├── IVentaPort.ts
│   │   │   ├── IImpresionPort.ts
│   │   │   ├── IAuthPort.ts (placeholder)
│   │   │   ├── IInventarioPort.ts (placeholder)
│   │   │   ├── IReportesPort.ts (placeholder)
│   │   │   ├── IHistorialPort.ts (placeholder)
│   │   │   └── IDevolucionPort.ts (placeholder)
│   │   └── calculadora.ts   # Funciones puras
│   │
│   ├── application/         # Orquestación de estado
│   │   ├── store/
│   │   │   └── usePOSStore.ts  # Zustand store
│   │   └── hooks/
│   │       ├── useSearch.ts
│   │       ├── useCart.ts
│   │       ├── usePayment.ts
│   │       ├── useReceipt.ts
│   │       ├── useFocusManager.ts
│   │       ├── useKeyboardShortcuts.ts
│   │       ├── useAuth.ts (placeholder)
│   │       ├── useInventario.ts (placeholder)
│   │       ├── useReportes.ts (placeholder)
│   │       ├── useHistorial.ts (placeholder)
│   │       └── useDevolucion.ts (placeholder)
│   │
│   ├── infrastructure/      # Implementaciones concretas
│   │   ├── adapters/
│   │   │   ├── ProductoAdapter.ts
│   │   │   ├── VentaAdapter.ts
│   │   │   ├── ImpresionAdapter.ts
│   │   │   ├── AuthAdapter.ts (mock)
│   │   │   ├── InventarioAdapter.ts (mock)
│   │   │   ├── ReportesAdapter.ts (mock)
│   │   │   ├── HistorialAdapter.ts (mock)
│   │   │   └── DevolucionAdapter.ts (mock)
│   │   └── mocks/
│   │       ├── auth.mock.ts
│   │       ├── inventario.mock.ts
│   │       ├── reportes.mock.ts
│   │       ├── historial.mock.ts
│   │       └── devolucion.mock.ts
│   │
│   └── ui/                  # Componentes React
│       ├── components/
│       │   ├── SearchBar/
│       │   ├── ProductList/
│       │   ├── Cart/
│       │   ├── OrderSummary/
│       │   ├── PaymentPanel/
│       │   ├── ErrorBanner/
│       │   ├── Header/
│       │   ├── ReceiptButton/
│       │   ├── ReceiptPortal/
│       │   └── ReceiptViewer/
│       └── POSApp.tsx       # Componente raíz
│
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── .env.example
├── .gitignore
└── README.md
```

## Testing

### Ejecutar tests

```bash
npm run test
```

### Cobertura

```bash
npm run test:coverage
```

**Target:** ≥70%

### Property-Based Testing

```typescript
// calculadora.property.test.ts
import fc from 'fast-check';
import { calcularResumen } from './calculadora';

test('IVA is always 19% of subtotal', () => {
  fc.assert(
    fc.property(
      fc.array(fc.record({
        precioUnitario: fc.nat(),
        cantidad: fc.nat()
      })),
      (items) => {
        const carrito = items.map(item => ({
          ...item,
          subtotal: item.precioUnitario * item.cantidad
        }));
        
        const resumen = calcularResumen(carrito);
        const expectedIva = Math.round(resumen.subtotal * 0.19);
        
        expect(resumen.iva).toBe(expectedIva);
      }
    ),
    { numRuns: 100 }
  );
});
```

**100+ casos generados aleatoriamente** para verificar propiedades matemáticas.

## Troubleshooting

### Error: "Cannot find module 'zustand'"

**Solución:**
```bash
npm install
```

### Error: "VITE_API_BASE_URL is not defined"

**Solución:**
```bash
cp .env.example .env
# Editar .env con la URL correcta
```

### Error: "CORS policy blocked"

**Causa:** API Gateway no tiene CORS configurado.

**Solución:** Verificar que el backend retorna headers CORS:
```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, OPTIONS
```

## Licencia

MIT

---

**Desarrollado con ❤️ usando Spec-Driven Development**
