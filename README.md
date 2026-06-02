# ProyectPOS — Sistema de Punto de Venta

Sistema completo de punto de venta (POS) con arquitectura **serverless en AWS**, desarrollado con **React 18 + TypeScript** en el frontend y **Java 21 + AWS Lambda + DynamoDB** en el backend.

**Desarrollado siguiendo Spec-Driven Development (SDD)** — Los specs se escriben antes del código.

---

## Tabla de contenidos

- [Vista general](#vista-general)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Documentación](#documentación)
- [Quick Start](#quick-start)
- [Backend — AWS SAM](#backend--aws-sam)
- [Frontend — React](#frontend--react)
- [Testing](#testing)
- [Proceso SDD](#proceso-sdd)
- [URL del API Gateway Desplegado](#url-del-api-gateway-desplegado)
- [Stack tecnológico](#stack-tecnológico)

---

## Vista general

ProyectPOS es un sistema de punto de venta diseñado para cajeros. Permite gestionar el ciclo completo de ventas: búsqueda de productos, construcción de carrito, procesamiento de pagos en efectivo, confirmación de ventas y registro en DynamoDB.

### Capturas de pantalla

*Vista principal del POS — búsqueda y carrito*
<img width="1868" height="946" alt="image" src="https://github.com/user-attachments/assets/6d585d84-e7d9-4b6d-810a-88704864b2a4" />

---

## Arquitectura

El sistema sigue una arquitectura **cliente-servidor serverless**:

```
┌──────────────┐         HTTPS          ┌──────────────┐
│   Frontend   │ ◄──────────────────► │ API Gateway  │
│  (React 18)  │    JSON REST API      │              │
│              │                        │  /products   │
│  - Search    │                        │  /sales      │
│  - Cart      │                        │  /reports    │
│  - Payment   │                        │              │
└──────────────┘                        └──────────────┘
                                              │
                                              ▼
                                       ┌──────────────┐
                                       │   Lambda     │
                                       │  Functions   │
                                       │  (Java 21)   │
                                       └──────────────┘
                                              │
                                    ┌─────────┴─────────┐
                                    ▼                   ▼
                             ┌─────────────┐   ┌─────────────┐
                             │ Productos   │   │   Ventas    │
                             │   Table     │   │    Table    │
                             │ (DynamoDB)  │   │ (DynamoDB)  │
                             └─────────────┘   └─────────────┘
```

### Principios de diseño

- **Serverless**: Sin servidores que administrar — Lambda escala automáticamente
- **Separación de responsabilidades**: Handler (HTTP) → Service (lógica) → Repository (DynamoDB)
- **Spec-Driven Development**: Cada decisión de implementación parte de un spec escrito
- **Testabilidad**: La capa Service no sabe nada de AWS — se mockea fácilmente
- **Arquitectura Hexagonal (Frontend)**: Dominio desacoplado de infraestructura mediante ports y adapters

---

## Estructura del proyecto

```
proyectPOSAWS/
│
├── .kiro/specs/               ← Especificaciones SDD
│   ├── pos-backend/           ← Specs del backend SAM
│   │   ├── requirements.md    ← 14 endpoints documentados
│   │   ├── design.md          ← ADRs, DynamoDB schema, Lambda design
│   │   └── tasks.md           ← 40+ tareas de implementación
│   ├── pos-frontend/          ← Specs del frontend React
│   │   ├── requirements.md    ← 19 requisitos funcionales
│   │   ├── design.md          ← State machine, componentes, ports
│   │   └── tasks.md           ← 18 fases de implementación
│   └── pos-bugs-fix/          ← Specs de correcciones
│
├── aws-microservices/         ← Backend SAM simplificado (2 endpoints)
│   ├── README.md              ← Documentación completa del backend
│   ├── template.yaml          ← Define API Gateway + Lambdas + DynamoDB
│   ├── samconfig.toml
│   ├── productos-service/     ← Lambda GET /productos
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/java/com/pos/aws/productos/
│   │       │   ├── handler/   ← ProductosHandler
│   │       │   ├── service/   ← ProductoService (lógica)
│   │       │   ├── repository/← ProductoRepository (DynamoDB)
│   │       │   └── model/
│   │       └── test/          ← ProductoServiceTest (9 tests con mocks)
│   └── ventas-service/        ← Lambda POST /ventas
│       ├── pom.xml
│       └── src/
│           ├── main/java/com/pos/aws/ventas/
│           │   ├── handler/   ← VentasHandler
│           │   ├── service/   ← VentaService (lógica + IVA 19%)
│           │   ├── repository/← VentaRepository (DynamoDB)
│           │   └── model/
│           └── test/          ← VentaServiceTest (tests con mocks)
│
├── PROYECTPOS/
│   ├── backend/
│   │   └── pos-backend/       ← Backend SAM completo (14 endpoints - versión principal)
│   │       ├── template.yaml
│   │       ├── get-products/  ← Lambda GetProductsFunction
│   │       │   └── src/test/  ← GetProductsHandlerTest + ProductServiceTest (12 tests)
│   │       └── save-sale/     ← Lambda SaveSaleFunction
│   │           └── src/test/  ← SaveSaleHandlerTest + SaleServiceTest (tests con mocks)
│   └── frontend/
│       └── pos-frontend/      ← Aplicación React
│           ├── README.md      ← Documentación completa del frontend
│           ├── src/
│           │   ├── domain/    ← Tipos, puertos, calculadora
│           │   ├── application/← Store Zustand + hooks
│           │   ├── infrastructure/← Adaptadores HTTP
│           │   └── ui/        ← Componentes React
│           └── package.json
│
├── docs/                      ← Documentación general
│   ├── DEVELOPMENT.md
│   └── TROUBLESHOOTING.md
│
├── ANALISIS-CUMPLIMIENTO-EXAMEN.md  ← Verificación de requisitos del examen
├── PLAN-ACCION-CUMPLIMIENTO.md     ← Plan para completar requisitos
└── README.md                        ← Este archivo
```

---

## Documentación

### Backend
- **[Backend README completo](./aws-microservices/README.md)** — Arquitectura, endpoints, despliegue, testing
- **[Backend Specs](./kiro/specs/pos-backend/)** — Requirements, design, tasks

### Frontend
- **[Frontend README completo](./PROYECTPOS/frontend/pos-frontend/README.md)** — Arquitectura hexagonal, componentes, testing
- **[Frontend Specs](./.kiro/specs/pos-frontend/)** — Requirements, design, tasks

### General
- **[Análisis de Cumplimiento](./ANALISIS-CUMPLIMIENTO-EXAMEN.md)** — Verificación de requisitos del examen
- **[Plan de Acción](./PLAN-ACCION-CUMPLIMIENTO.md)** — Tareas pendientes para cumplimiento 100%

---

## Quick Start

### Frontend (desarrollo local)

```bash
cd PROYECTPOS/frontend/pos-frontend
npm install
cp .env.example .env
# Editar .env con la URL del API Gateway
npm run dev
# → http://localhost:5173
```

### Backend (despliegue AWS)

```bash
cd aws-microservices
sam build
sam deploy --guided   # Primera vez
sam deploy            # Despliegues siguientes
```

---

## Backend — AWS SAM

### Endpoints

**Nota:** `aws-microservices/` tiene una implementación simplificada con 2 endpoints funcionales. Para la versión completa con 14 endpoints, ver `PROYECTPOS/backend/pos-backend/`.

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/productos?tipo=todos` | Listar todos los productos |
| `GET` | `/productos?tipo=nombre&q={query}` | Buscar por nombre |
| `GET` | `/productos?tipo=id&q={uuid}` | Buscar por ID |
| `POST` | `/ventas` | Registrar una venta |

### Body POST /ventas

```json
{
  "cajero": "juan.perez",
  "metodoPago": "EFECTIVO",
  "montoPagado": 500000,
  "items": [
    {
      "productoId": "prod-001",
      "nombre": "Mouse Inalámbrico",
      "cantidad": 2,
      "precioUnitario": 45000
    }
  ]
}
```

### Respuesta exitosa

```json
{
  "success": true,
  "mensaje": "Venta registrada exitosamente",
  "data": {
    "ventaId": "uuid-generado",
    "subtotal": 90000,
    "iva": 17100,
    "total": 107100,
    "cambio": 392900,
    "estado": "COMPLETADA"
  }
}
```

### Tablas DynamoDB

**Productos-{entorno}**

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| `id` | String | Clave primaria (PK) |
| `codigoBarras` | String | GSI búsqueda |
| `codigo` | String | GSI búsqueda |
| `nombre` | String | Nombre del producto |
| `precio` | Number | Precio en centavos |
| `stock` | Number | Cantidad disponible |

**Ventas-{entorno}**

| Atributo | Tipo | Descripción |
|----------|------|-------------|
| `ventaId` | String | Clave primaria (UUID) |
| `cajero` | String | Usuario cajero |
| `metodoPago` | String | EFECTIVO / TARJETA |
| `subtotal` | Number | Subtotal sin IVA |
| `iva` | Number | IVA 19% |
| `total` | Number | Total con IVA |
| `cambio` | Number | Cambio devuelto |
| `estado` | String | COMPLETADA |

### Comandos

```bash
# Compilar
cd aws-microservices/productos-service && mvn clean package -DskipTests
cd aws-microservices/ventas-service    && mvn clean package -DskipTests

# Tests
cd aws-microservices/productos-service && mvn test
cd aws-microservices/ventas-service    && mvn test

# Desplegar
cd aws-microservices
sam build
sam deploy

# Prueba local
sam local start-api
curl "http://localhost:3000/productos?tipo=todos"
```

---

## Frontend — React

Ver documentación completa en [`PROYECTPOS/frontend/pos-frontend/README.md`](PROYECTPOS/frontend/pos-frontend/README.md).

### Instalación rápida

```bash
cd PROYECTPOS/frontend/pos-frontend
npm install
npm run dev
```

### Configurar API Gateway

```bash
# .env
VITE_API_BASE_URL=https://tu-api-id.execute-api.us-east-1.amazonaws.com/Prod
```

---

## Testing

### Resumen de cobertura

| Módulo | Tests | Cobertura |
|--------|-------|-----------|
| `aws-microservices/productos-service` | 9 tests | Service: búsqueda por nombre, id, código, todos, errores |
| `aws-microservices/ventas-service` | Tests | Service: IVA, validaciones, cambio exacto, errores |
| `pos-backend/get-products` | 12 tests | Service: búsqueda por tipo, validaciones, errores |
| `pos-backend/save-sale` | Tests | Handler + Service con mocks Mockito |

### Ejecutar todos los tests

```bash
# aws-microservices
cd aws-microservices/productos-service && mvn test
cd aws-microservices/ventas-service    && mvn test

# pos-backend
cd PROYECTPOS/backend/pos-backend/get-products && mvn test
cd PROYECTPOS/backend/pos-backend/save-sale    && mvn test

# Frontend
cd PROYECTPOS/frontend/pos-frontend && npm run test
```

### Estrategia de testing

Los tests aíslan DynamoDB mediante **mocks de Mockito**:

```java
@Mock
private ProductoRepository repository;

@Test
void buscarPorNombre_retornaProductos() {
    when(repository.buscarPorNombre("mouse")).thenReturn(List.of(productoEjemplo()));

    List<ProductoResponse> resultado = service.buscar("nombre", "mouse");

    assertEquals(1, resultado.size());
    verify(repository).buscarPorNombre("mouse");
}
```

Casos cubiertos por módulo:
- **Respuesta exitosa** — datos correctos retornados
- **Tabla vacía** — lista vacía sin errores
- **Error de conexión** — RuntimeException propagada
- **Validaciones** — items vacío, pago insuficiente, IDs inválidos

---

## Proceso SDD

Este proyecto sigue **Spec-Driven Development**: los specs se escriben antes del código.

### Estructura de Specs

```
.kiro/specs/
├── pos-backend/
│   ├── requirements.md  ← QUÉ debe hacer cada endpoint (14 endpoints)
│   ├── design.md        ← CÓMO: tablas DynamoDB, contratos request/response, ADRs
│   └── tasks.md         ← ORDEN de implementación (40+ tareas)
└── pos-frontend/
    ├── requirements.md  ← QUÉ debe hacer cada componente (19 requisitos)
    ├── design.md        ← CÓMO: state machine, arquitectura hexagonal, ports
    └── tasks.md         ← ORDEN de implementación (18 fases)
```

### Flujo SDD

1. **Specs Primero**
   - Se escriben requirements.md, design.md y tasks.md
   - Se revisan y aprueban antes de codificar
   - Timestamps de commits muestran specs antes que código

2. **Implementación Guiada**
   - Cada línea de código es trazable a un spec
   - Ejemplo: Task 2.1 → GetProductsHandler.java
   - Ejemplo: SPEC-001 (Product Search) → SearchBar.tsx

3. **Validación**
   - Acceptance criteria → casos de prueba
   - Error codes en requirements → manejo en código
   - Data structures en design → DTOs en Java/TypeScript

4. **Actualización Continua**
   - Si durante la implementación se descubre algo no especificado, se actualiza el spec primero
   - Los specs son documentación viva

### Evidencia SDD

- ✅ Specs en `.kiro/specs/` con timestamps anteriores al código
- ✅ Commits muestran "spec → implementación" en orden
- ✅ Trazabilidad: Task ID → archivo de código
- ✅ Acceptance criteria → casos de prueba verificables

### Beneficios del SDD

- **Claridad:** Todos saben qué construir antes de empezar
- **Trazabilidad:** Cada línea de código tiene un "por qué" documentado
- **Testabilidad:** Acceptance criteria son casos de prueba
- **Documentación:** Specs son documentación viva
- **Colaboración:** Equipo revisa specs antes de implementar
- **Calidad:** Menos bugs porque el diseño se valida antes de codificar

---

## URL del API Gateway Desplegado

```
https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod
```

**Ejemplo de uso:**
```bash
# Listar productos
curl https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod/api/v1/products

# Buscar por nombre
curl "https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod/api/v1/products?type=name&q=mouse"

# Crear venta
curl -X POST https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod/api/v1/sales \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":"...","quantity":2,"priceAtSale":45000}],"paymentMethod":"CASH","amountPaid":90000}'
```

---

## Stack tecnológico

### Backend
| Herramienta | Versión | Propósito |
|-------------|---------|-----------|
| Java | 21 LTS | Runtime |
| AWS Lambda | — | Funciones serverless |
| AWS API Gateway | — | Exposición de endpoints REST |
| AWS DynamoDB | — | Base de datos NoSQL |
| AWS SAM | — | Infraestructura como código |
| Jackson | 2.17 | Serialización JSON |
| JUnit 5 | 5.10 | Tests unitarios |
| Mockito | 5.11 | Mocks para DynamoDB |

### Frontend
| Herramienta | Versión | Propósito |
|-------------|---------|-----------|
| React | 18.x | Framework de UI |
| TypeScript | 5.x | Tipado estático |
| Vite | 5.x | Build tool |
| Zustand | 4.x | Estado global |
| Vitest | 1.x | Tests unitarios |
| fast-check | 3.x | Property-Based Testing |

---

## Licencia

MIT — Proyecto académico / de demostración.
