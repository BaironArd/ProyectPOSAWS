# POS Serverless — Microservicios AWS

Migración del backend POS a arquitectura serverless usando AWS Lambda, API Gateway y DynamoDB.

## Arquitectura

```
Cliente / Postman
       │
       ▼
  API Gateway
  ┌────┴────┐
  │         │
  ▼         ▼
Lambda    Lambda
Productos  Ventas
  │         │
  ▼         ▼
DynamoDB  DynamoDB
Productos  Ventas
```

## Microservicios

### 1. Microservicio Productos (`productos-service`)

**Endpoint:** `GET /productos`

Consulta productos por diferentes criterios:

| Parámetro | Descripción                                      | Ejemplo                          |
|-----------|--------------------------------------------------|----------------------------------|
| `tipo`    | Tipo de búsqueda                                 | `nombre`, `codigo`, `codigoBarras`, `id`, `todos` |
| `q`       | Valor a buscar (no requerido cuando tipo=`todos`) | `mouse`                          |

**Ejemplos:**
```http
GET /productos?tipo=nombre&q=mouse
GET /productos?tipo=codigoBarras&q=7501234567890
GET /productos?tipo=codigo&q=PROD-001
GET /productos?tipo=id&q=abc-123-uuid
GET /productos?tipo=todos
```

**Flujo interno:**
```
ProductosHandler → ProductoService → ProductoRepository → DynamoDB (Tabla Productos)
```

---

### 2. Microservicio Ventas (`ventas-service`)

**Endpoint:** `POST /ventas`

Registra una venta del POS en DynamoDB.

**Body JSON:**
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
    },
    {
      "productoId": "prod-002",
      "nombre": "Teclado Mecánico",
      "cantidad": 1,
      "precioUnitario": 80000
    }
  ]
}
```

**Respuesta:**
```json
{
  "success": true,
  "mensaje": "Venta registrada exitosamente",
  "data": {
    "ventaId": "uuid-generado",
    "subtotal": 170000,
    "iva": 32300,
    "total": 202300,
    "cambio": 297700,
    "estado": "COMPLETADA",
    "fechaHora": "2024-01-15T10:30:00Z",
    "detalle": [...]
  }
}
```

**Flujo interno:**
```
VentasHandler → VentaService → VentaRepository → DynamoDB (Tabla Ventas)
```

---

## Estructura del proyecto

```
aws-microservices/
├── template.yaml              ← SAM: define Lambdas, API Gateway y DynamoDB
├── samconfig.toml             ← Configuración de despliegue SAM
├── productos-service/
│   ├── pom.xml
│   └── src/main/java/com/pos/aws/productos/
│       ├── handler/           ← ProductosHandler (punto de entrada Lambda)
│       ├── service/           ← ProductoService (lógica y validaciones)
│       ├── repository/        ← ProductoRepository (consultas DynamoDB)
│       └── model/             ← ProductoDynamo, ProductoResponse
└── ventas-service/
    ├── pom.xml
    └── src/main/java/com/pos/aws/ventas/
        ├── handler/           ← VentasHandler (punto de entrada Lambda)
        ├── service/           ← VentaService (lógica, cálculos, validaciones)
        ├── repository/        ← VentaRepository (escritura DynamoDB)
        └── model/             ← VentaDynamo, RegistrarVentaRequest, VentaResponse
```

---

## Tablas DynamoDB

### Tabla `Productos-{entorno}`

| Atributo     | Tipo   | Descripción                        |
|--------------|--------|------------------------------------|
| `id`         | String | Clave primaria (PK)                |
| `codigoBarras` | String | GSI: búsqueda por código de barras |
| `codigo`     | String | GSI: búsqueda por código alfanumérico |
| `nombre`     | String | Nombre del producto                |
| `precio`     | Number | Precio en centavos                 |
| `stock`      | Number | Cantidad disponible                |
| `categoria`  | String | Categoría del producto             |
| `activo`     | Boolean | Si el producto está activo        |

### Tabla `Ventas-{entorno}`

| Atributo     | Tipo   | Descripción                        |
|--------------|--------|------------------------------------|
| `ventaId`    | String | Clave primaria (UUID)              |
| `cajero`     | String | Usuario que realizó la venta       |
| `metodoPago` | String | EFECTIVO / TARJETA / TRANSFERENCIA |
| `montoPagado` | Number | Monto recibido del cliente        |
| `subtotal`   | Number | Subtotal sin IVA                   |
| `iva`        | Number | IVA (19%)                          |
| `total`      | Number | Total con IVA                      |
| `cambio`     | Number | Cambio devuelto                    |
| `fechaHora`  | String | ISO-8601 timestamp                 |
| `estado`     | String | COMPLETADA / PENDIENTE / ANULADA   |
| `detalle`    | List   | Array de ítems vendidos            |

---

## Requisitos previos

- Java 21
- Maven 3.9+
- AWS CLI configurado (`aws configure`)
- AWS SAM CLI instalado

## Comandos

### Build

```bash
# Compilar microservicio productos
cd productos-service
mvn clean package -DskipTests

# Compilar microservicio ventas
cd ventas-service
mvn clean package -DskipTests
```

### Tests

```bash
cd productos-service && mvn test
cd ventas-service && mvn test
```

### Despliegue con SAM

```bash
# Desde la carpeta aws-microservices/
cd aws-microservices

# Primera vez (guiado)
sam build
sam deploy --guided

# Despliegues siguientes
sam build
sam deploy
```

### Prueba local con SAM

```bash
# Levantar API local
sam local start-api

# Probar productos
curl "http://localhost:3000/productos?tipo=todos"

# Probar ventas
curl -X POST http://localhost:3000/ventas \
  -H "Content-Type: application/json" \
  -d '{"cajero":"test","metodoPago":"EFECTIVO","montoPagado":500000,"items":[{"productoId":"p1","nombre":"Mouse","cantidad":1,"precioUnitario":45000}]}'
```

---

## Notas de arquitectura

- Cada Lambda tiene **una sola responsabilidad** (Productos = consultar, Ventas = registrar).
- La capa Handler solo maneja HTTP — no contiene lógica de negocio.
- La capa Service contiene validaciones y cálculos — no sabe nada de AWS.
- La capa Repository encapsula DynamoDB — fácil de mockear en tests.
- Los clientes DynamoDB se inicializan **fuera del método handleRequest** para aprovechar el warm start de Lambda.
- Las tablas usan `PAY_PER_REQUEST` (on-demand) — sin costos fijos.
