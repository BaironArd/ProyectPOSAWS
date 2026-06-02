/**
 * Barrel export de todos los mocks de datos para testing
 * 
 * Útiles para:
 * - Tests unitarios
 * - Desarrollo sin backend
 * - Documentación de estructuras de datos
 */

// Auth mocks
export {
  mockUsuarioCajero,
  mockUsuarioAdmin,
  mockUsuarios,
} from './auth.mock';

// Reportes mocks
export {
  mockReporteResumen,
  mockReporteDiario,
  mockTopProductos,
} from './reportes.mock';

// Historial mocks
export {
  mockVentaHistorial,
  mockVentasHistorial,
} from './historial.mock';

// Inventario mocks
export {
  mockProductosBajoStock,
  mockProductoNuevo,
} from './inventario.mock';

// Devolucion mocks
export {
  mockResultadoDevolucionExitosa,
  mockResultadoDevolucionPendiente,
  mockPuedeDevolver,
  mockNoPuedeDevolver,
} from './devolucion.mock';
