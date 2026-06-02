import type { Usuario } from '@domain/ports/IAuthPort';

/**
 * Mock de datos de autenticación para testing
 */

export const mockUsuarioCajero: Usuario = {
  id: 'user-001',
  nombre: 'Juan Pérez',
  rol: 'CAJERO',
  turno: '2024-01-15',
};

export const mockUsuarioAdmin: Usuario = {
  id: 'admin-001',
  nombre: 'María Admin',
  rol: 'ADMIN',
  turno: '2024-01-15',
};

export const mockUsuarios = [mockUsuarioCajero, mockUsuarioAdmin];
