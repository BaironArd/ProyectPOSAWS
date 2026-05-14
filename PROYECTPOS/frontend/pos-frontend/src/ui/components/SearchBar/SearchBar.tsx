import { usePOSStore } from '@application/store/usePOSStore';
import type { IProductoPort } from '@domain/ports/IProductoPort';
import { useSearch } from '@application/hooks/useSearch';
import styles from './SearchBar.module.css';

interface Props {
  productoPort: IProductoPort;
}

export function SearchBar({ productoPort }: Props) {
  const query = usePOSStore((s) => s.query);
  const estado = usePOSStore((s) => s.estado);
  const setQuery = usePOSStore((s) => s.setQuery);

  useSearch(productoPort);

  const buscando = estado === 'BUSCANDO';

  return (
    <div className={styles.wrapper}>
      <div className={styles.inputWrapper}>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Buscar producto por nombre o código (mínimo 2 caracteres)..."
          className={styles.input}
          aria-label="Buscar producto"
          aria-busy={buscando}
        />
        {buscando && (
          <span className={styles.spinner} role="status" aria-label="Buscando...">
            <span className={styles.spinnerIcon} />
          </span>
        )}
      </div>
    </div>
  );
}
