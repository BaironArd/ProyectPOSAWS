/**
 * Barrel export de todos los ports del dominio
 * 
 * Ports activos (implementados):
 * - IProductoPort: Búsqueda de productos
 * - IVentaPort: Confirmación de ventas
 * - IImpresionPort: Impresión de recibos
 * 
 * Ports placeholder (para expansión futura):
 * - IAuthPort: Autenticación de usuarios
 * - IInventarioPort: Gestión de inventario (Admin)
 * - IReportesPort: Generación de reportes
 * - IHistorialPort: Consulta de historial de ventas
 * - IDevolucionPort: Procesamiento de devoluciones
 */

// Ports activos
export type { IProductoPort } from './IProductoPort';
export type { IVentaPort, ConfirmarVentaPayload, ConfirmarVentaResult } from './IVentaPort';
export type { IImpresionPort } from './IImpresionPort';

// Ports placeholder
export type { IAuthPort, Usuario } from './IAuthPort';
export type {
  IInventarioPort,
  ActualizarStockPayload,
  CrearProductoPayload,
} from './IInventarioPort';
export type {
  IReportesPort,
  ReporteDiario,
  ReporteResumen,
  TopProducto,
} from './IReportesPort';
export type { IHistorialPort, VentaHistorial } from './IHistorialPort';
export type {
  IDevolucionPort,
  ProcesarDevolucionPayload,
  ResultadoDevolucion,
} from './IDevolucionPort';
