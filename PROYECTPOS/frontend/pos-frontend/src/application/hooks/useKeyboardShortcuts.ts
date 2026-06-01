import { useEffect } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import { useFocusManager } from './useFocusManager';

/**
 * Hook para atajos de teclado completos del sistema POS
 * 
 * Atajos implementados:
 * - F3 o Ctrl+F → Enfocar buscador
 * - ←/→ → Cambiar entre secciones (Productos ↔ Carrito ↔ Pago)
 * - F10 → Abrir historial (TODO: cuando esté implementado)
 * - F12 → Nueva venta
 * - Escape → Limpiar búsqueda / cancelar pago
 * - ↑/↓ → Navegar en sección activa (manejado en cada componente)
 * - Enter → Acción contextual según sección activa
 * - +/- → Ajustar cantidades (manejado en Cart)
 * - Delete → Eliminar del carrito (manejado en Cart)
 */
export function useKeyboardShortcuts() {
  const { estado, carrito, resetVenta, setEstado } = usePOSStore();
  const { moveLeft, moveRight } = useFocusManager();

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

      // ← → Mover a sección izquierda
      if (e.key === 'ArrowLeft') {
        // No permitir navegación si estamos en panel de pago
        if (estado === 'CALCULANDO_PAGO' || estado === 'PROCESANDO') return;
        
        // Solo si no estamos en un input
        const target = e.target as HTMLElement;
        if (target.tagName !== 'INPUT' && target.tagName !== 'TEXTAREA') {
          e.preventDefault();
          moveLeft();
        }
        return;
      }

      // → → Mover a sección derecha
      if (e.key === 'ArrowRight') {
        // No permitir navegación si estamos en panel de pago
        if (estado === 'CALCULANDO_PAGO' || estado === 'PROCESANDO') return;
        
        // Solo si no estamos en un input
        const target = e.target as HTMLElement;
        if (target.tagName !== 'INPUT' && target.tagName !== 'TEXTAREA') {
          e.preventDefault();
          moveRight();
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

      // Enter en venta exitosa → Nueva venta
      if (e.key === 'Enter' && estado === 'VENTA_COMPLETA') {
        e.preventDefault();
        resetVenta();
        return;
      }
    }

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [estado, carrito, resetVenta, setEstado, moveLeft, moveRight]);
}
