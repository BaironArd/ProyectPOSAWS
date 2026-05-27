# pos-sam — POS Serverless AWS

Sistema POS migrado a arquitectura serverless con AWS SAM.

## Arquitectura

```
Cliente / Postman / Frontend
           │
           ▼
      API Gateway (Prod)
      /api/v1/*
      ┌──────┴──────┐
      │             │
      ▼             ▼
GetProductsFunction  SaveSaleFunction
(Lambda Java 21)     (Lambda Java 21)
      │             │
      ▼             ▼
ProductosTable    VentasTable
(DynamoDB)        (DynamoDB)
```

## Estructura

```
pos-sam/
├── template.yaml          ← SAM: Lambdas + API Gateway + DynamoDB + IAM
├── samconfig.toml         ← Config despliegue (stack: pos-sam, region: us-east-1)
├── seed-products.py       ← Carga 50 productos de prueba
├── get-products/          ← Lambda GetProductsFunction
│   ├── pom.xml
│   └── src/main/java/com/pos/sam/products/
│       ├── handler/GetProductsHandler.java   ← Punto de entrada Lambda
│       ├── service/ProductService.java        ← Lógica y validaciones
│       ├── repository/ProductRepository.java  ← Consultas DynamoDB
│       └── model/                             ← ProductRecord, ProductItem
└── save-sale/             ← Lambda SaveSaleFunction
    ├── pom.xml
    └── src/main/java/com/pos/sam/sales/
        ├── handler/SaveSaleHandler.java       ← Punto de entrada Lambda
        ├── service/SaleService.java           ← Ventas, pagos, reportes
        ├── repository/SaleRepository.java     ← DynamoDB
        └── model/                             ← VentaRecord, SaleDetail, SaleItem
```

## 14 Endpoints

### GetProductsFunction
| # | Método | Path | Descripción |
|---|--------|------|-------------|
| 1 | GET | `/api/v1/products` | Todos los productos |
| 2 | GET | `/api/v1/products?type=id&q={id}` | Por ID |
| 3 | GET | `/api/v1/products?type=code&q={code}` | Por código |
| 4 | GET | `/api/v1/products?type=name&q={name}` | Por nombre |
| 5 | GET | `/api/v1/products/{id}` | Por ID (path param) |

### SaveSaleFunction
| # | Método | Path | Descripción |
|---|--------|------|-------------|
| 6 | POST | `/api/v1/sales` | Crear venta |
| 7 | GET | `/api/v1/sales` | Listar ventas |
| 8 | GET | `/api/v1/sales/{id}` | Obtener venta por ID |
| 9 | POST | `/api/v1/payments` | Procesar pago |
| 10 | GET | `/api/v1/payments?method=CASH` | Ventas por método de pago |
| 11 | GET | `/api/v1/reports/daily?date=2024-01-15` | Ventas del día |
| 12 | GET | `/api/v1/reports/summary` | Resumen financiero |
| 13 | GET | `/api/v1/reports/top-products` | Top productos vendidos |
| 14 | OPTIONS | `/api/v1/*` | CORS preflight |

## Estructura DynamoDB

### ProductosTable
```json
{
  "id":   "uuid",
  "code": "PERI-001",
  "producto": {
    "name":                "Mouse Inalámbrico",
    "price":               45000,
    "stock_level":         25,
    "low_stock_threshold": 5
  }
}
```

### VentasTable
```json
{
  "id": "uuid",
  "detalle": {
    "status":        "COMPLETED",
    "total":         107100,
    "createdAt":     "2024-01-15T10:30:00Z",
    "paymentMethod": "CASH",
    "items": [
      {
        "productId":  "PERI-001",
        "name":       "Mouse Inalámbrico",
        "quantity":   2,
        "unitPrice":  45000,
        "subtotal":   90000
      }
    ]
  }
}
```

## Requisitos previos

- Java 21
- Maven 3.9+
- Python 3.8+ con `pip install boto3`
- [AWS CLI](https://aws.amazon.com/cli/) configurado
- [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html)

## Instalación AWS CLI y SAM CLI (Windows)

```powershell
# AWS CLI
winget install Amazon.AWSCLI

# SAM CLI
winget install Amazon.SAM-CLI

# Verificar
aws --version
sam --version
```

## Configurar credenciales AWS

```bash
aws configure
# AWS Access Key ID:     [tu access key]
# AWS Secret Access Key: [tu secret key]
# Default region name:   us-east-1
# Default output format: json
```

## Permisos IAM requeridos

El usuario IAM necesita estas políticas:
- `AWSCloudFormationFullAccess`
- `AWSLambda_FullAccess`
- `AmazonDynamoDBFullAccess`
- `AmazonAPIGatewayAdministrator`
- `IAMFullAccess`
- `AmazonS3FullAccess`

## Despliegue

```bash
# 1. Compilar ambas Lambdas
cd pos-sam/get-products && mvn clean package -DskipTests
cd ../save-sale && mvn clean package -DskipTests
cd ..

# 2. Build SAM
sam build

# 3. Despliegue guiado (primera vez)
sam deploy --guided
# Stack Name: pos-sam
# Region:     us-east-1
# Confirm changeset: Y
# Allow SAM CLI IAM role creation: Y
# Save arguments to samconfig.toml: Y

# 4. Despliegues siguientes
sam deploy
```

## Cargar productos de prueba

```bash
# Después del despliegue
python pos-sam/seed-products.py
```

## Conectar el frontend

Editar `.env` del frontend:
```env
BACKEND_URL=https://[API_ID].execute-api.us-east-1.amazonaws.com/Prod
```

Iniciar frontend:
```bash
node src/app.js
# http://localhost:3000
```

## Pruebas con curl

```bash
BASE=https://[API_ID].execute-api.us-east-1.amazonaws.com/Prod

# Todos los productos
curl "$BASE/api/v1/products"

# Buscar por nombre
curl "$BASE/api/v1/products?type=name&q=mouse"

# Crear venta
curl -X POST "$BASE/api/v1/sales" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentMethod": "CASH",
    "amountPaid": 200000,
    "items": [
      {"productId":"PERI-001","name":"Mouse","quantity":2,"unitPrice":45000}
    ]
  }'

# Reporte resumen
curl "$BASE/api/v1/reports/summary"
```

## Ver en CloudFormation

1. Ir a AWS Console → CloudFormation
2. Stack: `pos-sam`
3. Pestaña **Resources**: ver Lambdas, tablas, API Gateway
4. Pestaña **Outputs**: ver URLs de los endpoints
