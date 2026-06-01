# Backend Requirements — AWS SAM Serverless POS
**Version:** 1.0  
**Approach:** Spec-Driven Development (SDD)  
**Technology:** Java 21 · AWS Lambda · DynamoDB · SAM  
**Date:** 2024-2025  

---

## 1. Overview

El backend de este POS es una **arquitectura serverless** usando AWS SAM (Serverless Application Model) que expone **14 endpoints REST** a través de API Gateway. La lógica de negocio se implementa en **dos funciones Lambda (Java 21)** que interactúan con **dos tablas DynamoDB**.

**Ventajas elegidas:**
- Zero server management (AWS maneja infraestructura)
- Escalabilidad automática (paga por invocaciones, no por instancias)
- Despliegue declarativo con SAM template.yaml
- IAM roles granulares por Lambda

---

## 2. System Responsibilities

### Endpoint 1-5: GetProductsFunction
| # | Método | Path | Descripción |
|---|--------|------|-------------|
| 1 | GET | `/api/v1/products` | Obtener **todos** los productos |
| 2 | GET | `/api/v1/products?type=id&q={id}` | Buscar por **ID (UUID)** |
| 3 | GET | `/api/v1/products?type=code&q={code}` | Buscar por **código (ej: PERI-001)** |
| 4 | GET | `/api/v1/products?type=name&q={name}` | Buscar por **nombre (partial match)** |
| 5 | GET | `/api/v1/products/{id}` | Obtener **un producto por ID** |

### Endpoint 6-13: SaveSaleFunction
| # | Método | Path | Descripción |
|---|--------|------|-------------|
| 6 | POST | `/api/v1/sales` | **Crear venta** (registra items + pago) |
| 7 | GET | `/api/v1/sales` | **Listar todas** las ventas |
| 8 | GET | `/api/v1/sales/{id}` | **Obtener una venta** por ID |
| 9 | POST | `/api/v1/payments` | **Registrar pago** (efectivo, tarjeta, mixto) |
| 10 | GET | `/api/v1/payments?method=CASH` | **Listar ventas** por método pago |
| 11 | GET | `/api/v1/reports/daily?date=2024-01-15` | **Reporte de ventas** del día |
| 12 | GET | `/api/v1/reports/summary` | **Resumen financiero** (totales, IVA) |
| 13 | GET | `/api/v1/reports/top-products` | **Top 10 productos** vendidos |

### Endpoint 14: CORS Preflight
| # | Método | Path | Descripción |
|---|--------|------|-------------|
| 14 | OPTIONS | `/api/v1/*` | Responder a CORS preflight del navegador |

---

## 3. Detailed Specs — GetProductsFunction

### GET /api/v1/products (Endpoint 1)
**Obtiene todos los productos de la tabla ProductosTable**

#### Request
```
GET /api/v1/products HTTP/1.1
Host: {API_GATEWAY_BASE}
```

#### Precondition
- ProductosTable debe existir en DynamoDB
- Tabla debe tener ≥1 producto

#### Success Response (200 OK)
```json
{
  "products": [
    {
      "id": "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8",
      "code": "PERI-001",
      "name": "Mouse Inalámbrico",
      "price": 45000,
      "stock": 25,
      "lowStockThreshold": 5
    },
    {
      "id": "b2c3d4e5-f6a7-4890-b123-c4d5e6f7a8b9",
      "code": "TECH-002",
      "name": "Teclado Mecánico",
      "price": 85000,
      "stock": 12,
      "lowStockThreshold": 3
    }
  ],
  "count": 2
}
```

#### Error Response (500 Internal Server Error)
```json
{
  "error": "Cannot connect to DynamoDB",
  "code": "INTERNAL_SERVER_ERROR"
}
```

#### Acceptance Criteria
- [ ] Retorna JSON válido con array de productos
- [ ] Cada producto contiene: id, code, name, price, stock, lowStockThreshold
- [ ] count coincide con número de elementos
- [ ] HTTP 200 si operación exitosa
- [ ] HTTP 500 si error de conexión DynamoDB

---

### GET /api/v1/products?type=id&q={id} (Endpoint 2)
**Busca un producto específico por ID (UUID)**

#### Request
```
GET /api/v1/products?type=id&q=a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8 HTTP/1.1
```

#### Precondition
- Parámetro `q` debe ser un UUID válido
- Producto debe existir en tabla

#### Success Response (200 OK)
```json
{
  "product": {
    "id": "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8",
    "code": "PERI-001",
    "name": "Mouse Inalámbrico",
    "price": 45000,
    "stock": 25,
    "lowStockThreshold": 5
  }
}
```

#### Error Response - Producto no encontrado (404)
```json
{
  "error": "Product not found",
  "code": "NOT_FOUND"
}
```

#### Error Response - UUID inválido (400)
```json
{
  "error": "Invalid UUID format",
  "code": "INVALID_INPUT"
}
```

#### Acceptance Criteria
- [ ] Busca exacto en tabla por partition key `id`
- [ ] Retorna HTTP 404 si no encuentra
- [ ] Retorna HTTP 400 si UUID inválido
- [ ] Retorna HTTP 200 + producto si existe

---

### GET /api/v1/products?type=name&q={name} (Endpoint 4)
**Busca productos por nombre (búsqueda parcial, case-insensitive)**

#### Request
```
GET /api/v1/products?type=name&q=mouse HTTP/1.1
```

#### Precondition
- Parámetro `q` debe tener ≥2 caracteres
- Tabla debe tener ≥1 producto coincidente

#### Success Response (200 OK)
```json
{
  "products": [
    {
      "id": "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8",
      "code": "PERI-001",
      "name": "Mouse Inalámbrico",
      "price": 45000,
      "stock": 25,
      "lowStockThreshold": 5
    }
  ],
  "count": 1
}
```

#### Error Response - No coincidencias (200 OK - lista vacía)
```json
{
  "products": [],
  "count": 0
}
```

#### Acceptance Criteria
- [ ] Búsqueda case-insensitive (mouse = MOUSE = Mouse)
- [ ] Búsqueda parcial (mouse coincide con "Mouse Inalámbrico")
- [ ] Retorna lista vacía si no hay coincidencias
- [ ] HTTP 200 siempre

---

## 4. Detailed Specs — SaveSaleFunction

### POST /api/v1/sales (Endpoint 6)
**Crea una nueva venta (registra items del carrito + método de pago)**

#### Request
```json
POST /api/v1/sales HTTP/1.1
Content-Type: application/json

{
  "items": [
    {
      "productId": "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8",
      "quantity": 2,
      "priceAtSale": 45000
    },
    {
      "productId": "b2c3d4e5-f6a7-4890-b123-c4d5e6f7a8b9",
      "quantity": 1,
      "priceAtSale": 85000
    }
  ],
  "paymentMethod": "CASH",
  "amountPaid": 175100,
  "discount": 0
}
```

#### Precondition
- Items array no puede estar vacío
- Todos los productId deben ser UUID válidos
- paymentMethod ∈ {CASH, CARD, TRANSFER, MIXED}
- amountPaid ≥ subtotal

#### Postcondition
- VentasTable contiene nuevo registro con id único
- Venta registrada con timestamp ISO-8601
- Todos los items persistidos

#### Success Response (201 Created)
```json
{
  "id": "s5a6b7c8-d9e0-4f12-a345-b6c7d8e9f0a1",
  "total": 175100,
  "subtotal": 175000,
  "tax": 100,
  "paymentMethod": "CASH",
  "createdAt": "2024-12-15T10:30:45Z",
  "items": [
    {
      "productId": "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8",
      "quantity": 2,
      "priceAtSale": 45000
    },
    {
      "productId": "b2c3d4e5-f6a7-4890-b123-c4d5e6f7a8b9",
      "quantity": 1,
      "priceAtSale": 85000
    }
  ]
}
```

#### Error Response - Items vacío (400 Bad Request)
```json
{
  "error": "Items array cannot be empty",
  "code": "INVALID_INPUT"
}
```

#### Error Response - Monto insuficiente (400)
```json
{
  "error": "Payment amount is less than total",
  "code": "INVALID_PAYMENT"
}
```

#### Error Response - Error DynamoDB (500)
```json
{
  "error": "Failed to save sale to database",
  "code": "INTERNAL_SERVER_ERROR"
}
```

#### Acceptance Criteria
- [ ] Genera ID único (UUID v4) para la venta
- [ ] Calcula total = suma de (quantity × priceAtSale)
- [ ] Calcula tax = total × 0.19 (IVA Colombia)
- [ ] HTTP 201 si creación exitosa
- [ ] HTTP 400 si validación falla
- [ ] HTTP 500 si error de BD

---

### GET /api/v1/sales (Endpoint 7)
**Lista todas las ventas registradas**

#### Request
```
GET /api/v1/sales HTTP/1.1
```

#### Precondition
- VentasTable debe existir
- Puede tener ≥0 ventas

#### Success Response (200 OK)
```json
{
  "sales": [
    {
      "id": "s5a6b7c8-d9e0-4f12-a345-b6c7d8e9f0a1",
      "total": 175100,
      "paymentMethod": "CASH",
      "createdAt": "2024-12-15T10:30:45Z",
      "itemCount": 2
    }
  ],
  "count": 1
}
```

#### Acceptance Criteria
- [ ] Retorna todos los registros de VentasTable
- [ ] HTTP 200 siempre
- [ ] count coincide con número de ventas

---

### GET /api/v1/reports/daily?date=2024-01-15 (Endpoint 11)
**Reporte de ventas de un día específico**

#### Request
```
GET /api/v1/reports/daily?date=2024-12-15 HTTP/1.1
```

#### Precondition
- Parámetro `date` en formato YYYY-MM-DD
- Pueden existir 0 o más ventas ese día

#### Success Response (200 OK)
```json
{
  "date": "2024-12-15",
  "totalSales": 3,
  "revenue": 525300,
  "tax": 100,
  "byPaymentMethod": {
    "CASH": 175100,
    "CARD": 350200
  }
}
```

#### Error Response - Fecha inválida (400)
```json
{
  "error": "Invalid date format. Use YYYY-MM-DD",
  "code": "INVALID_INPUT"
}
```

#### Acceptance Criteria
- [ ] Filtra ventas creadas en esa fecha (por timestamp)
- [ ] Retorna suma total de revenue y tax
- [ ] Agrupa por paymentMethod
- [ ] HTTP 400 si formato de fecha inválido
- [ ] HTTP 200 si éxito

---

## 5. OPTIONS Endpoint (CORS Preflight)

### OPTIONS /api/v1/* (Endpoint 14)
**Responde a solicitudes CORS preflight del navegador**

#### Request (generado automáticamente por navegador)
```
OPTIONS /api/v1/products HTTP/1.1
Origin: http://localhost:5173
Access-Control-Request-Method: GET
Access-Control-Request-Headers: content-type
```

#### Success Response (200 OK)
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Max-Age: 3600
```

#### Acceptance Criteria
- [ ] HTTP 200 para OPTIONS
- [ ] Header Access-Control-Allow-Origin presente
- [ ] Header Access-Control-Allow-Methods contiene el método solicitado
- [ ] No requiere body

---

## 6. Data Structures

### ProductRecord (ProductosTable item)
```json
{
  "id": "uuid string (Partition Key)",
  "code": "string (Global Secondary Index)",
  "product": {
    "name": "string",
    "price": "number (centavos)",
    "stock_level": "number",
    "low_stock_threshold": "number"
  }
}
```

### VentaRecord (VentasTable item)
```json
{
  "id": "uuid string (Partition Key)",
  "detalle": {
    "status": "COMPLETED | RETURNED",
    "total": "number (centavos)",
    "tax": "number (centavos)",
    "createdAt": "ISO-8601 timestamp (Sort Key)",
    "paymentMethod": "CASH | CARD | TRANSFER | MIXED",
    "items": [
      {
        "productId": "uuid",
        "name": "string",
        "quantity": "number",
        "priceAtSale": "number"
      }
    ]
  }
}
```

---

## 7. Error Handling

Todos los errores deben retornar JSON con estructura:
```json
{
  "error": "Descripción legible",
  "code": "CÓDIGO_EN_MAYUSCULAS",
  "timestamp": "ISO-8601"
}
```

Códigos de error estándar:
- `INVALID_INPUT` (400): Parámetros inválidos
- `NOT_FOUND` (404): Recurso no existe
- `INVALID_PAYMENT` (400): Validación de pago falló
- `INTERNAL_SERVER_ERROR` (500): Error en servidor/BD

---

## 8. Non-Functional Requirements

- **Latency:** GET /products < 500ms; POST /sales < 1s
- **Availability:** 99.9% (SLA de API Gateway + Lambda)
- **Data Format:** Todos los montos en centavos (int), nunca float
- **Authentication:** No requerida en MVP (endpoints públicos)
- **Logging:** Cada invocación Lambda registra en CloudWatch
- **Deployment:** SAM `sam build` + `sam deploy` a us-east-1

---

## 9. Acceptance Criteria Summary

Cada endpoint es **completamente implementado** cuando:
1. ✅ Recibe request correctamente
2. ✅ Valida entrada (400 si inválida)
3. ✅ Consulta DynamoDB (o genera erro 500)
4. ✅ Transforma datos según spec
5. ✅ Retorna HTTP status correcto
6. ✅ Retorna JSON válido
7. ✅ Registra en CloudWatch
8. ✅ Tiene unit test con mock DynamoDB
