# POS Frontend - Sistema de Punto de Venta

Sistema de punto de venta (POS) web desarrollado con React + TypeScript siguiendo metodología **Spec-Driven Development (SDD)** y arquitectura hexagonal.

---

## 📋 Tabla de Contenidos

- [Arquitectura Cliente-Servidor](#arquitectura-cliente-servidor)
- [Framework Elegido](#framework-elegido)
- [Instalación y Ejecución](#instalación-y-ejecución)
- [Configuración del API Gateway](#configuración-del-api-gateway)
- [Atajos de Teclado](#atajos-de-teclado)
- [Capturas de Pantalla](#capturas-de-pantalla)
- [Proceso Spec-Driven Development](#proceso-spec-driven-development)
- [Estructura del Proyecto](#estructura-del-proyecto)

---

## 🏗️ Arquitectura Cliente-Servidor

Este proyecto implementa una arquitectura **cliente-servidor distribuida** con separación clara de responsabilidades:

### **Cliente (Frontend)**
- **Tecnología**: React 18 + TypeScript + Vite
- **Responsabilidades**:
  - Interfaz de usuario interactiva
  - Validación de datos en el cliente
  - Gestión de estado local con Zustand
  - Consumo de API REST mediante `fetch`
  - Renderizado dinámico de productos y carrito

### **Servidor (Backend)**
- **Tecnología**: AWS Lambda (Java 21) + DynamoDB + API Gateway
- **Responsabilidades**:
  - Lógica de negocio (cálculo de totales, validación de stock)
  - Persistencia de datos en DynamoDB
  - Autenticación y autorización (si aplica)
  - Exposición de endpoints REST

### **Comunicación**
- **Protocolo**: HTTP/HTTPS
- **Formato**: JSON
- **Endpoints**:
  - `GET /products?type=name&q={query}` - Búsqueda de productos
  - `POST /sales` - Registro de ventas

```
┌─────────────────┐         HTTPS/JSON          ┌──────────────────┐
│                 │  ────────────────────────>   │                  │
│  React Frontend │                              │  AWS API Gateway │
│  (localhost)    │  <────────────────────────   │                  │
└─────────────────┘                              └──────────────────┘
                                                          │
                                                          ▼
                                                  ┌──────────────────┐
                                                  │  Lambda Functions│
                                                  │  (Java 21)       │
                                                  └──────────────────┘
                                                          │
                                                          ▼
                                                  ┌──────────────────┐
                                                  │    DynamoDB      │
                                                  │  (NoSQL Tables)  │
                                                  └──────────────────┘
```

---

## ⚛️ Framework Elegido: React con Hooks

### **Justificación Técnica**

**React** fue seleccionado por las siguientes razones:

1. **Componentes Reutilizables**: Permite crear componentes modulares (`ProductList`, `Cart`, `PaymentPanel`) que se pueden reutilizar y testear independientemente.

2. **Hooks Modernos**: Uso de `useState`, `useEffect` y hooks personalizados (`useSearch`, `usePayment`, `useKeyboardShortcuts`) para lógica reutilizable sin clases.

3. **Gestión de Estado Eficiente**: Integración con Zustand para un store global ligero y reactivo, evitando prop drilling.

4. **Ecosistema Maduro**: Amplia disponibilidad de librerías, herramientas de testing (Vitest, Testing Library) y documentación.

5. **TypeScript First-Class**: Soporte nativo para TypeScript, permitiendo detección de errores en tiempo de desarrollo.

6. **Rendimiento**: Virtual DOM optimizado para actualizaciones eficientes del carrito y lista de productos.

### **Alternativas Consideradas**

- **Vue 3**: Excelente opción, pero React tiene mayor adopción en proyectos empresariales.
- **Angular**: Demasiado pesado para un POS simple, curva de aprendizaje más pronunciada.
- **Vanilla JS**: Viable pero requiere más código boilerplate para gestión de estado y renderizado.

---

## 🚀 Instalación y Ejecución

### **Requisitos Previos**

- Node.js 18+ y npm 9+
- Git

### **Pasos de Instalación**

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/proyectPOSAWS.git
cd proyectPOSAWS/PROYECTPOS/frontend/pos-frontend

# 2. Instalar dependencias
npm install

# 3. Configurar variables de entorno (ver siguiente sección)
cp .env.example .env
# Editar .env con la URL real del API Gateway

# 4. Ejecutar en modo desarrollo
npm run dev

# 5. Abrir en el navegador
# http://localhost:5173
```

### **Scripts Disponibles**

```bash
npm run dev          # Servidor de desarrollo con hot-reload
npm run build        # Build de producción (dist/)
npm run preview      # Preview del build de producción
npm run test         # Ejecutar tests unitarios
npm run lint         # Verificar código con ESLint
```

---

## ⚙️ Configuración del API Gateway

### **Paso 1: Obtener la URL del API Gateway**

Después de ejecutar `sam deploy` en el proyecto `pos-sam/`, obtendrás una URL como:

```
https://abc123xyz.execute-api.us-east-1.amazonaws.com/Prod
```

### **Paso 2: Configurar Variable de Entorno**

Crear archivo `.env` en la raíz del proyecto frontend:

```bash
# .env
VITE_API_BASE_URL=https://abc123xyz.execute-api.us-east-1.amazonaws.com/Prod
```

### **Paso 3: Verificar Configuración**

El archivo `src/config.ts` lee automáticamente esta variable:

```typescript
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 
  'https://your-api-id.execute-api.us-east-1.amazonaws.com/Prod';
```

### **Endpoints Consumidos**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/products?type=all` | Listar todos los productos |
| GET | `/products?type=name&q={query}` | Buscar productos por nombre |
| POST | `/sales` | Registrar una venta |

---

## ⌨️ Atajos de Teclado

El sistema implementa **atajos de teclado estándar POS** para agilizar el flujo de trabajo del cajero:

| Tecla | Acción | Contexto |
|-------|--------|----------|
| **F1** o **/** | Enfocar buscador de productos | Cualquier momento |
| **F2** | Ir al panel de pago | Solo si hay items en el carrito |
| **Enter** | Confirmar venta | Solo en panel de pago |
| **Escape** | Cancelar pago / Limpiar búsqueda | Panel de pago o búsqueda activa |
| **F12** | Nueva venta (resetear todo) | Cualquier momento |

### **Implementación**

Los atajos están implementados en el hook personalizado `useKeyboardShortcuts.ts`:

```typescript
// src/application/hooks/useKeyboardShortcuts.ts
export function useKeyboardShortcuts() {
  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (e.key === 'F1' || e.key === '/') {
        // Enfocar buscador
      }
      // ... más atajos
    }
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [estado, carrito]);
}
```

---

## 📸 Capturas de Pantalla

### 1. Listado de Productos desde API

![Listado de Productos](./docs/screenshots/productos-listado.png)

**Descripción**: Vista principal mostrando productos cargados desde el API Gateway de AWS. Se puede observar:
- Barra de búsqueda funcional
- Lista de productos con nombre, precio y stock
- Botón "Agregar" para cada producto

**Endpoint consumido**: `GET /products?type=all`

---

### 2. Registro de Venta Exitosa

![Venta Exitosa](./docs/screenshots/venta-exitosa.png)

**Descripción**: Confirmación de venta registrada correctamente. Muestra:
- ID de venta generado por el backend
- Cálculo de cambio correcto
- Botón para imprimir recibo
- Opción de nueva venta

**Endpoint consumido**: `POST /sales`

**Respuesta del API**:
```json
{
  "success": true,
  "data": {
    "ventaId": "VNT-20260530-001"
  }
}
```

---

### 3. Manejo de Error (API Caído)

![Error de Conexión](./docs/screenshots/error-api-caido.png)

**Descripción**: Manejo robusto de errores cuando el API no está disponible:
- Banner de error visible
- Mensaje descriptivo para el usuario
- Botón de reintentar
- La aplicación no se rompe, mantiene el estado

**Código de manejo**:
```typescript
try {
  const res = await fetch(`${API_BASE_URL}/products`);
  if (!res.ok) throw await toPosApiError(res);
} catch (error) {
  setError({
    codigo: 'ERROR_CONEXION',
    mensaje: 'No se pudo conectar con el servidor. Verifica tu conexión.'
  });
}
```

---

## 📐 Proceso Spec-Driven Development

Este proyecto fue desarrollado siguiendo la metodología **SDD (Spec-Driven Development)**, donde los specs guían la implementación:

### **Fase 1: Especificaciones (`.kiro/specs/pos-frontend/`)**

Antes de escribir código, se crearon 3 documentos:

#### **1. requirements.md**
Define **QUÉ** debe hacer el sistema:
- Requisitos funcionales (búsqueda, carrito, pago)
- Criterios de aceptación medibles
- Endpoints del API a consumir
- Respuestas esperadas del backend

**Ejemplo**:
```markdown
### RF-001: Búsqueda de Productos
**Como** cajero
**Quiero** buscar productos por nombre
**Para** agregarlos rápidamente al carrito

**Criterios de Aceptación**:
- [ ] El buscador debe tener debounce de 300ms
- [ ] Debe consumir GET /products?type=name&q={query}
- [ ] Debe mostrar "No se encontraron productos" si la lista está vacía
```

#### **2. design.md**
Define **CÓMO** se implementará:
- Arquitectura hexagonal (domain, application, infrastructure, ui)
- Estructura de componentes React
- Gestión de estado con Zustand
- Contrato con el API (request/response)
- Justificación de React como framework

**Ejemplo**:
```markdown
## Arquitectura de Componentes

POSApp
├── Header
├── SearchBar (consume IProductoPort)
├── ProductList
│   └── ProductCard
├── Cart
│   └── CartRow
├── OrderSummary
└── PaymentPanel (consume IVentaPort)
```

#### **3. tasks.md**
Define el **ORDEN** de implementación:
- Lista de tareas numeradas
- Dependencias entre tareas
- Criterios de "done" para cada tarea

**Ejemplo**:
```markdown
- [ ] 1. Scaffolding y dominio base
  - [ ] 1.1 Inicializar proyecto React + TypeScript con Vite
  - [ ] 1.2 Instalar dependencias (zustand, vitest, fast-check)
  - [ ] 1.3 Crear tipos del dominio (POSState.ts, Producto.ts)
```

---

### **Fase 2: Implementación Guiada por Specs**

Cada tarea del `tasks.md` se implementó verificando que cumpliera los criterios de `requirements.md`:

```typescript
// Ejemplo: Implementación de SearchBar según SPEC-001
export function SearchBar({ productoPort }: Props) {
  const [query, setQuery] = useState('');
  const setProductos = usePOSStore(s => s.setProductos);
  
  // ✅ Criterio: Debounce de 300ms
  const debouncedQuery = useDebounce(query, 300);
  
  useEffect(() => {
    if (debouncedQuery.length < 2) return;
    
    // ✅ Criterio: Consumir GET /products?type=name&q={query}
    productoPort.buscar(debouncedQuery)
      .then(setProductos)
      .catch(handleError);
  }, [debouncedQuery]);
  
  // ✅ Criterio: Mostrar mensaje si lista vacía
  return (
    <input 
      type="search"
      value={query}
      onChange={e => setQuery(e.target.value)}
      placeholder="Buscar producto (F1)"
    />
  );
}
```

---

### **Fase 3: Verificación contra Specs**

Cada feature se verificó contra los criterios de aceptación:

| Spec | Criterio | Estado | Evidencia |
|------|----------|--------|-----------|
| RF-001 | Debounce 300ms | ✅ | `useDebounce(query, 300)` |
| RF-001 | Consumir GET /products | ✅ | `ProductoAdapter.buscar()` |
| RF-002 | Agregar al carrito | ✅ | `usePOSStore.agregarAlCarrito()` |
| RF-003 | Calcular IVA 19% | ✅ | `calculadora.ts` con tests |
| RF-004 | Confirmar venta | ✅ | `POST /sales` en `VentaAdapter` |

---

### **Beneficios del Enfoque SDD**

1. **Claridad**: Los specs eliminan ambigüedad sobre qué implementar
2. **Trazabilidad**: Cada línea de código mapea a un requisito
3. **Testabilidad**: Los criterios de aceptación se convierten en tests
4. **Documentación**: Los specs sirven como documentación viva
5. **Colaboración**: Facilita revisión de código y onboarding

---

## 📁 Estructura del Proyecto

```
pos-frontend/
├── .kiro/
│   └── specs/
│       └── pos-frontend/
│           ├── requirements.md    # Requisitos funcionales
│           ├── design.md          # Decisiones de diseño
│           └── tasks.md           # Plan de implementación
├── src/
│   ├── domain/                    # Lógica de negocio pura
│   │   ├── types/
│   │   │   └── POSState.ts        # Tipos del dominio
│   │   ├── ports/
│   │   │   ├── IProductoPort.ts   # Contrato de productos
│   │   │   ├── IVentaPort.ts      # Contrato de ventas
│   │   │   └── IImpresionPort.ts  # Contrato de impresión
│   │   ├── calculadora.ts         # Cálculos de venta
│   │   └── errors/
│   │       └── PosApiError.ts     # Errores del dominio
│   ├── application/               # Casos de uso
│   │   ├── store/
│   │   │   └── usePOSStore.ts     # Store global Zustand
│   │   └── hooks/
│   │       ├── useSearch.ts       # Hook de búsqueda
│   │       ├── usePayment.ts      # Hook de pago
│   │       ├── useReceipt.ts      # Hook de recibo
│   │       └── useKeyboardShortcuts.ts  # Atajos de teclado
│   ├── infrastructure/            # Adaptadores externos
│   │   └── adapters/
│   │       ├── ProductoAdapter.ts # Adaptador API productos
│   │       ├── VentaAdapter.ts    # Adaptador API ventas
│   │       └── ImpresionAdapter.ts # Adaptador impresión
│   ├── ui/                        # Componentes React
│   │   ├── components/
│   │   │   ├── Header/
│   │   │   ├── SearchBar/
│   │   │   ├── ProductList/
│   │   │   ├── Cart/
│   │   │   ├── OrderSummary/
│   │   │   ├── PaymentPanel/
│   │   │   ├── ErrorBanner/
│   │   │   ├── ReceiptButton/
│   │   │   ├── ReceiptPortal/
│   │   │   └── ReceiptViewer/
│   │   └── POSApp.tsx             # Componente raíz
│   ├── config.ts                  # Configuración API Gateway
│   └── main.tsx                   # Entry point
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
├── .env.example                   # Ejemplo de variables de entorno
├── .gitignore
└── README.md                      # Este archivo
```

### **Arquitectura Hexagonal**

El proyecto sigue **Ports & Adapters** para desacoplar la lógica de negocio:

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│  (React Components - SearchBar, Cart, PaymentPanel)    │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                Application Layer                        │
│  (Hooks - useSearch, usePayment, useKeyboardShortcuts)  │
│  (Store - usePOSStore con Zustand)                      │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                  Domain Layer                           │
│  (Ports - IProductoPort, IVentaPort)                    │
│  (Types - POSState, Producto, ItemCarrito)              │
│  (Logic - calculadora.ts)                               │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Infrastructure Layer                       │
│  (Adapters - ProductoAdapter, VentaAdapter)             │
│  (HTTP Client - fetch con manejo de errores)            │
└─────────────────────────────────────────────────────────┘
```

---

## 🧪 Fundamentos HTML5, CSS y JavaScript

### **HTML5 Semántico**

El proyecto usa etiquetas semánticas apropiadas:

```html
<header>  <!-- Encabezado del POS -->
<main>    <!-- Contenido principal -->
<section> <!-- Secciones de productos y carrito -->
<article> <!-- Cada producto individual -->
<form>    <!-- Formulario de pago -->
<button>  <!-- Acciones del usuario -->
```

**No todo es `<div>`** - cada elemento tiene significado semántico.

---

### **CSS: Box Model, Flexbox y Grid**

#### **Box Model**
```css
.card {
  padding: 1rem;        /* Espacio interno */
  margin: 0.5rem;       /* Espacio externo */
  border: 1px solid;    /* Borde */
  box-sizing: border-box; /* Incluir padding en width */
}
```

#### **Flexbox para Layouts**
```css
.cart {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
```

#### **Grid para Productos**
```css
.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}
```

---

### **JavaScript: Async/Await, Fetch, Manejo de Errores**

#### **Consumo de API con Fetch**
```typescript
async function buscarProductos(query: string): Promise<Producto[]> {
  try {
    const res = await fetch(`${API_BASE_URL}/products?type=name&q=${query}`, {
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}: ${res.statusText}`);
    }
    
    const data = await res.json();
    return data.data;
    
  } catch (error) {
    console.error('Error al buscar productos:', error);
    throw error;
  }
}
```

#### **Manejo de Eventos**
```typescript
function SearchBar() {
  const handleKeyDown = (e: KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      buscarProductos(query);
    }
  };
  
  return (
    <input 
      onKeyDown={handleKeyDown}
      onChange={e => setQuery(e.target.value)}
    />
  );
}
```

#### **Promesas y Async/Await**
```typescript
// ❌ Callback Hell (evitado)
fetch(url).then(res => {
  res.json().then(data => {
    procesarDatos(data).then(resultado => {
      // ...
    });
  });
});

// ✅ Async/Await (usado en el proyecto)
async function procesarVenta() {
  const productos = await buscarProductos(query);
  const resultado = await confirmarVenta(carrito);
  return resultado;
}
```

---

## 🔧 Tecnologías Utilizadas

| Categoría | Tecnología | Versión | Propósito |
|-----------|-----------|---------|-----------|
| **Framework** | React | 18.3.1 | UI components |
| **Lenguaje** | TypeScript | 5.4.5 | Type safety |
| **Build Tool** | Vite | 5.3.1 | Dev server + bundler |
| **Estado** | Zustand | 4.5.2 | State management |
| **Testing** | Vitest | 1.6.0 | Unit tests |
| **Testing** | fast-check | 3.19.0 | Property-based testing |
| **Linting** | ESLint | 8.57.0 | Code quality |
| **Formatting** | Prettier | 3.3.2 | Code formatting |

---

## 📝 Licencia

Este proyecto es parte de un examen académico para la asignatura **Desarrollo Avanzado de Aplicaciones en Red** - Ingeniería de Software.

---

## 👨‍💻 Autor

**Estudiante**: [Tu Nombre]  
**Código**: [Tu Código]  
**Programa**: Ingeniería de Software  
**Docente**: Jose Dario Paez Perez  
**Fecha**: 02-Mayo-2026

---

## 🔗 Enlaces Útiles

- [Repositorio GitHub](https://github.com/tu-usuario/proyectPOSAWS)
- [Documentación React](https://react.dev/)
- [Documentación Zustand](https://zustand-demo.pmnd.rs/)
- [AWS SAM Documentation](https://docs.aws.amazon.com/serverless-application-model/)
