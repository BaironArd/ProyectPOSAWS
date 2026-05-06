import { describe, it, expect, beforeEach } from 'vitest';
import { usePOSStore } from './usePOSStore';

function resetStore() {
  usePOSStore.setState({
    estado: 'LOGIN',
    sesion: null,
    query: '',
    productos: [],
    carrito: [],
    resumen: { subtotal: 0, iva: 0, total: 0 },
    metodoPago: null,
    pagos: [],
    montoPagado: 0,
    cambio: 0,
    historial: [],
    estadoPrevio: null,
    error: null,
    ventaIdActual: null,
  });
}

describe('usePOSStore — transiciones de estado', () => {
  beforeEach(resetStore);

  it('LOGIN → IDLE es válida (login exitoso)', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    expect(usePOSStore.getState().estado).toBe('IDLE');
  });

  it('IDLE → BUSCANDO es válida', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    usePOSStore.getState().setEstado('BUSCANDO');
    expect(usePOSStore.getState().estado).toBe('BUSCANDO');
  });

  it('IDLE → PROCESANDO es inválida (ignorada)', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    usePOSStore.getState().setEstado('PROCESANDO');
    expect(usePOSStore.getState().estado).toBe('IDLE');
  });

  it('RESULTADOS → CARRITO_ACTIVO al agregar primer ítem', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    usePOSStore.setState({ estado: 'RESULTADOS' });
    usePOSStore.getState().agregarAlCarrito({ id: 1, nombre: 'Mouse', precio: 30000, stock: 5 });
    expect(usePOSStore.getState().estado).toBe('CARRITO_ACTIVO');
  });

  it('CARRITO_ACTIVO → RESULTADOS automáticamente cuando carrito queda vacío', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    usePOSStore.setState({ estado: 'RESULTADOS' });
    usePOSStore.getState().agregarAlCarrito({ id: 1, nombre: 'Mouse', precio: 30000, stock: 5 });
    expect(usePOSStore.getState().estado).toBe('CARRITO_ACTIVO');
    usePOSStore.getState().modificarCantidad(1, 0);
    expect(usePOSStore.getState().estado).toBe('RESULTADOS');
    expect(usePOSStore.getState().carrito).toHaveLength(0);
  });

  it('logout resetea el estado completamente', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    usePOSStore.getState().agregarAlCarrito({ id: 1, nombre: 'Mouse', precio: 30000, stock: 5 });
    usePOSStore.getState().logout();
    const s = usePOSStore.getState();
    expect(s.estado).toBe('LOGIN');
    expect(s.sesion).toBeNull();
    expect(s.carrito).toHaveLength(0);
  });

  it('setError transiciona a ERROR', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    usePOSStore.getState().setError({ codigo: 'TEST', mensaje: 'Error de prueba' });
    expect(usePOSStore.getState().estado).toBe('ERROR');
    expect(usePOSStore.getState().error?.codigo).toBe('TEST');
  });

  it('clearError limpia el error y regresa al estado previo', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    usePOSStore.setState({ estadoPrevio: 'RESULTADOS' });
    usePOSStore.getState().setError({ codigo: 'TEST', mensaje: 'Error' });
    usePOSStore.getState().clearError();
    expect(usePOSStore.getState().estado).toBe('RESULTADOS');
    expect(usePOSStore.getState().error).toBeNull();
  });

  it('verHistorial guarda estadoPrevio y transiciona a HISTORIAL', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    usePOSStore.getState().verHistorial();
    expect(usePOSStore.getState().estado).toBe('HISTORIAL');
    expect(usePOSStore.getState().estadoPrevio).toBe('IDLE');
  });

  it('volverDeHistorial restaura el estado previo', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    usePOSStore.setState({ estado: 'RESULTADOS' });
    usePOSStore.getState().verHistorial();
    usePOSStore.getState().volverDeHistorial();
    expect(usePOSStore.getState().estado).toBe('RESULTADOS');
  });
});

describe('usePOSStore — carrito', () => {
  beforeEach(resetStore);

  it('agregar mismo producto incrementa cantidad, no crea fila nueva', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    usePOSStore.setState({ estado: 'RESULTADOS' });
    const p = { id: 1, nombre: 'Mouse', precio: 30000, stock: 5 };
    usePOSStore.getState().agregarAlCarrito(p);
    usePOSStore.getState().agregarAlCarrito(p);
    const carrito = usePOSStore.getState().carrito;
    expect(carrito).toHaveLength(1);
    expect(carrito[0]?.cantidad).toBe(2);
    expect(carrito[0]?.subtotal).toBe(60000);
  });

  it('no agrega producto con stock 0', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    usePOSStore.setState({ estado: 'RESULTADOS' });
    usePOSStore.getState().agregarAlCarrito({ id: 1, nombre: 'Mouse', precio: 30000, stock: 0 });
    expect(usePOSStore.getState().carrito).toHaveLength(0);
  });

  it('resumen se actualiza reactivamente', () => {
    usePOSStore.getState().login({ usuario: 'cajero01', rol: 'CAJERO', token: 'tok' });
    usePOSStore.setState({ estado: 'RESULTADOS' });
    usePOSStore.getState().agregarAlCarrito({ id: 1, nombre: 'Mouse', precio: 100000, stock: 5 });
    const r = usePOSStore.getState().resumen;
    expect(r.subtotal).toBe(100000);
    expect(r.iva).toBe(19000);
    expect(r.total).toBe(119000);
  });
});
