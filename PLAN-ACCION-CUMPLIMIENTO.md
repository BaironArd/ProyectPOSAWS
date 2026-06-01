# Plan de Acción para Cumplimiento 100% con Requisitos del Examen

## Fecha: 1 de junio de 2026
## Objetivo: Alcanzar 100% de cumplimiento con ambos exámenes

---

## FASE 1: Reorganización de Estructura (COMPLETADO ✅)

### Tarea 1.1: Mover specs del frontend
- [x] Crear `.kiro/specs/pos-frontend/`
- [x] Copiar requirements.md, design.md, tasks.md
- [x] Verificar integridad de archivos

**Resultado:** Estructura ahora cumple con requisitos del examen

---

## FASE 2: Documentación README (PRIORIDAD ALTA)

### Tarea 2.1: Crear README del Backend

**Ubicación:** `aws-microservices/README.md` o `PROYECTPOS/backend/pos-backend/README.md`

**Contenido requerido:**

```markdown
# Backend POS — AWS SAM Serverless

## Arquitectura del Sistema

[Descripción de arquitectura serverless: API Gateway + Lambda + DynamoDB]

## Framework y Tecnologías

- **AWS SAM** (Serverless Application Model)
- **Java 21** para funciones Lambda
- **DynamoDB** como base de datos NoSQL
- **API Gateway** para exposición de endpoints REST

### Justificación de AWS SAM

[Copiar de design.md sección 1.1 "Why AWS SAM Instead of Spring Boot?"]

## Endpoints Disponibles

### Productos (GetProductsFunction)
- GET /api/v1/products
- GET /api/v1/products?type=id&q={id}
- GET /api/v1/products?type=code&q={code}
- GET /api/v1/products?type=name&q={name}
- GET /api/v1/products/{id}

### Ventas (SaveSaleFunction)
- POST /api/v1/sales
- GET /api/v1/sales
- GET /api/v1/sales/{id}
- GET /api/v1/reports/daily?date=YYYY-MM-DD
- GET /api/v1/reports/summary
- GET /api/v1/reports/top-products

## Instrucciones de Despliegue

### Prerrequisitos
- Java 21 SDK
- AWS SAM CLI (`sam --version`)
- AWS CLI configurado (`aws configure`)
- Región: us-east-1

### Paso 1: Build
```bash
cd aws-microservices
sam build
```

### Paso 2: Deploy
```bash
sam deploy --guided
```

Prompts:
- Stack name: `pos-sam`
- Region: `us-east-1`
- Confirm deployments: Y
- Allow IAM role creation: Y

### Paso 3: Verificar
```bash
aws cloudformation describe-stacks --stack-name pos-sam
```

## URL del API Gateway Desplegado

```
https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod
```

## Pruebas Unitarias

### Ejecutar tests
```bash
cd productos-service
mvn test

cd ../ventas-service
mvn test
```

### Cobertura
- Target: ≥70%
- Mocks: Mockito para DynamoDB

## Capturas de Pantalla

### Postman - GET /productos (éxito)
![GET productos](./docs/screenshots/postman-get-productos.png)

### Postman - POST /ventas (éxito)
![POST ventas](./docs/screenshots/postman-post-ventas.png)

### Postman - Error 400 (items vacío)
![Error 400](./docs/screenshots/postman-error-400.png)

### Pruebas Unitarias
![Tests](./docs/screenshots/unit-tests.png)

## Proceso SDD (Spec-Driven Development)

### 1. Specs Primero
Antes de escribir código, creamos:
- `requirements.md`: 14 endpoints con acceptance criteria
- `design.md`: ADRs, DynamoDB schema, Lambda design
- `tasks.md`: 40+ tareas de implementación

### 2. Implementación Guiada
Cada función Lambda se implementó siguiendo:
- Contrato de endpoint en requirements.md
- Estructura de código en design.md
- Orden de tareas en tasks.md

### 3. Trazabilidad
- Task 2.1 → GetProductsHandler.java
- Task 3.1 → SaveSaleHandler.java
- Task 4.1 → ProductosTable en template.yaml

### 4. Validación
- Acceptance criteria → casos de prueba
- Error codes en requirements → manejo en código
- Data structures en design → DTOs en Java

## Estructura del Repositorio

```
aws-microservices/
├── .kiro/specs/pos-backend/
│   ├── requirements.md
│   ├── design.md
│   └── tasks.md
├── productos-service/
│   ├── src/main/java/com/pos/productos/
│   │   ├── handler/ProductosHandler.java
│   │   ├── service/ProductService.java
│   │   ├── repository/ProductRepository.java
│   │   └── model/
│   ├── src/test/java/
│   └── pom.xml
├── ventas-service/
│   ├── src/main/java/com/pos/ventas/
│   ├── src/test/java/
│   └── pom.xml
├── template.yaml
├── samconfig.toml
└── README.md
```

## Licencia
MIT
```

**Acción:** Crear este README en `aws-microservices/README.md`

---

### Tarea 2.2: Crear README del Frontend

**Ubicación:** `PROYECTPOS/frontend/pos-frontend/README.md`

**Contenido requerido:**

```markdown
# Frontend POS — React + TypeScript

## Arquitectura Cliente-Servidor

El frontend es una **Single Page Application (SPA)** construida con React 18 que consume el backend serverless AWS SAM a través de API Gateway.

```
┌─────────────┐         HTTPS          ┌──────────────┐
│   Browser   │ ◄──────────────────► │ API Gateway  │
│  (React)    │    JSON REST API      │              │
└─────────────┘                        └──────────────┘
                                              │
                                              ▼
                                       ┌──────────────┐
                                       │   Lambda     │
                                       │  Functions   │
                                       └──────────────┘
                                              │
                                              ▼
                                       ┌──────────────┐
                                       │  DynamoDB    │
                                       └──────────────┘
```

## Framework Elegido: React 18 + TypeScript 5

### Justificación

| Criterio | Ventaja |
|----------|---------|
| **Type Safety** | TypeScript previene errores en tiempo de compilación |
| **Hooks** | useState, useEffect permiten lógica reutilizable |
| **Ecosystem** | Zustand para state, Vitest para testing |
| **Performance** | Virtual DOM optimiza renderizado |
| **Developer Experience** | Hot reload, debugging tools |

**Alternativas consideradas:**
- Vue 3: Menos experiencia del equipo
- Angular: Overhead excesivo para POS
- Vanilla JS: Falta de estructura para app compleja

**Decisión:** React con arquitectura hexagonal para desacoplar dominio de infraestructura.

## Arquitectura Hexagonal (Ports & Adapters)

```
┌─────────────────────────────────────────────┐
│           UI LAYER (React)                  │
│  SearchBar, Cart, PaymentPanel, etc.        │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│      APPLICATION LAYER (Zustand)            │
│  usePOSStore + hooks (useSearch, useCart)   │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│         DOMAIN LAYER (Pure TS)              │
│  types/, ports/, calculadora.ts             │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│    INFRASTRUCTURE LAYER (Adapters)          │
│  ProductoAdapter, VentaAdapter, mocks/      │
└─────────────────────────────────────────────┘
```

## Funcionalidades

### Vista de Productos
- Búsqueda por nombre o código
- Listado con precio y stock
- Agregar al carrito

### Flujo de Ventas
- Carrito con modificación de cantidades
- Cálculo automático de IVA (19%)
- Múltiples métodos de pago (efectivo, tarjeta, transferencia, mixto)
- Confirmación de venta

### Manejo de Respuestas
- ✅ Mensaje de éxito: "Venta completada! Cambio: $X.XXX"
- ❌ Mensaje de error: "No se pudo procesar la venta. Intente nuevamente."

## Configuración del API Gateway

### Archivo: `.env`

```env
VITE_API_BASE_URL=https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod
```

**Importante:** No subir `.env` a Git. Usar `.env.example` como plantilla.

### Archivo: `.env.example`

```env
VITE_API_BASE_URL=https://your-api-gateway-url.amazonaws.com/Prod
```

## Instrucciones de Ejecución

### Prerrequisitos
- Node.js 18+
- npm 9+

### Paso 1: Instalar dependencias
```bash
cd PROYECTPOS/frontend/pos-frontend
npm install
```

### Paso 2: Configurar API Gateway
```bash
cp .env.example .env
# Editar .env con la URL real del API Gateway
```

### Paso 3: Ejecutar en desarrollo
```bash
npm run dev
```

Abrir: http://localhost:5173

### Paso 4: Build para producción
```bash
npm run build
```

Salida: `dist/`

## Capturas de Pantalla

### Listado de Productos
![Productos](./docs/screenshots/productos-list.png)

### Registro de Venta Exitosa
![Venta exitosa](./docs/screenshots/venta-exitosa.png)

### Manejo de Error (API caído)
![Error](./docs/screenshots/error-api.png)

## Proceso SDD (Spec-Driven Development)

### 1. Specs Primero
Antes de escribir código, creamos:
- `requirements.md`: 19 requisitos funcionales con acceptance criteria
- `design.md`: State machine, componentes, ports
- `tasks.md`: 18 fases de implementación

### 2. Implementación Guiada
Cada componente se implementó siguiendo:
- Requisito funcional en requirements.md
- Diseño de componente en design.md
- Tarea específica en tasks.md

### 3. Trazabilidad

| Requisito | Componente | Tarea |
|-----------|------------|-------|
| SPEC-001 (Product Search) | SearchBar.tsx | Task 5.1 |
| SPEC-002 (Add to Cart) | Cart.tsx | Task 6.2 |
| SPEC-006 (Sale Confirmation) | PaymentPanel.tsx | Task 9.1 |

### 4. Validación
- Acceptance criteria → casos de prueba con Vitest
- State machine → property-based testing con fast-check
- Calculator → 100+ casos generados aleatoriamente

## Fundamentos Demostrados

### HTML5 Semántico
```tsx
<header>
  <nav>
    <button>Productos</button>
  </nav>
</header>
<main>
  <section aria-label="Búsqueda">
    <input type="search" />
  </section>
  <article>
    <h2>Carrito</h2>
  </article>
</main>
```

### CSS (Flexbox y Grid)
```css
.cart-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1rem;
}

.payment-panel {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
```

### JavaScript (Async/Await y Fetch)
```typescript
export class ProductoAdapter implements IProductoPort {
  async buscar(query: string): Promise<Producto[]> {
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/v1/products?q=${query}`
      );
      if (!response.ok) throw new Error('Network error');
      return await response.json();
    } catch (error) {
      console.error('Error fetching products:', error);
      throw error;
    }
  }
}
```

## Estructura del Repositorio

```
pos-frontend/
├── .kiro/specs/pos-frontend/
│   ├── requirements.md
│   ├── design.md
│   └── tasks.md
├── src/
│   ├── domain/
│   │   ├── types/POSState.ts
│   │   ├── ports/ (8 interfaces)
│   │   └── calculadora.ts
│   ├── application/
│   │   ├── store/usePOSStore.ts
│   │   └── hooks/
│   ├── infrastructure/
│   │   ├── adapters/ (8 adaptadores)
│   │   └── mocks/
│   └── ui/
│       ├── components/ (13 componentes)
│       └── POSApp.tsx
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── .env.example
├── .gitignore
└── README.md
```

## Licencia
MIT
```

**Acción:** Crear este README en `PROYECTPOS/frontend/pos-frontend/README.md`

---

## FASE 3: Capturas de Pantalla (PRIORIDAD ALTA)

### Tarea 3.1: Crear carpeta de screenshots

```bash
mkdir -p aws-microservices/docs/screenshots
mkdir -p PROYECTPOS/frontend/pos-frontend/docs/screenshots
```

### Tarea 3.2: Capturas del Backend (Postman)

**Requeridas:**

1. **GET /api/v1/products** (éxito 200)
   - Archivo: `postman-get-productos.png`
   - Mostrar: Request, Response body con array de productos, Status 200

2. **POST /api/v1/sales** (éxito 201)
   - Archivo: `postman-post-ventas.png`
   - Mostrar: Request body con items, Response con ventaId, Status 201

3. **POST /api/v1/sales** (error 400 - items vacío)
   - Archivo: `postman-error-400.png`
   - Mostrar: Request body con items:[], Response con error, Status 400

4. **GET /api/v1/products?type=name&q=mouse** (búsqueda)
   - Archivo: `postman-search-productos.png`
   - Mostrar: Query params, Response filtrado

5. **Pruebas unitarias ejecutándose**
   - Archivo: `unit-tests.png`
   - Mostrar: Terminal con `mvn test` exitoso, coverage report

### Tarea 3.3: Capturas del Frontend

**Requeridas:**

1. **Listado de productos cargado desde el API**
   - Archivo: `productos-list.png`
   - Mostrar: SearchBar, lista de productos con precios

2. **Registro de una venta exitosa con respuesta del API visible**
   - Archivo: `venta-exitosa.png`
   - Mostrar: Carrito, resumen con IVA, mensaje "Venta completada"

3. **Manejo de un error (API caído o respuesta inválida)**
   - Archivo: `error-api.png`
   - Mostrar: ErrorBanner con mensaje de error

**Instrucciones para capturar:**
- Usar herramienta de captura de pantalla (Snipping Tool, Lightshot, etc.)
- Resolución mínima: 1280x720
- Formato: PNG
- Incluir URL del navegador en la captura

---

## FASE 4: Verificación de Implementación (PRIORIDAD MEDIA)

### Tarea 4.1: Verificar fundamentos HTML5 en frontend

**Checklist:**
- [ ] Componentes usan etiquetas semánticas (`<header>`, `<nav>`, `<main>`, `<section>`, `<article>`)
- [ ] No todo es `<div>`
- [ ] Inputs tienen `type` apropiado (`type="search"`, `type="number"`)
- [ ] Botones usan `<button>` no `<div onclick>`

**Acción:** Revisar archivos en `src/ui/components/`

### Tarea 4.2: Verificar CSS con Flexbox/Grid

**Checklist:**
- [ ] Al menos 2 componentes usan `display: flex`
- [ ] Al menos 1 componente usa `display: grid`
- [ ] Estilos propios (no solo clases de framework)
- [ ] Box model aplicado correctamente (margin, padding, border)

**Acción:** Revisar archivos `.css` o styled-components

### Tarea 4.3: Verificar manejo de errores con try/catch

**Checklist:**
- [ ] Todos los adapters tienen try/catch en métodos async
- [ ] Errores se propagan con throw
- [ ] Errores se muestran en ErrorBanner

**Acción:** Revisar archivos en `src/infrastructure/adapters/`

---

## FASE 5: Actualización de README Raíz (PRIORIDAD MEDIA)

### Tarea 5.1: Actualizar README.md raíz

**Ubicación:** `README.md` (raíz del proyecto)

**Secciones a agregar:**

```markdown
# Sistema POS — Proyecto Final

## Descripción General

Sistema de Punto de Venta (POS) completo con:
- **Backend serverless** en AWS SAM (Java 21 + Lambda + DynamoDB)
- **Frontend web** en React 18 + TypeScript 5

## Arquitectura

```
┌──────────────┐         HTTPS          ┌──────────────┐
│   Frontend   │ ◄──────────────────► │ API Gateway  │
│  (React 18)  │    JSON REST API      │              │
└──────────────┘                        └──────────────┘
                                              │
                                              ▼
                                       ┌──────────────┐
                                       │   Lambda     │
                                       │  (Java 21)   │
                                       └──────────────┘
                                              │
                                              ▼
                                       ┌──────────────┐
                                       │  DynamoDB    │
                                       └──────────────┘
```

## Estructura del Proyecto

```
proyectPOSAWS/
├── .kiro/specs/
│   ├── pos-backend/      # Specs del backend
│   └── pos-frontend/     # Specs del frontend
├── aws-microservices/    # Backend AWS SAM
│   ├── productos-service/
│   ├── ventas-service/
│   └── template.yaml
├── PROYECTPOS/frontend/  # Frontend React
│   └── pos-frontend/
└── README.md
```

## Documentación

- [Backend README](./aws-microservices/README.md)
- [Frontend README](./PROYECTPOS/frontend/pos-frontend/README.md)
- [Backend Specs](./kiro/specs/pos-backend/)
- [Frontend Specs](./.kiro/specs/pos-frontend/)

## Proceso SDD (Spec-Driven Development)

Este proyecto sigue el enfoque **Spec-Driven Development**:

1. **Specs primero**: requirements.md, design.md, tasks.md antes de código
2. **Implementación guiada**: cada línea de código trazable a un spec
3. **Validación continua**: acceptance criteria → casos de prueba

### Evidencia SDD

- ✅ Specs en `.kiro/specs/` con timestamps anteriores al código
- ✅ Commits muestran "spec → implementación" en orden
- ✅ Trazabilidad: Task ID → archivo de código

## URL del API Gateway Desplegado

```
https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod
```

## Licencia
MIT
```

---

## FASE 6: Validación Final (PRIORIDAD BAJA)

### Tarea 6.1: Checklist de entrega

**Backend:**
- [ ] `.kiro/specs/pos-backend/` con requirements.md, design.md, tasks.md
- [ ] `template.yaml` presente
- [ ] Lambdas implementadas (productos-service, ventas-service)
- [ ] Pruebas unitarias con mocks
- [ ] README con arquitectura, instrucciones, capturas, SDD
- [ ] 5+ capturas de Postman
- [ ] 1+ captura de tests

**Frontend:**
- [ ] `.kiro/specs/pos-frontend/` con requirements.md, design.md, tasks.md
- [ ] Componentes React implementados
- [ ] Consumo de API con fetch/axios
- [ ] README con arquitectura, framework justificado, capturas, SDD
- [ ] 3+ capturas de funcionalidad
- [ ] HTML5 semántico verificado
- [ ] CSS con flexbox/grid verificado
- [ ] Manejo de errores con try/catch verificado

**General:**
- [ ] README raíz actualizado
- [ ] `.gitignore` correcto (no subir node_modules, .env, target/)
- [ ] Repositorio en GitHub público
- [ ] Commits con mensajes descriptivos

### Tarea 6.2: Revisión de calidad

**Criterios:**
- [ ] Todos los specs tienen fecha anterior al código
- [ ] No hay código comentado sin explicación
- [ ] No hay TODOs sin resolver
- [ ] No hay console.log en producción
- [ ] No hay credenciales hardcodeadas

---

## Cronograma Sugerido

| Fase | Tiempo Estimado | Prioridad |
|------|-----------------|-----------|
| Fase 1: Reorganización | ✅ Completado | ALTA |
| Fase 2: README Backend | 2 horas | ALTA |
| Fase 2: README Frontend | 2 horas | ALTA |
| Fase 3: Capturas Backend | 1 hora | ALTA |
| Fase 3: Capturas Frontend | 1 hora | ALTA |
| Fase 4: Verificar fundamentos | 2 horas | MEDIA |
| Fase 5: README raíz | 1 hora | MEDIA |
| Fase 6: Validación final | 1 hora | BAJA |
| **TOTAL** | **10 horas** | |

---

## Próximos Pasos Inmediatos

1. ✅ Crear `aws-microservices/README.md` (Tarea 2.1)
2. ✅ Crear `PROYECTPOS/frontend/pos-frontend/README.md` (Tarea 2.2)
3. ⏳ Capturar screenshots de Postman (Tarea 3.2)
4. ⏳ Capturar screenshots del frontend (Tarea 3.3)
5. ⏳ Actualizar README raíz (Tarea 5.1)

---

**Fecha de entrega objetivo:** [Definir según calendario académico]
**Responsable:** Equipo de desarrollo
