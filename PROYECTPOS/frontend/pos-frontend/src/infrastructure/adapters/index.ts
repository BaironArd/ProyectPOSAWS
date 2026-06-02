/**
 * Barrel export de todos los adapters de infraestructura
 * 
 * Adapters activos (implementados y conectados al API):
 * - ProductoAdapter: Consulta de productos desde API Gateway
 * - VentaAdapter: Confirmación de ventas a API Gateway
 * - ImpresionAdapter: Impresión de recibos (window.print)
 * 
 * Adapters placeholder (implementaciones mock para expansión futura):
 * - AuthAdapter: Autenticación (mock)
 * - InventarioAdapter: Gestión de inventario (mock)
 * - ReportesAdapter: Reportes (mock)
 * - HistorialAdapter: Historial de ventas (mock)
 * - DevolucionAdapter: Devoluciones (mock)
 */

// Adapters activos
export { ProductoAdapter } from './ProductoAdapter';
export { VentaAdapter } from './VentaAdapter';
export { ImpresionAdapter } from './ImpresionAdapter';

// Adapters placeholder (mock implementations)
export { AuthAdapter } from './AuthAdapter';
export { InventarioAdapter } from './InventarioAdapter';
export { ReportesAdapter } from './ReportesAdapter';
export { HistorialAdapter } from './HistorialAdapter';
export { DevolucionAdapter } from './DevolucionAdapter';
