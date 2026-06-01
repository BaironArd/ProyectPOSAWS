import { useEffect } from 'react';
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
  const datosRecibo = usePOSStore((s) => s.datosRecibo);
  const ventaIdActual = usePOSStore((s) => s.ventaIdActual);
  const irAIdle     = usePOSStore((s) => s.irAIdle);
  const resetVenta  = usePOSStore((s) => s.resetVenta);

  const activeSection = useFocusManager((s) => s.activeSection);

  // Activar atajos de teclado estándar POS
  useKeyboardShortcuts();

  // Auto-retorno a IDLE tras 8s después de venta completa
  useEffect(() => {
    if (estado !== 'VENTA_COMPLETA') return;
    const timer = setTimeout(() => irAIdle(), 8000);
    return () => clearTimeout(timer);
  }, [estado, irAIdle]);

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
              />
              <button className={styles.btnNuevaVenta} onClick={resetVenta}>
                Nueva venta
              </button>
            </div>
          </div>
        )}

        {/* ── Panel principal: búsqueda + carrito ── */}
        {mostrarPanelVenta && (
          <div className={styles.flujoVenta}>
            <div className={`${styles.columnaIzq} ${activeSection !== 'products' ? styles.inactive : ''}`}>
              <SearchBar productoPort={productoPort} />
              <ProductList />
            </div>
            <div className={`${styles.columnaDer} ${activeSection === 'products' ? styles.inactive : ''}`}>
              <Cart />
              <OrderSummary />
              <PaymentPanel ventaPort={ventaPort} />
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
