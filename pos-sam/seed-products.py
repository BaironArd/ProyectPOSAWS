"""
seed-products.py
Carga productos de prueba en la tabla ProductosTable de DynamoDB.

Estructura de cada item (exactamente como pide el profesor):
{
    "id":                 String (PK - UUID),
    "code":               String (GSI code-index),
    "name":               String,
    "price":              Number,
    "stock_level":        Number,
    "low_stock_threshold": Number
}

Uso:
    python pos-sam/seed-products.py

Requisitos:
    pip install boto3
    aws configure  (credenciales configuradas)
"""

import boto3
import uuid
from decimal import Decimal

TABLE_NAME = "ProductosTable"
REGION     = "us-east-1"

PRODUCTS = [
    # Bebidas
    {"code": "BEB-001", "name": "Red Bull 250ml",          "price": 3500,  "stock": 40, "threshold": 5},
    {"code": "BEB-002", "name": "Coca-Cola 350ml",         "price": 2500,  "stock": 60, "threshold": 10},
    {"code": "BEB-003", "name": "Agua Cristal 600ml",      "price": 1800,  "stock": 80, "threshold": 15},
    {"code": "BEB-004", "name": "Jugo Hit Naranja 300ml",  "price": 2200,  "stock": 45, "threshold": 8},
    {"code": "BEB-005", "name": "Gatorade Azul 500ml",     "price": 4000,  "stock": 30, "threshold": 5},
    {"code": "BEB-006", "name": "Pony Malta 330ml",        "price": 2800,  "stock": 50, "threshold": 8},
    {"code": "BEB-007", "name": "Sprite 350ml",            "price": 2500,  "stock": 55, "threshold": 10},
    {"code": "BEB-008", "name": "Monster Energy 473ml",    "price": 6500,  "stock": 25, "threshold": 5},

    # Snacks
    {"code": "SNK-001", "name": "Papas Margarita 30g",     "price": 1500,  "stock": 100,"threshold": 20},
    {"code": "SNK-002", "name": "Chitos 40g",              "price": 1800,  "stock": 80, "threshold": 15},
    {"code": "SNK-003", "name": "Oreo 36g",                "price": 2200,  "stock": 60, "threshold": 10},
    {"code": "SNK-004", "name": "Nucita 30g",              "price": 1200,  "stock": 90, "threshold": 15},
    {"code": "SNK-005", "name": "Chocolatina Jet 16g",     "price": 1000,  "stock": 120,"threshold": 20},
    {"code": "SNK-006", "name": "Maní Salado 50g",         "price": 2000,  "stock": 70, "threshold": 12},
    {"code": "SNK-007", "name": "Galletas Festival 100g",  "price": 2500,  "stock": 55, "threshold": 10},
    {"code": "SNK-008", "name": "Bon Bon Bum Fresa",       "price": 500,   "stock": 200,"threshold": 30},

    # Lácteos
    {"code": "LAC-001", "name": "Leche Alquería 1L",       "price": 4200,  "stock": 30, "threshold": 5},
    {"code": "LAC-002", "name": "Yogurt Alpina 200g",      "price": 2800,  "stock": 40, "threshold": 8},
    {"code": "LAC-003", "name": "Queso Campesino 250g",    "price": 5500,  "stock": 20, "threshold": 4},
    {"code": "LAC-004", "name": "Kumis Alpina 200ml",      "price": 2500,  "stock": 35, "threshold": 6},

    # Panadería
    {"code": "PAN-001", "name": "Pan Tajado Bimbo",        "price": 6500,  "stock": 25, "threshold": 5},
    {"code": "PAN-002", "name": "Croissant",               "price": 3500,  "stock": 30, "threshold": 5},
    {"code": "PAN-003", "name": "Pandebono x3",            "price": 4000,  "stock": 20, "threshold": 4},
    {"code": "PAN-004", "name": "Almojábana x3",           "price": 3500,  "stock": 20, "threshold": 4},

    # Aseo personal
    {"code": "ASE-001", "name": "Jabón Protex 125g",       "price": 3800,  "stock": 40, "threshold": 8},
    {"code": "ASE-002", "name": "Shampoo Head&Shoulders",  "price": 18000, "stock": 15, "threshold": 3},
    {"code": "ASE-003", "name": "Desodorante Axe 150ml",   "price": 12000, "stock": 20, "threshold": 4},
    {"code": "ASE-004", "name": "Crema Dental Colgate",    "price": 8500,  "stock": 25, "threshold": 5},
    {"code": "ASE-005", "name": "Papel Higiénico x4",      "price": 7500,  "stock": 30, "threshold": 6},

    # Hogar
    {"code": "HOG-001", "name": "Detergente Ariel 500g",   "price": 9500,  "stock": 20, "threshold": 4},
    {"code": "HOG-002", "name": "Jabón Loza Axión 500g",   "price": 5500,  "stock": 25, "threshold": 5},
    {"code": "HOG-003", "name": "Suavizante Downy 500ml",  "price": 8000,  "stock": 18, "threshold": 3},

    # Cigarrillos y miscelánea
    {"code": "MIS-001", "name": "Cigarrillos Marlboro x10","price": 5000,  "stock": 50, "threshold": 10},
    {"code": "MIS-002", "name": "Encendedor BIC",          "price": 3000,  "stock": 40, "threshold": 8},
    {"code": "MIS-003", "name": "Pilas AA x2",             "price": 4500,  "stock": 30, "threshold": 6},
    {"code": "MIS-004", "name": "Bolsa Plástica x10",      "price": 1000,  "stock": 100,"threshold": 20},

    # Enlatados
    {"code": "ENL-001", "name": "Atún Van Camps 170g",     "price": 4800,  "stock": 35, "threshold": 7},
    {"code": "ENL-002", "name": "Sardinas Deli 125g",      "price": 3500,  "stock": 30, "threshold": 6},
    {"code": "ENL-003", "name": "Frijoles La Constancia",  "price": 3200,  "stock": 25, "threshold": 5},

    # Condimentos
    {"code": "CON-001", "name": "Sal Refisal 500g",        "price": 1800,  "stock": 40, "threshold": 8},
    {"code": "CON-002", "name": "Azúcar Riopaila 500g",    "price": 2500,  "stock": 35, "threshold": 7},
    {"code": "CON-003", "name": "Aceite Girasol 500ml",    "price": 8500,  "stock": 20, "threshold": 4},
    {"code": "CON-004", "name": "Salsa de Tomate Fruco",   "price": 5500,  "stock": 25, "threshold": 5},
    {"code": "CON-005", "name": "Mayonesa Fruco 200g",     "price": 6000,  "stock": 22, "threshold": 4},

    # Frutas y verduras empacadas
    {"code": "FRU-001", "name": "Banano x5",               "price": 3000,  "stock": 30, "threshold": 5},
    {"code": "FRU-002", "name": "Manzana x3",              "price": 4500,  "stock": 25, "threshold": 5},
    {"code": "FRU-003", "name": "Naranja x6",              "price": 3500,  "stock": 28, "threshold": 5},

    # Medicamentos básicos
    {"code": "MED-001", "name": "Acetaminofén 500mg x10",  "price": 3500,  "stock": 30, "threshold": 6},
    {"code": "MED-002", "name": "Ibuprofeno 400mg x10",    "price": 4500,  "stock": 25, "threshold": 5},
    {"code": "MED-003", "name": "Suero Oral Pedialyte",    "price": 5500,  "stock": 20, "threshold": 4},
]


def limpiar_tabla(table):
    """Elimina todos los items existentes en la tabla."""
    print("Limpiando tabla existente...")
    scan = table.scan()
    items = scan.get("Items", [])
    eliminados = 0
    with table.batch_writer() as batch:
        for item in items:
            batch.delete_item(Key={"id": item["id"]})
            eliminados += 1
    print(f"  {eliminados} items eliminados.\n")


def seed_products():
    dynamodb = boto3.resource("dynamodb", region_name=REGION)
    table    = dynamodb.Table(TABLE_NAME)

    print(f"Conectando a DynamoDB tabla '{TABLE_NAME}' en {REGION}...")

    # Limpiar primero
    limpiar_tabla(table)

    print(f"Cargando {len(PRODUCTS)} productos...\n")

    success = 0
    errors  = 0

    with table.batch_writer() as batch:
        for p in PRODUCTS:
            try:
                item = {
                    "id":                  str(uuid.uuid4()),
                    "code":                p["code"],
                    "name":                p["name"],
                    "price":               Decimal(str(p["price"])),
                    "stock_level":         p["stock"],
                    "low_stock_threshold": p["threshold"]
                }
                batch.put_item(Item=item)
                print(f"  ✓ [{p['code']}] {p['name']} - ${p['price']:,}")
                success += 1
            except Exception as e:
                print(f"  ✗ Error en {p['code']}: {e}")
                errors += 1

    print(f"\n{'='*50}")
    print(f"Carga completada: {success} exitosos, {errors} errores")
    print(f"Tabla: {TABLE_NAME}")
    print(f"Región: {REGION}")
    print(f"{'='*50}")


if __name__ == "__main__":
    seed_products()
