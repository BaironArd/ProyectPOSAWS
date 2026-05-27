"""
seed-products.py
Carga 50 productos de prueba en la tabla ProductosTable de DynamoDB.

Uso:
    python pos-sam/seed-products.py

Requisitos:
    pip install boto3
    aws configure  (credenciales configuradas)

La estructura de cada item es exactamente la que pide el profesor:
{
    "id":      String (PK),
    "code":    String (GSI code-index),
    "producto": {
        "name":                String,
        "price":               Number,
        "stock_level":         Number,
        "low_stock_threshold": Number
    }
}
"""

import boto3
import uuid
import json
from decimal import Decimal

# ── Configuración ─────────────────────────────────────────────────────────────
TABLE_NAME = "ProductosTable"
REGION     = "us-east-1"

# ── 50 productos de prueba ────────────────────────────────────────────────────
PRODUCTS = [
    # Periféricos
    {"code": "PERI-001", "name": "Mouse Inalámbrico Logitech",     "price": 45000,  "stock": 25, "threshold": 5},
    {"code": "PERI-002", "name": "Teclado Mecánico RGB",           "price": 89000,  "stock": 15, "threshold": 3},
    {"code": "PERI-003", "name": "Audífonos Bluetooth Sony",       "price": 120000, "stock": 10, "threshold": 2},
    {"code": "PERI-004", "name": "Webcam Full HD 1080p",           "price": 65000,  "stock": 12, "threshold": 3},
    {"code": "PERI-005", "name": "Mouse Pad XL Gaming",            "price": 25000,  "stock": 30, "threshold": 5},
    {"code": "PERI-006", "name": "Hub USB-C 7 puertos",            "price": 55000,  "stock": 20, "threshold": 4},
    {"code": "PERI-007", "name": "Micrófono USB Condensador",      "price": 95000,  "stock": 8,  "threshold": 2},
    {"code": "PERI-008", "name": "Teclado Inalámbrico Slim",       "price": 48000,  "stock": 18, "threshold": 4},
    {"code": "PERI-009", "name": "Mouse Ergonómico Vertical",      "price": 72000,  "stock": 14, "threshold": 3},
    {"code": "PERI-010", "name": "Auriculares con Micrófono",      "price": 38000,  "stock": 22, "threshold": 5},

    # Almacenamiento
    {"code": "STOR-001", "name": "SSD Externo 1TB Samsung",        "price": 185000, "stock": 8,  "threshold": 2},
    {"code": "STOR-002", "name": "Memoria USB 64GB Kingston",      "price": 22000,  "stock": 40, "threshold": 8},
    {"code": "STOR-003", "name": "Disco Duro Externo 2TB WD",      "price": 145000, "stock": 6,  "threshold": 2},
    {"code": "STOR-004", "name": "Memoria RAM DDR4 8GB",           "price": 78000,  "stock": 12, "threshold": 3},
    {"code": "STOR-005", "name": "Tarjeta MicroSD 128GB",          "price": 35000,  "stock": 25, "threshold": 5},
    {"code": "STOR-006", "name": "SSD NVMe 500GB",                 "price": 165000, "stock": 7,  "threshold": 2},
    {"code": "STOR-007", "name": "Memoria USB 32GB Sandisk",       "price": 15000,  "stock": 50, "threshold": 10},
    {"code": "STOR-008", "name": "Disco Duro Externo 1TB Seagate", "price": 115000, "stock": 9,  "threshold": 2},

    # Cables y adaptadores
    {"code": "CABL-001", "name": "Cable HDMI 2.0 2m",             "price": 18000,  "stock": 35, "threshold": 7},
    {"code": "CABL-002", "name": "Cable USB-C a USB-A 1m",        "price": 12000,  "stock": 45, "threshold": 10},
    {"code": "CABL-003", "name": "Adaptador HDMI a VGA",          "price": 22000,  "stock": 20, "threshold": 4},
    {"code": "CABL-004", "name": "Cable DisplayPort 1.4 2m",      "price": 28000,  "stock": 15, "threshold": 3},
    {"code": "CABL-005", "name": "Adaptador USB-C a HDMI",        "price": 35000,  "stock": 18, "threshold": 4},
    {"code": "CABL-006", "name": "Cable Ethernet Cat6 3m",        "price": 14000,  "stock": 30, "threshold": 6},
    {"code": "CABL-007", "name": "Regleta 6 tomas con USB",       "price": 42000,  "stock": 16, "threshold": 3},

    # Accesorios de laptop
    {"code": "LAPT-001", "name": "Soporte Laptop Aluminio",       "price": 55000,  "stock": 12, "threshold": 3},
    {"code": "LAPT-002", "name": "Mochila Laptop 15.6\"",         "price": 85000,  "stock": 10, "threshold": 2},
    {"code": "LAPT-003", "name": "Funda Neopreno 14\"",           "price": 28000,  "stock": 20, "threshold": 4},
    {"code": "LAPT-004", "name": "Cargador Universal 65W USB-C",  "price": 72000,  "stock": 8,  "threshold": 2},
    {"code": "LAPT-005", "name": "Cooling Pad con 4 ventiladores","price": 48000,  "stock": 11, "threshold": 3},
    {"code": "LAPT-006", "name": "Candado de Seguridad Kensington","price": 32000, "stock": 14, "threshold": 3},

    # Redes
    {"code": "NETW-001", "name": "Router WiFi 6 TP-Link",         "price": 195000, "stock": 5,  "threshold": 1},
    {"code": "NETW-002", "name": "Switch 8 puertos Gigabit",      "price": 85000,  "stock": 7,  "threshold": 2},
    {"code": "NETW-003", "name": "Adaptador WiFi USB 600Mbps",    "price": 38000,  "stock": 15, "threshold": 3},
    {"code": "NETW-004", "name": "Repetidor WiFi 300Mbps",        "price": 55000,  "stock": 9,  "threshold": 2},
    {"code": "NETW-005", "name": "Tarjeta de Red PCIe Gigabit",   "price": 42000,  "stock": 8,  "threshold": 2},

    # Impresión
    {"code": "PRNT-001", "name": "Cartucho Tinta Negro HP 664",   "price": 28000,  "stock": 20, "threshold": 5},
    {"code": "PRNT-002", "name": "Cartucho Tinta Color HP 664",   "price": 32000,  "stock": 18, "threshold": 4},
    {"code": "PRNT-003", "name": "Papel Fotográfico A4 50 hojas", "price": 22000,  "stock": 25, "threshold": 5},
    {"code": "PRNT-004", "name": "Tóner Samsung MLT-D101S",       "price": 65000,  "stock": 8,  "threshold": 2},

    # Energía
    {"code": "ENRG-001", "name": "UPS 600VA APC",                 "price": 185000, "stock": 4,  "threshold": 1},
    {"code": "ENRG-002", "name": "Batería Portátil 20000mAh",     "price": 75000,  "stock": 12, "threshold": 3},
    {"code": "ENRG-003", "name": "Cargador Inalámbrico 15W",      "price": 45000,  "stock": 16, "threshold": 4},
    {"code": "ENRG-004", "name": "Batería Portátil 10000mAh",     "price": 48000,  "stock": 14, "threshold": 3},

    # Monitores y video
    {"code": "DISP-001", "name": "Monitor LED 24\" Full HD",      "price": 485000, "stock": 3,  "threshold": 1},
    {"code": "DISP-002", "name": "Soporte Monitor Articulado",    "price": 95000,  "stock": 6,  "threshold": 2},
    {"code": "DISP-003", "name": "Filtro Privacidad 15.6\"",      "price": 55000,  "stock": 8,  "threshold": 2},

    # Seguridad
    {"code": "SECU-001", "name": "Cámara IP WiFi 1080p",          "price": 125000, "stock": 5,  "threshold": 1},
    {"code": "SECU-002", "name": "Lector Huella Digital USB",     "price": 68000,  "stock": 7,  "threshold": 2},
]


def seed_products():
    """Carga los 50 productos en DynamoDB."""
    dynamodb = boto3.resource("dynamodb", region_name=REGION)
    table    = dynamodb.Table(TABLE_NAME)

    print(f"Conectando a DynamoDB tabla '{TABLE_NAME}' en {REGION}...")
    print(f"Cargando {len(PRODUCTS)} productos...\n")

    success = 0
    errors  = 0

    with table.batch_writer() as batch:
        for p in PRODUCTS:
            try:
                item = {
                    "id":   str(uuid.uuid4()),
                    "code": p["code"],
                    "producto": {
                        "name":                p["name"],
                        "price":               Decimal(str(p["price"])),
                        "stock_level":         p["stock"],
                        "low_stock_threshold": p["threshold"]
                    }
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
