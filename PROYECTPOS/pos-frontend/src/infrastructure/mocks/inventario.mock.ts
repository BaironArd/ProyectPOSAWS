/**
 * Mock de datos de inventario para testing
 */

export const mockProductosBajoStock = [
  {
    id: 'prod-015',
    nombre: 'Cable HDMI 2m',
    stock: 3,
  },
  {
    id: 'prod-022',
    nombre: 'Adaptador USB',
    stock: 2,
  },
  {
    id: 'prod-031',
    nombre: 'Mousepad Ergonómico',
    stock: 4,
  },
];

export const mockProductoNuevo = {
  id: 'prod-999',
  codigo: 'PERI-999',
  codigoBarras: '7501234567899',
  nombre: 'Producto de Prueba',
  precio: 50000,
  stock: 100,
  categoria: 'Periféricos',
  umbralBajoStock: 10,
};
