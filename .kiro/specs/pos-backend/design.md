# Backend Design — AWS SAM Serverless POS
**Version:** 1.0  
**Technology Decisions:** Java 21 · Lambda · DynamoDB · SAM  

---

## 1. Architecture Decision Record (ADR)

### 1.1 Why AWS SAM Instead of Spring Boot?

**Decision:** Migrar de Spring Boot (monolito) a AWS SAM (serverless).

**Rationale:**
| Factor | Spring Boot | AWS SAM | Winner |
|--------|------------|---------|--------|
| **Cost** | Instancia EC2 24/7 (~$30/mes) | Pago por invocación (~$1/mes para POS) | **SAM** |
| **Scaling** | Manual (auto-scaling config) | Automático (AWS gestiona) | **SAM** |
| **Ops** | Gestionar patches, SO, reinicio | Zero ops (AWS maneja) | **SAM** |
| **Learning** | Spring Framework, MVC, Beans | AWS Lambda, API Gateway | **Spring** |
| **Time-to-deploy** | Docker build + push (5 min) | `sam deploy` (2 min) | **SAM** |

**Decision:** SAM es elegido porque es la opción más eficiente operacionalmente para un POS en producción con usuarios puntuales.

---

### 1.2 Lambda Language: Java 21 vs Node.js

**Decision:** Usar Java 21 para ambas Lambdas.

**Rationale:**
- **Tipo system:** TypeScript en Node.js vs Java 21 con tipos compilados → menos bugs
- **Performance:** Java 21 es más rápido después de warmup
- **Equipo:** El backend anteriormente era Spring Boot (Java) → reutilizar conocimiento

---

### 1.3 Database: DynamoDB NoSQL vs RDS SQL

**Decision:** DynamoDB (NoSQL).

**Rationale:**
- **Serverless:** RDS requiere instance mínima siempre; DynamoDB paga por uso
- **Escalabilidad:** DynamoDB escala a millones de requests sin reconfig
- **Throughput:** API Gateway + Lambda son **muy rápidos** → mejor con NoSQL
- **Schema flexibility:** Productos y ventas pueden tener campos variables

**Trade-off:**
- ❌ No hay JOINs (pero no los necesitamos)
- ❌ Queries complejas requieren denormalización
- ✅ Extremadamente rápido y escalable

---

## 2. DynamoDB Table Design

### 2.1 ProductosTable

**Partition Key (PK):** `id` (UUID string)  
**Sort Key (SK):** None  
**Global Secondary Index (GSI):** `code-index` → Permite buscar por código

```
ProductosTable
├── Partition Key
│   └── id (String): "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8"
│
├── Attributes
│   ├── code (String): "PERI-001"
│   └── product (Map)
│       ├── name (String): "Mouse Inalámbrico"
│       ├── price (Number): 45000 (centavos)
│       ├── stock_level (Number): 25
│       └── low_stock_threshold (Number): 5
│
└── Global Secondary Index
    ├── Index Name: code-index
    ├── PK: code
    ├── Projection: ALL (todos los atributos)
    └── Capacity: Pay-per-request
```

**Example Item:**
```json
{
  "id": "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8",
  "code": "PERI-001",
  "product": {
    "name": "Mouse Inalámbrico",
    "price": 45000,
    "stock_level": 25,
    "low_stock_threshold": 5
  }
}
```

**Capacity Mode:** On-demand (pay-per-request)  
**Justification:** POS tiene picos de uso (mañana/tarde) → on-demand es más barato

---

### 2.2 VentasTable

**Partition Key (PK):** `id` (UUID string)  
**Sort Key (SK):** `detalle.createdAt` (ISO-8601 timestamp)  
**Global Secondary Index:** `paymentMethod-createdAt` → Permite filtrar por método de pago + fecha

```
VentasTable
├── Partition Key
│   └── id (String): "s5a6b7c8-d9e0-4f12-a345-b6c7d8e9f0a1"
│
├── Sort Key
│   └── createdAt (String): "2024-12-15T10:30:45Z"
│
├── Attributes
│   └── detalle (Map)
│       ├── status (String): "COMPLETED" | "RETURNED"
│       ├── total (Number): 175100 (centavos, incluye IVA)
│       ├── tax (Number): 100 (centavos)
│       ├── paymentMethod (String): "CASH" | "CARD" | "TRANSFER" | "MIXED"
│       └── items (List of Maps)
│           └── [0]
│               ├── productId (String): "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8"
│               ├── name (String): "Mouse Inalámbrico"
│               ├── quantity (Number): 2
│               └── priceAtSale (Number): 45000
│
└── Global Secondary Index
    ├── Index Name: paymentMethod-createdAt
    ├── PK: paymentMethod
    ├── SK: createdAt
    ├── Projection: ALL
    └── Capacity: On-demand
```

**Example Item:**
```json
{
  "id": "s5a6b7c8-d9e0-4f12-a345-b6c7d8e9f0a1",
  "createdAt": "2024-12-15T10:30:45Z",
  "detalle": {
    "status": "COMPLETED",
    "total": 175100,
    "tax": 33250,
    "paymentMethod": "CASH",
    "items": [
      {
        "productId": "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8",
        "name": "Mouse Inalámbrico",
        "quantity": 2,
        "priceAtSale": 45000
      },
      {
        "productId": "b2c3d4e5-f6a7-4890-b123-c4d5e6f7a8b9",
        "name": "Teclado Mecánico",
        "quantity": 1,
        "priceAtSale": 85000
      }
    ]
  }
}
```

**Capacity Mode:** On-demand  
**Justification:** Escrituras intensivas durante ventas; on-demand maneja picos sin sobreprovisionar

---

## 3. Lambda Function Design

### 3.1 GetProductsFunction (Java 21)

**Handler Invocation:** `com.pos.sam.products.handler.GetProductsHandler::handleRequest`

**Request Flow:**
```
API Gateway (GET /api/v1/products)
          │
          ▼
GetProductsHandler
          │
          ├─► Validate Query Params (type, q)
          │
          ├─► ProductService
          │   ├─► Parse query type (id|code|name)
          │   └─► Build DynamoDB query
          │
          ├─► ProductRepository
          │   └─► Execute DynamoDB query
          │           (Scan o Query según index)
          │
          ├─► Transform to DTO
          │
          └─► Return HTTP 200 + JSON
```

**Response Format:**
```json
{
  "statusCode": 200,
  "headers": {
    "Access-Control-Allow-Origin": "*",
    "Content-Type": "application/json"
  },
  "body": "{\"products\": [...], \"count\": 2}"
}
```

**Dependencies:**
- AWS SDK for Java v2: `software.amazon.awssdk:dynamodb`
- Jackson: `com.fasterxml.jackson.core:jackson-databind` (JSON serialization)
- Lombok: `org.projectlombok:lombok` (reducir boilerplate)

**Logging:** Todos los pasos registran en CloudWatch Logs usando `System.out.println()`

---

### 3.2 SaveSaleFunction (Java 21)

**Handler Invocation:** `com.pos.sam.sales.handler.SaveSaleHandler::handleRequest`

**Request Flow:**
```
API Gateway (POST /api/v1/sales)
          │
          ▼
SaveSaleHandler
          │
          ├─► Parse Request Body (JSON → VentaRequest)
          │
          ├─► Validate Input
          │   ├─► Items no vacío
          │   ├─► Payment amount ≥ total
          │   └─► UUIDs válidos
          │
          ├─► SaleService
          │   ├─► Calculate total
          │   ├─► Calculate tax (19% IVA)
          │   └─► Generate sale UUID
          │
          ├─► SaleRepository
          │   └─► Save to VentasTable
          │
          ├─► Transform to VentaResponse DTO
          │
          └─► Return HTTP 201 + JSON
```

**Response Format (Success):**
```json
{
  "statusCode": 201,
  "headers": {
    "Access-Control-Allow-Origin": "*",
    "Content-Type": "application/json"
  },
  "body": "{\"id\": \"...\", \"total\": 175100, ...}"
}
```

**Response Format (Error):**
```json
{
  "statusCode": 400,
  "headers": {"Access-Control-Allow-Origin": "*"},
  "body": "{\"error\": \"Items cannot be empty\", \"code\": \"INVALID_INPUT\"}"
}
```

---

### 3.3 OPTIONS Handler (Both Functions)

**Implementación en ambas Lambdas:**
```
Request Method = OPTIONS
          │
          ▼
Check if "OPTIONS"
          │
          ▼
Return preflight() response
```

**Response:**
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Max-Age: 3600
```

**Justification:** Los navegadores envían OPTIONS antes de GET/POST para validar CORS. Sin esta respuesta, el request real es bloqueado.

---

## 4. API Gateway Configuration (template.yaml)

### 4.1 Resource Structure

```
API Gateway (pos-api)
├── /api
│   └── /v1
│       ├── /products
│       │   ├── GET       → GetProductsFunction
│       │   └── OPTIONS   → GetProductsFunction
│       │
│       └── /sales
│           ├── POST      → SaveSaleFunction
│           ├── GET       → SaveSaleFunction
│           └── OPTIONS   → SaveSaleFunction
```

### 4.2 API Gateway Settings

**Auth:** None (abierto públicamente)  
**CORS:** Manejado por OPTIONS handlers  
**Logging:** CloudWatch (opcional, agregado costo)  
**Stage:** `Prod` (default en SAM)  

---

## 5. Montos y Dinero

**Estándar:** Todos los montos se almacenan en **centavos** (integers), NUNCA floats.

**Conversión:**
- Moneda: "45.50 USD" → Centavos: 4550
- Moneda: "0.19 USD" (IVA) → Centavos: 19
- Moneda: "100.00 USD" → Centavos: 10000

**Cálculo IVA:**
```
tax = Math.round(subtotal * 0.19)  // 19% IVA Colombia
total = subtotal + tax
```

**Justificación:** Evita errores de redondeo de punto flotante. El dinero no es decimal.

---

## 6. Timestamp Standard

**Formato:** ISO-8601 UTC  
**Ejemplo:** `2024-12-15T10:30:45Z`  
**Implementación (Java 21):**
```java
Instant.now().toString()  // "2024-12-15T10:30:45.123456Z"
```

**Uso en DynamoDB:** Strings (fáciles de buscar)  
**Justificación:** ISO-8601 es estándar internacional, UTC evita confusiones de zona horaria

---

## 7. Error Response Contract

Todos los errores deben retornar:
```json
{
  "error": "Human-readable description",
  "code": "SHOUTING_SNAKE_CASE",
  "timestamp": "2024-12-15T10:30:45Z"
}
```

**Códigos definidos:**
- `INVALID_INPUT` (400): Parámetros mal formados
- `NOT_FOUND` (404): Recurso no existe
- `INVALID_PAYMENT` (400): Pago insuficiente
- `DATABASE_ERROR` (500): Error conectando DynamoDB
- `INTERNAL_SERVER_ERROR` (500): Error inesperado

---

## 8. Deployment Architecture

```
git push to main
        │
        ▼
GitHub Actions (optional)
        │
        ▼
Local: sam build
        │ (compila Java, genera artefactos)
        ▼
Local: sam deploy --guided
        │ (sube a S3, crea/actualiza CloudFormation)
        ▼
AWS CloudFormation
        │
        ├─► IAM Role (LambdaExecutionRole)
        │   └─► Policy: DynamoDB ReadWrite, CloudWatch Logs
        │
        ├─► GetProductsFunction
        │   └─► Code: JAR desde S3
        │
        ├─► SaveSaleFunction
        │   └─► Code: JAR desde S3
        │
        ├─► ProductosTable (DynamoDB)
        ├─► VentasTable (DynamoDB)
        │
        └─► API Gateway
            └─► URL: https://{api-id}.execute-api.us-east-1.amazonaws.com/Prod
```

---

## 9. Data Flow Examples

### Ejemplo 1: GET /api/v1/products?type=name&q=mouse

```
Frontend (React)
    │ fetch("https://.../api/v1/products?type=name&q=mouse")
    │
    ▼
API Gateway (GET /products)
    │
    ▼
GetProductsFunction (Lambda)
    │
    ├─ Parse queryStringParameters: {type: "name", q: "mouse"}
    │
    ├─ ProductService.searchByName("mouse")
    │   │
    │   └─ ProductRepository.scanByName("mouse")
    │       │
    │       ▼ DynamoDB Scan (sin GSI, tabla pequeña <1000 items)
    │       └─ Retorna: [ProductRecord, ProductRecord]
    │
    ├─ Map ProductRecord[] → ProductDTO[]
    │
    └─ Return {statusCode: 200, body: JSON.stringify({products: [...], count: 2})}
           │
           ▼
       API Gateway (200 OK)
           │
           ▼
       Frontend (React usePOSStore.setProductos([...]))
```

---

### Ejemplo 2: POST /api/v1/sales

```
Frontend (React)
    │ fetch("/api/v1/sales", {method: "POST", body: {...}})
    │
    ▼
API Gateway (POST /sales)
    │
    ▼
SaveSaleHandler (Lambda)
    │
    ├─ Parse body JSON → VentaRequest
    │   {items: [{productId, quantity, priceAtSale}], paymentMethod, amountPaid}
    │
    ├─ Validate
    │   ├─ items.length > 0 ✓
    │   ├─ amountPaid ≥ subtotal ✓
    │   └─ UUIDs válidos ✓
    │
    ├─ SaleService.createSale()
    │   ├─ Calcular subtotal = sum(quantity × priceAtSale)
    │   ├─ Calcular tax = Math.round(subtotal × 0.19)
    │   ├─ total = subtotal + tax
    │   └─ id = UUID.randomUUID()
    │
    ├─ SaleRepository.save(VentaRecord)
    │   │
    │   ▼ DynamoDB PutItem
    │   └─ VentasTable: {id, createdAt, detalle: {...}}
    │
    ├─ Return VentaResponse {id, total, items, ...}
    │
    └─ Return {statusCode: 201, body: JSON.stringify(ventaResponse)}
           │
           ▼
       API Gateway (201 Created)
           │
           ▼
       Frontend (alert "Venta confirmada")
```

---

## 10. Code Organization (Java)

```
get-products/
├── pom.xml
└── src/main/java/com/pos/sam/products/
    ├── handler/
    │   └── GetProductsHandler.java
    │       ├── handleRequest(APIGatewayProxyRequestEvent) → APIGatewayProxyResponseEvent
    │       └── preflight() → respuesta OPTIONS
    │
    ├── service/
    │   └── ProductService.java
    │       ├── getAllProducts()
    │       ├── searchById(uuid)
    │       ├── searchByCode(code)
    │       └── searchByName(name)
    │
    ├── repository/
    │   └── ProductRepository.java
    │       ├── DynamoDbClient (inyectado)
    │       └── query(), scan() methods
    │
    └── model/
        ├── ProductRecord.java (item DynamoDB)
        └── ProductDTO.java (respuesta API)

save-sale/
├── pom.xml
└── src/main/java/com/pos/sam/sales/
    ├── handler/
    │   └── SaveSaleHandler.java
    │       ├── handleRequest(APIGatewayProxyRequestEvent)
    │       ├── route(method) → switch sobre GET/POST/OPTIONS
    │       └── preflight()
    │
    ├── service/
    │   └── SaleService.java
    │       ├── createSale(VentaRequest) → VentaResponse
    │       ├── listSales()
    │       └── generateReport(method|date)
    │
    ├── repository/
    │   └── SaleRepository.java
    │       ├── save(VentaRecord)
    │       ├── findAll()
    │       └── queryByPaymentMethod(method)
    │
    └── model/
        ├── VentaRecord.java
        ├── VentaRequest.java (body)
        ├── VentaResponse.java (response)
        └── SaleItem.java
```

---

## 11. Testing Strategy

**Unit Testing:**
- Test cada service con mock de repository
- Mock DynamoDbClient usando Mockito
- Casos: éxito, validación fallida, excepción BD

**Mock Pattern:**
```java
@Mock
private DynamoDbClient ddbClient;

@BeforeEach
void setUp() {
    service = new ProductService(ddbClient);
}

@Test
void testGetAll_success() {
    // Arrange
    when(ddbClient.scan(...)).thenReturn(scanResponse);
    
    // Act
    List<ProductDTO> result = service.getAllProducts();
    
    // Assert
    assertEquals(2, result.size());
}

@Test
void testGetAll_databaseError() {
    // Arrange
    when(ddbClient.scan(...)).thenThrow(DynamoDbException.class);
    
    // Act & Assert
    assertThrows(DatabaseException.class, () -> service.getAllProducts());
}
```

---

## 12. Deployment Checklist

- [ ] Java 21 SDK instalado localmente
- [ ] AWS SAM CLI actualizado (`sam --version`)
- [ ] AWS credentials configuradas (`aws configure`)
- [ ] Region: `us-east-1`
- [ ] `sam build` exitoso (sin errores)
- [ ] `sam deploy` exitoso (stack creado/actualizado)
- [ ] Verificar en AWS Console:
  - [ ] API Gateway URL generada
  - [ ] Lambdas creadas y con role IAM
  - [ ] Tablas DynamoDB con GSIs
- [ ] Probar endpoints con Postman/curl
