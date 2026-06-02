/**
 * Barrel export de todos los hooks de la aplicación
 * 
 * Hooks activos (implementados y en uso):
 * - useSearch: Búsqueda de productos
 * - usePayment: Procesamiento de pagos
 * - useReceipt: Gestión de recibos
 * - useFocusManager: Gestión de foco de inputs
 * - useKeyboardShortcuts: Atajos de teclado
 * - useCart: Gestión del carrito
 * 
 * Hooks placeholder (para expansión futura):
 * - useAuth: Autenticación de usuarios
 * - useInventario: Gestión de inventario (Admin)
 * - useReportes: Generación de reportes
 * - useHistorial: Consulta de historial
 * - useDevolucion: Procesamiento de devoluciones
 */

// Hooks activos
export { useSearch } from './useSearch';
export { usePayment } from './usePayment';
export { useReceipt } from './useReceipt';
export { useFocusManager } from './useFocusManager';
export { useKeyboardShortcuts } from './useKeyboardShortcuts';
export { useCart } from './useCart';

// Hooks placeholder
export { useAuth } from './useAuth';
export { useInventario } from './useInventario';
export { useReportes } from './useReportes';
export { useHistorial } from './useHistorial';
export { useDevolucion } from './useDevolucion';
