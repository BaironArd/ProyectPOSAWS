import { useState, type FormEvent } from 'react';
import type { IAuthPort } from '@domain/ports/IAuthPort';
import { useAuth } from '@application/hooks/useAuth';
import styles from './LoginForm.module.css';

interface Props {
  authPort: IAuthPort;
}

export function LoginForm({ authPort }: Props) {
  const [usuario, setUsuario] = useState('');
  const [contrasena, setContrasena] = useState('');
  const { login, cargando, errorLogin } = useAuth(authPort);

  const puedeIngresar = usuario.trim().length > 0 && contrasena.trim().length > 0;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!puedeIngresar) return;
    await login(usuario.trim(), contrasena);
  }

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.titulo}>Punto de Venta</h1>
        <h2 className={styles.subtitulo}>Iniciar sesión</h2>

        <form onSubmit={handleSubmit} className={styles.form} noValidate>
          <div className={styles.campo}>
            <label htmlFor="usuario" className={styles.label}>
              Usuario
            </label>
            <input
              id="usuario"
              type="text"
              value={usuario}
              onChange={(e) => setUsuario(e.target.value)}
              className={styles.input}
              autoComplete="username"
              disabled={cargando}
              aria-required="true"
            />
          </div>

          <div className={styles.campo}>
            <label htmlFor="contrasena" className={styles.label}>
              Contraseña
            </label>
            <input
              id="contrasena"
              type="password"
              value={contrasena}
              onChange={(e) => setContrasena(e.target.value)}
              className={styles.input}
              autoComplete="current-password"
              disabled={cargando}
              aria-required="true"
            />
          </div>

          {errorLogin && (
            <p className={styles.error} role="alert">
              {errorLogin}
            </p>
          )}

          <button
            type="submit"
            className={styles.boton}
            disabled={!puedeIngresar || cargando}
            aria-busy={cargando}
          >
            {cargando ? 'Verificando...' : 'Ingresar'}
          </button>
        </form>
      </div>
    </div>
  );
}
