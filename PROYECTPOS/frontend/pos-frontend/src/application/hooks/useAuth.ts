import { useState, useCallback } from 'react';
import type { IAuthPort, Usuario } from '@domain/ports/IAuthPort';

/**
 * useAuth - Hook para gestionar autenticación de usuarios
 * 
 * Maneja login, logout y estado de usuario actual.
 * Actualmente es un placeholder para expansión futura.
 */
export function useAuth(authPort: IAuthPort) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const login = useCallback(
    async (nombreUsuario: string, password: string) => {
      setCargando(true);
      setError(null);
      try {
        const user = await authPort.login(nombreUsuario, password);
        setUsuario(user);
        return user;
      } catch (err) {
        const mensaje = err instanceof Error ? err.message : 'Error al iniciar sesión';
        setError(mensaje);
        throw err;
      } finally {
        setCargando(false);
      }
    },
    [authPort]
  );

  const logout = useCallback(async () => {
    setCargando(true);
    try {
      await authPort.logout();
      setUsuario(null);
      setError(null);
    } catch (err) {
      console.error('Error al cerrar sesión:', err);
    } finally {
      setCargando(false);
    }
  }, [authPort]);

  const verificarSesion = useCallback(async () => {
    setCargando(true);
    try {
      const user = await authPort.getCurrentUser();
      setUsuario(user);
    } catch (err) {
      console.error('Error al verificar sesión:', err);
      setUsuario(null);
    } finally {
      setCargando(false);
    }
  }, [authPort]);

  return {
    usuario,
    cargando,
    error,
    login,
    logout,
    verificarSesion,
    estaAutenticado: usuario !== null,
    esAdmin: usuario?.rol === 'ADMIN',
  };
}
