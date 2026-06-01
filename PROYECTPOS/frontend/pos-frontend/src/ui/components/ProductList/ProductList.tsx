import { useState, useEffect, useRef } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import { formatearPrecio } from '@ui/utils/formato';
import styles from './ProductList.module.css';

export function ProductList() {
  const productos = usePOSStore((s) => s.productos);
  const query = usePOSStore((s) => s.query);
  const estado = usePOSStore((s) => s.estado);
  const agregarAlCarrito = usePOSStore((s) => s.agregarAlCarrito);
  
  const [selectedIndex, setSelectedIndex] = useState(0);
  const listRef = useRef<HTMLUListElement>(null);

  // Resetear selección cuando cambian los productos
  useEffect(() => {
    setSelectedIndex(0);
  }, [productos]);

  // Manejar navegación con teclado
  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      // Solo manejar teclas si estamos en la vista de resultados
      if (estado !== 'RESULTADOS' && estado !== 'CARRITO_ACTIVO') return;
      if (productos.length === 0) return;

      // Flecha arriba
      if (e.key === 'ArrowUp') {
        e.preventDefault();
        setSelectedIndex((prev) => (prev > 0 ? prev - 1 : productos.length - 1));
        return;
      }

      // Flecha abajo
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        setSelectedIndex((prev) => (prev < productos.length - 1 ? prev + 1 : 0));
        return;
      }

      // Enter - agregar producto seleccionado
      if (e.key === 'Enter') {
        // Solo si no estamos en un input
        const activeElement = document.activeElement;
        if (activeElement?.tagName === 'INPUT' || activeElement?.tagName === 'BUTTON') {
          return;
        }
        
        e.preventDefault();
        const productoSeleccionado = productos[selectedIndex];
        if (productoSeleccionado && productoSeleccionado.stock > 0) {
          agregarAlCarrito(productoSeleccionado);
        }
        return;
      }
    }

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [estado, productos, selectedIndex, agregarAlCarrito]);

  // Scroll automático al producto seleccionado
  useEffect(() => {
    if (listRef.current) {
      const selectedElement = listRef.current.children[selectedIndex] as HTMLElement;
      if (selectedElement) {
        selectedElement.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
      }
    }
  }, [selectedIndex]);

  if (estado !== 'RESULTADOS' && estado !== 'CARRITO_ACTIVO') return null;

  if (productos.length === 0) {
    return (
      <p className={styles.sinResultados}>
        Sin resultados para &ldquo;{query}&rdquo;
      </p>
    );
  }

  return (
    <ul ref={listRef} className={styles.lista} aria-label="Resultados de búsqueda">
      {productos.map((producto, index) => (
        <li 
          key={producto.id} 
          className={`${styles.card} ${index === selectedIndex ? styles.selected : ''}`}
          data-selected={index === selectedIndex}
        >
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
