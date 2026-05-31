import { useEffect } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';

/**
 * Hook para atajos de teclado estándar POS
 * 
 * Atajos implementados:
 * - F1 o / → Enfocar buscador
 * - F2 → Ir al panel de pago (si hay items en carrito)
 * - Enter → Confirmar venta (si está en panel de pago)
 * - Escape → Limpiar búsqueda / cancelar pago
 * - F12 → Nueva venta
 */
export function useKeyboardShortcuts() {
  const { estado, carrito, resetVenta, setEstado } = usePOSStore();

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      // F1 o / → Enfocar buscador
      if (e.key === 'F1' || e.key === '/') {
        e.preventDefault();
        const searchInput = document.querySelector<HTMLInputElement>('input[type="search"], input[placeholder*="Buscar"]');
        if (searchInput) {
          searchInput.focus();
          searchInput.select();
        }
        return;
      }

      // F2 → Ir al panel de pago (si hay items en carrito)
      if (e.key === 'F2') {
        e.preventDefault();
        if (carrito.length > 0 && estado === 'CARRITO_ACTIVO') {
          setEstado('CALCULANDO_PAGO');
        }
        return;
      }

      // Enter → Confirmar venta (si está en panel de pago)
      if (e.key === 'Enter' && estado === 'CALCULANDO_PAGO') {
        // El botón de confirmar venta manejará la lógica
        const confirmButton = document.querySelector<HTMLButtonElement>('button[type="submit"]');
        if (confirmButton && !confirmButton.disabled) {
          confirmButton.click();
        }
        return;
      }

      // Escape → Limpiar búsqueda / cancelar pago
      if (e.key === 'Escape') {
        e.preventDefault();
        if (estado === 'CALCULANDO_PAGO') {
          setEstado('CARRITO_ACTIVO');
        } else if (estado === 'BUSCANDO' || estado === 'RESULTADOS') {
          const searchInput = document.querySelector<HTMLInputElement>('input[type="search"], input[placeholder*="Buscar"]');
          if (searchInput) {
            searchInput.value = '';
            searchInput.dispatchEvent(new Event('input', { bubbles: true }));
          }
        }
        return;
      }

      // F12 → Nueva venta
      if (e.key === 'F12') {
        e.preventDefault();
        resetVenta();
        return;
      }
    }

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [estado, carrito, resetVenta, setEstado]);
}
