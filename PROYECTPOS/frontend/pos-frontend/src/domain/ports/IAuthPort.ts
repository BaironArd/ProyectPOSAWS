/**
 * IAuthPort - Port para autenticación de usuarios (cajeros, administradores)
 * 
 * Actualmente no implementado en la versión actual, pero documentado
 * para futuras expansiones del sistema POS.
 */

export interface Usuario {
  id: string;
  nombre: string;
  rol: 'CAJERO' | 'ADMIN';
  turno?: string;
}

export interface IAuthPort {
  /**
   * Iniciar sesión de cajero
   * @param usuario - Nombre de usuario
   * @param password - Contraseña
   * @returns Usuario autenticado
   */
  login(usuario: string, password: string): Promise<Usuario>;

  /**
   * Cerrar sesión actual
   */
  logout(): Promise<void>;

  /**
   * Obtener usuario actualmente autenticado
   * @returns Usuario o null si no hay sesión activa
   */
  getCurrentUser(): Promise<Usuario | null>;
}
