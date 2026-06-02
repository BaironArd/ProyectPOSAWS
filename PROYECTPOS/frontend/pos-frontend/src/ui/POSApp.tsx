import { useEffect, useState } from 'react';
import * as React from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import { useKeyboardShortcuts } from '@application/hooks/useKeyboardShortcuts';
import { useFocusManager } from '@application/hooks/useFocusManager';

import type { IProductoPort } from '@domain/ports/IProductoPort';
import type { IVentaPort } from '@domain/ports/IVentaPort';
import type { IImpresionPort } from '@domain/ports/IImpresionPort';

import { Header }       from './components/Header/Header';
import { ErrorBanner }  from './components/ErrorBanner/ErrorBanner';
import { SearchBar }    from './components/SearchBar/SearchBar';
import { ProductList }  from './components/ProductList/ProductList';
import { Cart }         from './components/Cart/Cart';
import { OrderSummary } from './components/OrderSummary/OrderSummary';
import { PaymentPanel } from './components/PaymentPanel/PaymentPanel';
import { ReceiptButton } from './components/ReceiptButton/ReceiptButton';
import { ReceiptPortal } from './components/ReceiptPortal/ReceiptPortal';

import styles from './POSApp.module.css';

interface Props {
  productoPort:  IProductoPort;
  ventaPort:     IVentaPort;
  impresionPort: IImpresionPort;
}

/**
 * Aplicación POS simplificada — solo flujo de cajero:
 * Buscar producto → Agregar al carrito → Pagar → Venta completa
 *
 * Sin login, sin admin, sin inventario, sin reportes, sin devoluciones.
 */
export function POSApp({ productoPort, ventaPort, impresionPort }: Props) {
  const estado      = usePOSStore((s) => s.estado);
  const carrito     = usePOSStore((s) => s.carrito);
  const datosRecibo = usePOSStore((s) => s.datosRecibo);
  const ventaIdActual = usePOSStore((s) => s.ventaIdActual);
  const resetVenta  = usePOSStore((s) => s.resetVenta);

  const activeSection = useFocusManager((s) => s.activeSection);
  const setActiveSection = useFocusManager((s) => s.setActiveSection);

  // Estado para navegación en pantalla de venta exitosa
  const [focusedSuccessButton, setFocusedSuccessButton] = React.useState(0); // 0=Imprimir, 1=Nueva venta

  // Activar atajos de teclado estándar POS
  useKeyboardShortcuts();

  // Navegación con teclado en pantalla de venta exitosa
  useEffect(() => {
    if (estado !== 'VENTA_COMPLETA') return;

    const handleKeyDown = (e: KeyboardEvent) => {
      // Prevenir que otros listeners manejen estas teclas en VENTA_COMPLETA
      if (['ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', 'Enter'].includes(e.key)) {
        e.stopPropagation();
      }

      // Flechas para navegar entre botones
      if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
        e.preventDefault();
        setFocusedSuccessButton(0); // Imprimir
        return;
      }

      if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
        e.preventDefault();
        setFocusedSuccessButton(1); // Nueva venta
        return;
      }

      // Enter para ejecutar el botón enfocado
      if (e.key === 'Enter') {
        e.preventDefault();
        if (focusedSuccessButton === 0) {
          // Hacer clic en el botón de imprimir
          const imprimirBtn = document.querySelector('[data-action="imprimir"]') as HTMLButtonElement;
          if (imprimirBtn) imprimirBtn.click();
        } else {
          // Nueva venta
          resetVenta();
        }
        return;
      }
    };

    // Usar capturing phase para capturar eventos antes que otros listeners
    window.addEventListener('keydown', handleKeyDown, true);
    return () => window.removeEventListener('keydown', handleKeyDown, true);
  }, [estado, focusedSuccessButton, resetVenta]);

  // Resetear foco al entrar a venta exitosa
  useEffect(() => {
    if (estado === 'VENTA_COMPLETA') {
      setFocusedSuccessButton(1); // Enfoca "Nueva venta" por defecto
    }
  }, [estado]);

  const mostrarPanelVenta = estado !== 'VENTA_COMPLETA';

  return (
    <div className={styles.app}>
      {/* Portal de impresión */}
      {datosRecibo && <ReceiptPortal datos={datosRecibo} />}

      <Header />

      <main className={styles.main}>
        <ErrorBanner />

        {/* ── Pantalla de venta completada ── */}
        {estado === 'VENTA_COMPLETA' && (
          <div className={styles.ventaCompleta}>
            <div className={styles.exito}>
              <span className={styles.exitoIcono}>✅</span>
              <h2>¡Venta completada!</h2>
              <p className={styles.cambioTexto}>
                Cambio:{' '}
                <strong>
                  {new Intl.NumberFormat('es-CO', {
                    style: 'currency',
                    currency: 'COP',
                    maximumFractionDigits: 0,
                  }).format(datosRecibo?.cambio ?? 0)}
                </strong>
              </p>
              <p className={styles.ventaId}>ID: {ventaIdActual}</p>
            </div>
            <div className={styles.accionesVenta}>
              <ReceiptButton
                impresionPort={impresionPort}
                datosVenta={datosRecibo ?? undefined}
                isFocused={focusedSuccessButton === 0}
              />
              <button 
                className={`${styles.btnNuevaVenta} ${focusedSuccessButton === 1 ? styles.btnFocused : ''}`}
                onClick={resetVenta}
              >
                Nueva venta
              </button>
            </div>
          </div>
        )}

        {/* ── Panel principal: búsqueda + carrito ── */}
        {mostrarPanelVenta && (
          <div className={`${styles.flujoVenta} ${carrito.length > 0 ? styles.conCarrito : styles.sinCarrito}`}>
            <div className={`${styles.columnaIzq} ${activeSection !== 'products' ? styles.inactive : ''}`}>
              <SearchBar productoPort={productoPort} />
              <ProductList />
            </div>
            {carrito.length > 0 && (
              <div className={styles.columnaDer}>
                <Cart />
                <OrderSummary />
                <PaymentPanel ventaPort={ventaPort} />
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}
