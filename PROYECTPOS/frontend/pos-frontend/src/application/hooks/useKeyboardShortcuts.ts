import { useEffect } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';

/**
 * Hook para atajos de teclado completos del sistema POS
 * 
 * Atajos implementados:
 * - F3 o Ctrl+F → Enfocar buscador
 * - F9 → Confirmar venta (si está en panel de pago)
 * - F10 → Abrir historial (TODO: cuando esté implementado)
 * - F12 → Nueva venta
 * - Escape → Limpiar búsqueda / cancelar pago
 * - ↑/↓ → Navegar productos (manejado en ProductList)
 * - Enter → Agregar producto seleccionado (manejado en ProductList)
 * - +/- → Ajustar cantidades (manejado en Cart)
 * - Delete → Eliminar del carrito (manejado en Cart)
 */
export function useKeyboardShortcuts() {
  const { estado, carrito, resetVenta, setEstado } = usePOSStore();

  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      // Prevenir comportamiento por defecto de teclas de función
      if (e.key.startsWith('F') && e.key.length <= 3) {
        e.preventDefault();
      }

      // F3 o Ctrl+F → Enfocar buscador
      if (e.key === 'F3' || (e.ctrlKey && e.key === 'f')) {
        e.preventDefault();
        const searchInput = document.querySelector<HTMLInputElement>('input[type="text"], input[placeholder*="Buscar"]');
        if (searchInput) {
          searchInput.focus();
          searchInput.select();
        }
        return;
      }

      // F9 → Confirmar venta (si está en panel de pago)
      if (e.key === 'F9') {
        e.preventDefault();
        if (estado === 'CALCULANDO_PAGO') {
          const confirmButton = document.querySelector<HTMLButtonElement>('button[type="submit"]');
          if (confirmButton && !confirmButton.disabled) {
            confirmButton.click();
          }
        }
        return;
      }

      // F10 → Abrir historial
      if (e.key === 'F10') {
        e.preventDefault();
        // TODO: Implementar cuando esté el componente de historial
        console.log('F10: Abrir historial (pendiente de implementar)');
        return;
      }

      // F12 → Nueva venta
      if (e.key === 'F12') {
        e.preventDefault();
        resetVenta();
        return;
      }

      // Escape → Limpiar búsqueda / cancelar pago
      if (e.key === 'Escape') {
        e.preventDefault();
        if (estado === 'CALCULANDO_PAGO') {
          setEstado('CARRITO_ACTIVO');
        } else if (estado === 'BUSCANDO' || estado === 'RESULTADOS') {
          const searchInput = document.querySelector<HTMLInputElement>('input[type="text"], input[placeholder*="Buscar"]');
          if (searchInput) {
            searchInput.value = '';
            searchInput.dispatchEvent(new Event('input', { bubbles: true }));
          }
        }
        return;
      }
    }

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [estado, carrito, resetVenta, setEstado]);
}
