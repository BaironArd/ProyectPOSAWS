import type { ReporteDiario, ReporteResumen, TopProducto } from '@domain/ports/IReportesPort';

/**
 * Mock de datos de reportes para testing
 */

export const mockReporteResumen: ReporteResumen = {
  totalVentas: 5,
  subtotal: 450000,
  iva: 85500,
  total: 535500,
  ventasEfectivo: 300000,
  ventasTarjeta: 235500,
  cantidadVentas: 5,
};

export const mockReporteDiario: ReporteDiario = {
  fecha: '2024-01-15',
  ventas: [
    {
      ventaId: 'venta-001',
      hora: '10:30:00',
      total: 107100,
      metodoPago: 'EFECTIVO',
    },
    {
      ventaId: 'venta-002',
      hora: '11:45:00',
      total: 85000,
      metodoPago: 'TARJETA',
    },
  ],
  resumen: mockReporteResumen,
};

export const mockTopProductos: TopProducto[] = [
  {
    productoId: 'prod-001',
    nombre: 'Mouse Inalámbrico',
    cantidadVendida: 15,
    totalIngresos: 675000,
  },
  {
    productoId: 'prod-002',
    nombre: 'Teclado Mecánico',
    cantidadVendida: 8,
    totalIngresos: 720000,
  },
  {
    productoId: 'prod-003',
    nombre: 'Monitor 24"',
    cantidadVendida: 3,
    totalIngresos: 900000,
  },
];
