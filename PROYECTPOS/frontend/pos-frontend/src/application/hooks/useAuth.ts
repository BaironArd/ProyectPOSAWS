import { useState } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import type { IAuthPort } from '@domain/ports/IAuthPort';
import { PosApiError } from '@domain/errors/PosApiError';
import { mensajeErrorApi } from '@domain/errors/errorMessages';

export function useAuth(authPort: IAuthPort) {
  const [cargando, setCargando] = useState(false);
  const [errorLogin, setErrorLogin] = useState<string | null>(null);

  const loginStore = usePOSStore((s) => s.login);
  const logoutStore = usePOSStore((s) => s.logout);
  const sesion = usePOSStore((s) => s.sesion);

  async function login(usuario: string, contrasena: string) {
    setCargando(true);
    setErrorLogin(null);
    try {
      const sesionData = await authPort.login(usuario, contrasena);
      loginStore(sesionData);
    } catch (err) {
      if (err instanceof PosApiError) {
        setErrorLogin(mensajeErrorApi(err.codigo, err.message));
      } else {
        setErrorLogin(mensajeErrorApi('CREDENCIALES_INVALIDAS', 'Sin detalle'));
      }
    } finally {
      setCargando(false);
    }
  }

  async function logout() {
    if (sesion?.token) {
      try {
        await authPort.logout(sesion.token);
      } catch {
        // ignorar error de logout — limpiar sesión de todas formas
      }
    }
    logoutStore();
  }

  return { login, logout, cargando, errorLogin, sesion };
}
