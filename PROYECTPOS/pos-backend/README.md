# Backend POS — AWS SAM Serverless

## Arquitectura del Sistema

Este backend implementa una **arquitectura serverless** usando AWS SAM (Serverless Application Model) que expone **2 endpoints REST principales** a través de API Gateway. La lógica de negocio se ejecuta en **dos funciones Lambda (Java 21)** que interactúan con **dos tablas DynamoDB**.

**Nota:** Esta es una implementación simplificada con 2 endpoints principales.

```
┌─────────────┐         HTTPS          ┌──────────────┐
│   Cliente   │ ◄──────────────────► │ API Gateway  │
│  (Browser)  │    JSON REST API      │              │
└─────────────┘                        └──────────────┘
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

## Framework y Tecnologías

- **AWS SAM** (Serverless Application Model) — Infraestructura como código
- **Java 21** — Funciones Lambda con tipos compilados
- **DynamoDB** — Base de datos NoSQL serverless
- **API Gateway** — Exposición de endpoints REST
- **Maven** — Gestión de dependencias y build
- **JUnit 5 + Mockito** — Pruebas unitarias con mocks

### Justificación de AWS SAM vs Spring Boot

| Factor | Spring Boot | AWS SAM | Ganador |
|--------|------------|---------|---------|
| **Costo** | Instancia EC2 24/7 (~$30/mes) | Pago por invocación (~$1/mes para POS) | **SAM** |
| **Escalabilidad** | Manual (auto-scaling config) | Automático (AWS gestiona) | **SAM** |
| **Operaciones** | Gestionar patches, SO, reinicio | Zero ops (AWS maneja) | **SAM** |
| **Time-to-deploy** | Docker build + push (5 min) | `sam deploy` (2 min) | **SAM** |
| **Aprendizaje** | Spring Framework, MVC, Beans | AWS Lambda, API Gateway | **Spring** |

**Decisión:** SAM es la opción más eficiente operacionalmente para un POS en producción con tráfico variable. Paga solo por uso real, escala automáticamente y no requiere gestión de servidores.

## Endpoints Disponibles

### Productos (LambdaProductos)

| Método | Path | Descripción |
|--------|------|-------------|
| GET | `/productos` | Obtener todos los productos (default) |
| GET | `/productos?tipo=id&q={id}` | Buscar por ID (UUID) |
| GET | `/productos?tipo=codigo&q={code}` | Buscar por código (ej: PERI-001) |
| GET | `/productos?tipo=codigoBarras&q={barcode}` | Buscar por código de barras |
| GET | `/productos?tipo=nombre&q={name}` | Buscar por nombre (partial match) |

### Ventas (LambdaVentas)

| Método | Path | Descripción |
|--------|------|-------------|
| POST | `/ventas` | Crear venta (registra items + pago) |

### CORS Preflight

| Método | Path | Descripción |
|--------|------|-------------|
| OPTIONS | `/productos` | Responder a CORS preflight del navegador |
| OPTIONS | `/ventas` | Responder a CORS preflight del navegador |

**Nota:** Estos son los 2 endpoints principales funcionales del sistema POS.

## Instrucciones de Despliegue

### Prerrequisitos

1. **Java 21 SDK** instalado
   ```bash
   java -version  # Debe mostrar Java 21
   ```

2. **AWS SAM CLI** actualizado
   ```bash
   sam --version  # Debe ser >= 1.100.0
   ```

3. **AWS CLI** configurado
   ```bash
   aws configure
   # AWS Access Key ID: [tu-key]
   # AWS Secret Access Key: [tu-secret]
   # Default region name: us-east-1
   # Default output format: json
   ```

### Paso 1: Build

```bash
cd PROYECTPOS/pos-backend
sam build
```

**Resultado esperado:**
```
Build Succeeded

Built Artifacts  : .aws-sam/build
Built Template   : .aws-sam/build/template.yaml
```

### Paso 2: Deploy

```bash
sam deploy --guided
```

**Prompts:**
- Stack name: `pos-sam`
- AWS Region: `us-east-1`
- Confirm changes before deploy: `Y`
- Allow SAM CLI IAM role creation: `Y`
- Disable rollback: `N`
- Save arguments to configuration file: `Y`
- SAM configuration file: `samconfig.toml`
- SAM configuration environment: `default`

**Resultado esperado:**
```
CloudFormation stack changeset
---------------------------------
Operation                     LogicalResourceId
---------------------------------
+ Add                         GetProductsFunction
+ Add                         SaveSaleFunction
+ Add                         ProductosTable
+ Add                         VentasTable
+ Add                         PosApi
---------------------------------

Deploy this changeset? [y/N]: y

Successfully created/updated stack - pos-sam in us-east-1
```

### Paso 3: Verificar

```bash
aws cloudformation describe-stacks --stack-name pos-sam --query 'Stacks[0].Outputs'
```

**Salida esperada:**
```json
[
  {
    "OutputKey": "PosApiEndpoint",
    "OutputValue": "https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod",
    "Description": "API Gateway endpoint URL"
  }
]
```

## URL del API Gateway Desplegado

```
https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod
```

**Ejemplo de uso:**
```bash
curl https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod/api/v1/products
```

## Pruebas Unitarias

### Ejecutar tests

```bash
# Productos service
cd productos-service
mvn test

# Ventas service
cd ../ventas-service
mvn test
```

### Cobertura

- **Target:** ≥70%
- **Herramienta:** JaCoCo (incluido en pom.xml)
- **Mocks:** Mockito para DynamoDB

**Ejemplo de test:**
```java
@Test
void testGetAll_success() {
    // Arrange
    when(ddbClient.scan(...)).thenReturn(scanResponse);
    
    // Act
    List<ProductDTO> result = service.getAllProducts();
    
    // Assert
    assertEquals(2, result.size());
}
```

## Capturas de Pantalla

### Postman - GET /productos (éxito)
![GET productos](./docs/screenshots/postman-get-productos.png)

**Request:**
```
GET https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod/api/v1/products
```

**Response (200 OK):**
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

### Postman - POST /ventas (éxito)
![POST ventas](./docs/screenshots/postman-post-ventas.png)

**Request:**
```json
POST https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod/api/v1/sales
Content-Type: application/json

{
  "items": [
    {
      "productId": "a1b2c3d4-e5f6-4789-a012-b3c4d5e6f7a8",
      "quantity": 2,
      "priceAtSale": 45000
    }
  ],
  "paymentMethod": "CASH",
  "amountPaid": 90000,
  "discount": 0
}
```

**Response (201 Created):**
```json
{
  "id": "s5a6b7c8-d9e0-4f12-a345-b6c7d8e9f0a1",
  "total": 90000,
  "tax": 17100,
  "paymentMethod": "CASH",
  "createdAt": "2024-12-15T10:30:45Z",
  "items": [...]
}
```

### Postman - Error 400 (items vacío)
![Error 400](./docs/screenshots/postman-error-400.png)

**Request:**
```json
POST https://4udq52ntxl.execute-api.us-east-1.amazonaws.com/Prod/api/v1/sales

{
  "items": [],
  "paymentMethod": "CASH",
  "amountPaid": 0
}
```

**Response (400 Bad Request):**
```json
{
  "error": "Items array cannot be empty",
  "code": "INVALID_INPUT",
  "timestamp": "2024-12-15T10:30:45Z"
}
```

### Pruebas Unitarias
![Tests](./docs/screenshots/unit-tests.png)

**Comando:**
```bash
mvn test
```

**Salida:**
```
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## Proceso SDD (Spec-Driven Development)

### 1. Specs Primero

Antes de escribir cualquier línea de código, creamos tres documentos en `.kiro/specs/pos-backend/`:

- **requirements.md**: 14 endpoints con acceptance criteria detallados
- **design.md**: ADRs (Architecture Decision Records), DynamoDB schema, Lambda design
- **tasks.md**: 40+ tareas de implementación en orden de ejecución

**Evidencia:** Timestamps de commits muestran specs antes que código.

### 2. Implementación Guiada

Cada función Lambda se implementó siguiendo:

1. **Contrato de endpoint** en requirements.md
2. **Estructura de código** en design.md
3. **Orden de tareas** en tasks.md

**Ejemplo de trazabilidad:**

| Spec | Implementación |
|------|----------------|
| Requirements.md: "GET /api/v1/products" | GetProductsHandler.java |
| Design.md: "ProductService.getAllProducts()" | ProductService.java línea 45 |
| Tasks.md: "Task 2.1: Crear GetProductsHandler" | Commit SHA: abc123 |

### 3. Validación

- **Acceptance criteria** → casos de prueba
- **Error codes** en requirements → manejo en código
- **Data structures** en design → DTOs en Java

**Ejemplo:**

Requirements.md dice:
```
WHEN items array is empty, THEN return HTTP 400 with code INVALID_INPUT
```

Código implementa:
```java
if (request.getItems().isEmpty()) {
    return buildErrorResponse(400, "Items cannot be empty", "INVALID_INPUT");
}
```

Test verifica:
```java
@Test
void testCreateSale_emptyItems_returns400() {
    // ...
    assertEquals(400, response.getStatusCode());
    assertTrue(response.getBody().contains("INVALID_INPUT"));
}
```

### 4. Beneficios del SDD

- ✅ **Claridad:** Todos saben qué construir antes de empezar
- ✅ **Trazabilidad:** Cada línea de código tiene un "por qué"
- ✅ **Testabilidad:** Acceptance criteria son casos de prueba
- ✅ **Documentación:** Specs son documentación viva
- ✅ **Colaboración:** Equipo revisa specs antes de implementar

## Estructura del Repositorio

```
PROYECTPOS/pos-backend/
├── .kiro/specs/pos-backend/
│   ├── requirements.md      # 14 endpoints documentados
│   ├── design.md            # ADRs, DynamoDB schema, Lambda design
│   └── tasks.md             # 40+ tareas de implementación
│
├── productos-service/       # Lambda independiente
│   ├── src/main/java/com/pos/productos/
│   │   ├── handler/
│   │   │   └── ProductosHandler.java
│   │   ├── service/
│   │   │   └── ProductService.java
│   │   ├── repository/
│   │   │   └── ProductRepository.java
│   │   └── model/
│   │       ├── ProductRecord.java
│   │       └── ProductDTO.java
│   ├── src/test/java/
│   │   └── com/pos/productos/
│   │       ├── service/ProductServiceTest.java
│   │       └── repository/ProductRepositoryTest.java
│   └── pom.xml
│
├── ventas-service/          # Lambda independiente
│   ├── src/main/java/com/pos/ventas/
│   │   ├── handler/
│   │   │   └── VentasHandler.java
│   │   ├── service/
│   │   │   └── SaleService.java
│   │   ├── repository/
│   │   │   └── SaleRepository.java
│   │   └── model/
│   │       ├── VentaRecord.java
│   │       ├── VentaRequest.java
│   │       └── VentaResponse.java
│   ├── src/test/java/
│   └── pom.xml
│
├── template.yaml            # SAM template (infraestructura)
├── samconfig.toml           # Configuración de despliegue
├── .gitignore
└── README.md
```

## Troubleshooting

### Error: "Unable to import module 'handler'"

**Causa:** JAR no se construyó correctamente.

**Solución:**
```bash
sam build --use-container
sam deploy
```

### Error: "Access Denied" en DynamoDB

**Causa:** IAM role de Lambda no tiene permisos.

**Solución:** Verificar en `template.yaml`:
```yaml
Policies:
  - DynamoDBCrudPolicy:
      TableName: !Ref ProductosTable
```

### Error: "CORS policy blocked"

**Causa:** Headers CORS no configurados.

**Solución:** Verificar que Lambda retorna:
```java
headers.put("Access-Control-Allow-Origin", "*");
headers.put("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
```

## Licencia

MIT

---

**Desarrollado con ❤️ usando Spec-Driven Development**
