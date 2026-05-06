import { usePOSStore } from '@application/store/usePOSStore';
import type { IImpresionPort } from '@domain/ports/IImpresionPort';
import { formatearPrecio, formatearFecha } from '@ui/utils/formato';
import styles from './ReceiptButton.module.css';

interface Props {
  impresionPort: IImpresionPort;
  /** Datos completos de la venta para imprimir en el recibo */
  datosVenta?: {
    ventaId: string;
    fechaHora: string;
    cajero: string;
    items: Array<{ nombre: string; cantidad: number; subtotal: number }>;
    subtotal: number;
    iva: number;
    total: number;
    metodoPago: string;
    montoPagado: number;
    cambio: number;
  };
}

export function ReceiptButton({ impresionPort, datosVenta }: Props) {
  const estado = usePOSStore((s) => s.estado);

  if (estado !== 'VENTA_COMPLETA') return null;

  function handleImprimir() {
    window.print();
  }

  const esEfectivo = datosVenta?.metodoPago === 'EFECTIVO' || datosVenta?.cambio > 0;

  return (
    <>
      {/* Botón visible en pantalla */}
      <button className={styles.btn} onClick={handleImprimir} aria-label="Imprimir recibo">
        🖨️ Imprimir recibo
      </button>

      {/* Recibo — solo visible al imprimir (@media print) */}
      {datosVenta && (
        <div id="receipt" className={styles.recibo}>
          <div className={styles.reciboContenido}>
            <p className={styles.reciboTitulo}>PUNTO DE VENTA</p>
            <div className={styles.reciboSeparador} />

            <p>Fecha: {formatearFecha(datosVenta.fechaHora)}</p>
            <p>Cajero: {datosVenta.cajero}</p>
            <p>Venta: {datosVenta.ventaId}</p>

            <div className={styles.reciboSeparador} />

            {datosVenta.items.map((item, i) => (
              <div key={i} className={styles.reciboItem}>
                <span>{item.nombre}</span>
                <span>x{item.cantidad}</span>
                <span>{formatearPrecio(item.subtotal)}</span>
              </div>
            ))}

            <div className={styles.reciboSeparador} />

            <div className={styles.reciboFila}>
              <span>Subtotal:</span>
              <span>{formatearPrecio(datosVenta.subtotal)}</span>
            </div>
            <div className={styles.reciboFila}>
              <span>IVA (19%):</span>
              <span>{formatearPrecio(datosVenta.iva)}</span>
            </div>

            <div className={styles.reciboSeparador} />

            <div className={`${styles.reciboFila} ${styles.reciboTotal}`}>
              <span>TOTAL:</span>
              <span>{formatearPrecio(datosVenta.total)}</span>
            </div>

            <div className={styles.reciboSeparador} />

            <div className={styles.reciboFila}>
              <span>Pago: {datosVenta.metodoPago}</span>
              <span>{formatearPrecio(datosVenta.montoPagado)}</span>
            </div>

            {esEfectivo && (
              <div className={styles.reciboFila}>
                <span>Cambio:</span>
                <span>{formatearPrecio(datosVenta.cambio)}</span>
              </div>
            )}

            <div className={styles.reciboSeparador} />
            <p className={styles.reciboGracias}>¡Gracias por su compra!</p>
          </div>
        </div>
      )}
    </>
  );
}
