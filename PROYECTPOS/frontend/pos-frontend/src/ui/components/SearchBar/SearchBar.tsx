import { useState } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import type { IProductoPort } from '@domain/ports/IProductoPort';
import styles from './SearchBar.module.css';

interface Props {
  productoPort: IProductoPort;
}

// Patrón de código de barras: XXX-NNN (ej. LAP-001, MON-042)
const BARCODE_PATTERN = /^[A-Z]{3}-\d{3}$/;

export function SearchBar({ productoPort }: Props) {
  const estado = usePOSStore((s) => s.estado);
  const setEstado = usePOSStore((s) => s.setEstado);
  const setProductos = usePOSStore((s) => s.setProductos);
  const agregarAlCarrito = usePOSStore((s) => s.agregarAlCarrito);
  const setError = usePOSStore((s) => s.setError);

  const [searchText, setSearchText] = useState('');

  const buscando = estado === 'BUSCANDO';

  // Buscar al presionar Enter
  const handleSearch = async () => {
    if (searchText.trim().length < 2) {
      setProductos([]);
      setEstado('IDLE');
      return;
    }

    setEstado('BUSCANDO');

    try {
      const productos = await productoPort.buscar(searchText.trim());
      setProductos(productos);
      setEstado('RESULTADOS');
    } catch (error) {
      console.error('Error al buscar productos:', error);
      setError({
        codigo: 'SEARCH_FAILED',
        mensaje: 'Error al buscar productos'
      });
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleSearch();
    }
  };

  // Manejar paste para detectar código de barras
  const handlePaste = async (e: React.ClipboardEvent<HTMLInputElement>) => {
    const pastedText = e.clipboardData.getData('text').trim().toUpperCase();
    
    // Detectar si es un código de barras completo
    if (BARCODE_PATTERN.test(pastedText)) {
      e.preventDefault();
      
      try {
        // Buscar producto por código
        const productos = await productoPort.buscar(pastedText);
        
        if (productos.length === 1) {
          const producto = productos[0];
          
          if (producto && producto.stock > 0) {
            // Auto-agregar al carrito
            agregarAlCarrito(producto);
            
            // Limpiar búsqueda
            setSearchText('');
            
            // Mostrar notificación de éxito (usando console por ahora)
            console.log(`✓ ${producto.nombre} agregado al carrito`);
            
            // TODO: Agregar toast notification cuando esté implementado
          } else if (producto) {
            // Producto sin stock
            console.error(`✗ ${producto.nombre} sin stock`);
            // TODO: Agregar toast notification de error
          }
        } else if (productos.length === 0) {
          // Producto no encontrado
          console.error(`✗ Código ${pastedText} no encontrado`);
          // TODO: Agregar toast notification de error
        } else {
          // Múltiples productos - mostrar en lista (comportamiento normal)
          setSearchText(pastedText);
          // Buscar automáticamente cuando es paste de código
          setTimeout(() => handleSearch(), 0);
        }
      } catch (error) {
        console.error('Error al buscar código de barras:', error);
        // Comportamiento normal en caso de error
        setSearchText(pastedText);
      }
    }
    // Si no es código de barras, dejar que el paste normal ocurra
  };

  return (
    <div className={styles.wrapper}>
      <div className={styles.inputWrapper}>
        <input
          type="text"
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          onKeyDown={handleKeyDown}
          onPaste={handlePaste}
          placeholder="Buscar por nombre o código (presiona Enter para buscar)..."
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
