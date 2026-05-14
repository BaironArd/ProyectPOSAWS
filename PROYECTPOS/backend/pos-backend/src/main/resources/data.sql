-- ============================================================
-- Datos iniciales — POS Backend
-- Solo inserta productos si la tabla está vacía (H2 en archivo, ddl-auto=update)
-- ============================================================

INSERT INTO productos (nombre, precio, stock, categoria, activo, version)
SELECT * FROM (VALUES
  ('Mouse Óptico USB',      30000, 15, 'Periféricos',    true, 0),
  ('Teclado Mecánico',      55000,  8, 'Periféricos',    true, 0),
  ('Monitor 24"',          450000,  3, 'Monitores',      true, 0),
  ('Audífonos Bluetooth',   85000, 12, 'Audio',          true, 0),
  ('Cable HDMI 2m',         15000, 25, 'Cables',         true, 0),
  ('Hub USB 4 puertos',     22000,  0, 'Periféricos',    true, 0),
  ('Webcam HD 1080p',      120000,  5, 'Periféricos',    true, 0),
  ('Mousepad XL',           18000, 20, 'Accesorios',     true, 0),
  ('Disco SSD 500GB',      180000,  7, 'Almacenamiento', true, 0),
  ('Memoria RAM 8GB',       95000, 10, 'Componentes',    true, 0),
  ('Laptop Stand',          45000,  6, 'Accesorios',     true, 0),
  ('Cargador USB-C 65W',    38000, 14, 'Cargadores',     true, 0)
) AS nuevos(nombre, precio, stock, categoria, activo, version)
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE productos.nombre = nuevos.nombre);

-- Usuarios de prueba: se crean automáticamente al arrancar via DataInitializer.java
-- cajero01 / 1234  (rol: CAJERO)
-- cajero02 / 1234  (rol: CAJERO)
-- admin01  / admin123 (rol: ADMIN)
