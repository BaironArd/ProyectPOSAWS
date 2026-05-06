import type { IAuthPort } from '@domain/ports/IAuthPort';
import type { Sesion } from '@domain/types/POSState';

const USUARIOS: Record<string, { password: string; rol: 'CAJERO' | 'ADMIN' }> = {
  cajero01: { password: '1234', rol: 'CAJERO' },
  cajero02: { password: '1234', rol: 'CAJERO' },
  admin01:  { password: 'admin123', rol: 'ADMIN' },
};

export class AuthMock implements IAuthPort {
  async login(usuario: string, contrasena: string): Promise<Sesion> {
    await new Promise((r) => setTimeout(r, 400));
    const user = USUARIOS[usuario];
    if (!user || user.password !== contrasena) {
      throw new Error('CREDENCIALES_INVALIDAS');
    }
    return {
      usuario,
      rol: user.rol,
      token: `mock-jwt-${usuario}-${Date.now()}`,
    };
  }

  async logout(_token: string): Promise<void> {
    await new Promise((r) => setTimeout(r, 200));
  }
}

export const authMock = new AuthMock();
