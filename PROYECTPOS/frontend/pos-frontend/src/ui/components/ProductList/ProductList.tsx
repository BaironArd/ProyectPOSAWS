import { usePOSStore } from '@application/store/usePOSStore';
import { formatearPrecio } from '@ui/utils/formato';
import styles from './ProductList.module.css';

export function ProductList() {
  const productos = usePOSStore((s) => s.productos);
  const query = usePOSStore((s) => s.query);
  const estado = usePOSStore((s) => s.estado);
  const agregarAlCarrito = usePOSStore((s) => s.agregarAlCarrito);

  if (estado !== 'RESULTADOS' && estado !== 'CARRITO_ACTIVO') return null;

  if (productos.length === 0) {
    return (
      <p className={styles.sinResultados}>
        Sin resultados para &ldquo;{query}&rdquo;
      </p>
    );
  }

  return (
    <ul className={styles.lista} aria-label="Resultados de búsqueda">
      {productos.map((producto) => (
        <li key={producto.id} className={styles.card}>
          <div className={styles.info}>
            <span className={styles.nombre}>{producto.nombre}</span>
            <span className={styles.precio}>{formatearPrecio(producto.precio)}</span>
          </div>
          <button
            className={styles.botonAgregar}
            onClick={() => agregarAlCarrito(producto)}
            disabled={producto.stock === 0}
            aria-label={`Agregar ${producto.nombre} al carrito`}
          >
            {producto.stock === 0 ? 'Sin stock' : 'Agregar'}
          </button>
        </li>
      ))}
    </ul>
  );
}
