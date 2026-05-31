"""
seed-products.py
Carga 50 productos tecnológicos en la tabla ProductosTable de DynamoDB.

Estructura de cada item (formato que lee la Lambda Java via DynamoDbBean):
{
    "id":   String (PK - UUID),
    "code": String (GSI code-index),
    "producto": {
        "name":               String,
        "price":              Number,
        "stock_level":        Number,
        "low_stock_threshold": Number
    }
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
    # Laptops y computadores
    {"code": "LAP-001", "name": "Laptop Lenovo IdeaPad 15",       "price": 2800000, "stock": 10, "threshold": 2},
    {"code": "LAP-002", "name": "Laptop HP Pavilion 14",          "price": 2500000, "stock": 8,  "threshold": 2},
    {"code": "LAP-003", "name": "MacBook Air M2 13",              "price": 6500000, "stock": 5,  "threshold": 1},
    {"code": "LAP-004", "name": "Laptop Asus VivoBook 15",        "price": 2200000, "stock": 12, "threshold": 3},
    {"code": "LAP-005", "name": "Laptop Dell Inspiron 15",        "price": 3100000, "stock": 7,  "threshold": 2},

    # Smartphones
    {"code": "CEL-001", "name": "Samsung Galaxy A54 128GB",       "price": 1350000, "stock": 20, "threshold": 5},
    {"code": "CEL-002", "name": "iPhone 14 128GB Negro",          "price": 4200000, "stock": 8,  "threshold": 2},
    {"code": "CEL-003", "name": "Xiaomi Redmi Note 12 128GB",     "price": 850000,  "stock": 25, "threshold": 5},
    {"code": "CEL-004", "name": "Motorola Moto G84 256GB",        "price": 980000,  "stock": 18, "threshold": 4},
    {"code": "CEL-005", "name": "Samsung Galaxy S23 256GB",       "price": 3200000, "stock": 6,  "threshold": 2},

    # Tablets
    {"code": "TAB-001", "name": "iPad 10ma Gen 64GB WiFi",        "price": 2100000, "stock": 7,  "threshold": 2},
    {"code": "TAB-002", "name": "Samsung Galaxy Tab A8 64GB",     "price": 950000,  "stock": 10, "threshold": 3},
    {"code": "TAB-003", "name": "Lenovo Tab M10 Plus 128GB",      "price": 780000,  "stock": 12, "threshold": 3},

    # Periféricos - Teclados y ratones
    {"code": "PER-001", "name": "Teclado Mecánico Redragon K552", "price": 185000,  "stock": 30, "threshold": 8},
    {"code": "PER-002", "name": "Mouse Logitech MX Master 3",     "price": 320000,  "stock": 20, "threshold": 5},
    {"code": "PER-003", "name": "Mouse Inalámbrico HP 220",       "price": 65000,   "stock": 40, "threshold": 10},
    {"code": "PER-004", "name": "Teclado Inalámbrico Logitech K380","price": 145000, "stock": 25, "threshold": 6},
    {"code": "PER-005", "name": "Combo Teclado+Mouse Logitech MK235","price": 120000,"stock": 35, "threshold": 8},

    # Monitores
    {"code": "MON-001", "name": "Monitor LG 24 FHD IPS 75Hz",    "price": 680000,  "stock": 10, "threshold": 3},
    {"code": "MON-002", "name": "Monitor Samsung 27 QHD 144Hz",   "price": 1250000, "stock": 6,  "threshold": 2},
    {"code": "MON-003", "name": "Monitor AOC 22 FHD 75Hz",        "price": 520000,  "stock": 12, "threshold": 3},

    # Auriculares y audio
    {"code": "AUD-001", "name": "Audífonos Sony WH-1000XM5",      "price": 1450000, "stock": 8,  "threshold": 2},
    {"code": "AUD-002", "name": "Audífonos JBL Tune 510BT",       "price": 185000,  "stock": 25, "threshold": 6},
    {"code": "AUD-003", "name": "Audífonos Xiaomi Redmi Buds 4",  "price": 95000,   "stock": 30, "threshold": 8},
    {"code": "AUD-004", "name": "Parlante JBL Flip 6 Bluetooth",  "price": 480000,  "stock": 15, "threshold": 4},
    {"code": "AUD-005", "name": "Parlante Portátil Anker Soundcore","price": 145000, "stock": 20, "threshold": 5},

    # Almacenamiento
    {"code": "ALM-001", "name": "SSD Samsung 870 EVO 500GB",      "price": 280000,  "stock": 20, "threshold": 5},
    {"code": "ALM-002", "name": "Disco Duro Externo WD 1TB USB3", "price": 220000,  "stock": 18, "threshold": 5},
    {"code": "ALM-003", "name": "Memoria USB Kingston 64GB 3.0",  "price": 35000,   "stock": 60, "threshold": 15},
    {"code": "ALM-004", "name": "Memoria MicroSD SanDisk 128GB",  "price": 55000,   "stock": 50, "threshold": 12},
    {"code": "ALM-005", "name": "SSD NVMe Kingston 1TB M.2",      "price": 380000,  "stock": 15, "threshold": 4},

    # Redes y conectividad
    {"code": "RED-001", "name": "Router TP-Link Archer AX23 WiFi6","price": 320000, "stock": 12, "threshold": 3},
    {"code": "RED-002", "name": "Switch TP-Link 8 Puertos Gigabit","price": 95000,  "stock": 15, "threshold": 4},
    {"code": "RED-003", "name": "Adaptador WiFi USB TP-Link N150", "price": 35000,  "stock": 40, "threshold": 10},
    {"code": "RED-004", "name": "Cable HDMI 2.0 4K 2 metros",     "price": 28000,   "stock": 50, "threshold": 12},
    {"code": "RED-005", "name": "Hub USB-C 7 en 1 Anker",         "price": 145000,  "stock": 20, "threshold": 5},

    # Cámaras y fotografía
    {"code": "CAM-001", "name": "Cámara Web Logitech C920 FHD",   "price": 380000,  "stock": 10, "threshold": 3},
    {"code": "CAM-002", "name": "Cámara Web Xiaomi 1080p USB",     "price": 95000,   "stock": 20, "threshold": 5},
    {"code": "CAM-003", "name": "Trípode Flexible Gorilla Pod",    "price": 65000,   "stock": 25, "threshold": 6},

    # Cargadores y cables
    {"code": "CAR-001", "name": "Cargador USB-C 65W GaN Anker",   "price": 125000,  "stock": 30, "threshold": 8},
    {"code": "CAR-002", "name": "Cable USB-C a USB-C 1m Anker",   "price": 35000,   "stock": 50, "threshold": 12},
    {"code": "CAR-003", "name": "Power Bank 20000mAh Xiaomi",      "price": 185000,  "stock": 20, "threshold": 5},
    {"code": "CAR-004", "name": "Cargador Inalámbrico 15W Samsung","price": 95000,   "stock": 25, "threshold": 6},

    # Impresoras y accesorios
    {"code": "IMP-001", "name": "Impresora HP DeskJet 2775 WiFi", "price": 380000,  "stock": 8,  "threshold": 2},
    {"code": "IMP-002", "name": "Tóner HP 85A Negro LaserJet",    "price": 145000,  "stock": 15, "threshold": 4},
    {"code": "IMP-003", "name": "Papel Fotográfico A4 Glossy x50","price": 28000,   "stock": 40, "threshold": 10},

    # Accesorios varios
    {"code": "ACC-001", "name": "Soporte Laptop Ajustable Aluminio","price": 85000,  "stock": 25, "threshold": 6},
    {"code": "ACC-002", "name": "Mousepad XL Gaming Redragon",     "price": 55000,   "stock": 35, "threshold": 8},
    {"code": "ACC-003", "name": "Limpiador Pantallas Kit 3 en 1",  "price": 22000,   "stock": 60, "threshold": 15},
    {"code": "ACC-004", "name": "Funda Laptop 15 Impermeable",     "price": 45000,   "stock": 30, "threshold": 8},
    {"code": "ACC-005", "name": "Regleta 6 Tomas con USB 2m",      "price": 65000,   "stock": 25, "threshold": 6},
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

    print(f"Cargando {len(PRODUCTS)} productos tecnológicos...\n")

    success = 0
    errors  = 0

    with table.batch_writer() as batch:
        for p in PRODUCTS:
            try:
                item = {
                    "id":   str(uuid.uuid4()),
                    "code": p["code"],
                    "producto": {
                        "name":               p["name"],
                        "price":              Decimal(str(p["price"])),
                        "stock_level":        p["stock"],
                        "low_stock_threshold": p["threshold"]
                    }
                }
                batch.put_item(Item=item)
                print(f"  ✓ [{p['code']}] {p['name']} - ${p['price']:,}")
                success += 1
            except Exception as e:
                print(f"  ✗ Error en {p['code']}: {e}")
                errors += 1

    print(f"\n{'='*55}")
    print(f"Carga completada: {success} exitosos, {errors} errores")
    print(f"Tabla: {TABLE_NAME}")
    print(f"Región: {REGION}")
    print(f"{'='*55}")


if __name__ == "__main__":
    seed_products()
