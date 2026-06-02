import type { VentaHistorial } from '@domain/ports/IHistorialPort';

/**
 * Mock de datos de historial de ventas para testing
 */

export const mockVentaHistorial: VentaHistorial = {
  ventaId: 'venta-001',
  fecha: '2024-01-15',
  hora: '10:30:00',
  total: 107100,
  metodoPago: 'EFECTIVO',
  cajero: 'Juan Pérez',
  items: [
    {
      nombre: 'Mouse Inalámbrico',
      cantidad: 2,
      subtotal: 90000,
    },
    {
      nombre: 'Cable USB-C',
      cantidad: 1,
      subtotal: 15000,
    },
  ],
};

export const mockVentasHistorial: VentaHistorial[] = [
  mockVentaHistorial,
  {
    ventaId: 'venta-002',
    fecha: '2024-01-15',
    hora: '11:45:00',
    total: 85000,
    metodoPago: 'TARJETA',
    cajero: 'Juan Pérez',
    items: [
      {
        nombre: 'Teclado Mecánico',
        cantidad: 1,
        subtotal: 85000,
      },
    ],
  },
  {
    ventaId: 'venta-003',
    fecha: '2024-01-15',
    hora: '14:20:00',
    total: 150000,
    metodoPago: 'EFECTIVO',
    cajero: 'Juan Pérez',
    items: [
      {
        nombre: 'Webcam HD',
        cantidad: 1,
        subtotal: 120000,
      },
      {
        nombre: 'Mousepad',
        cantidad: 2,
        subtotal: 30000,
      },
    ],
  },
];
