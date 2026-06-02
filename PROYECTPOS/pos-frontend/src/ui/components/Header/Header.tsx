import { usePOSStore } from '@application/store/usePOSStore';
import styles from './Header.module.css';

/**
 * Header simplificado — solo muestra el nombre del POS.
 * Sin login, sin logout, sin roles de admin.
 */
export function Header() {
  const carrito = usePOSStore((s) => s.carrito);
  const estado  = usePOSStore((s) => s.estado);
  const resetVenta = usePOSStore((s) => s.resetVenta);

  const itemsCarrito = carrito.reduce((sum, i) => sum + i.cantidad, 0);
  const navegacionBloqueada = estado === 'PROCESANDO' || estado === 'CALCULANDO_PAGO';

  return (
    <header className={styles.header}>
      <div className={styles.marca}>
        <span className={styles.titulo}>🛒 Punto de Venta</span>
      </div>

      <nav className={styles.nav}>
        {itemsCarrito > 0 && (
          <span className={styles.carritoInfo}>
            {itemsCarrito} {itemsCarrito === 1 ? 'producto' : 'productos'} en carrito
          </span>
        )}

        {estado !== 'IDLE' && estado !== 'BUSCANDO' && estado !== 'RESULTADOS' && (
          <button
            className={styles.btnNav}
            onClick={resetVenta}
            disabled={navegacionBloqueada}
            aria-label="Nueva venta"
          >
            ➕ Nueva venta
          </button>
        )}
      </nav>
    </header>
  );
}
