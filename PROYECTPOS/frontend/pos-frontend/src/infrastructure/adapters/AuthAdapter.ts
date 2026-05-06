import type { IAuthPort } from '@domain/ports/IAuthPort';
import type { Sesion } from '@domain/types/POSState';

const API_BASE = import.meta.env.VITE_API_BASE_URL as string;

export class AuthAdapter implements IAuthPort {
  async login(usuario: string, contrasena: string): Promise<Sesion> {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ usuario, contrasena }),
    });
    if (!res.ok) throw new Error('CREDENCIALES_INVALIDAS');
    return res.json() as Promise<Sesion>;
  }

  async logout(token: string): Promise<void> {
    await fetch(`${API_BASE}/auth/logout`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
    });
  }
}

export const authAdapter = new AuthAdapter();
