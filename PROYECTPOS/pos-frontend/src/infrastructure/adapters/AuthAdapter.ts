import type { IAuthPort, Usuario } from '@domain/ports/IAuthPort';

/**
 * AuthAdapter - Implementación mock del port de autenticación
 * 
 * NOTA: Esta es una implementación placeholder.
 * En producción, esto debería conectarse a un servicio de autenticación real
 * (Cognito, Auth0, endpoint /auth, etc.)
 */
export class AuthAdapter implements IAuthPort {
  private currentUser: Usuario | null = null;

  async login(usuario: string, _password: string): Promise<Usuario> {
    // Mock: Siempre permite login (para desarrollo)
    // TODO: Implementar autenticación real contra API
    this.currentUser = {
      id: `user-${Date.now()}`,
      nombre: usuario,
      rol: usuario.toLowerCase().includes('admin') ? 'ADMIN' : 'CAJERO',
      turno: new Date().toISOString().split('T')[0],
    };

    return this.currentUser;
  }

  async logout(): Promise<void> {
    this.currentUser = null;
  }

  async getCurrentUser(): Promise<Usuario | null> {
    return this.currentUser;
  }
}
