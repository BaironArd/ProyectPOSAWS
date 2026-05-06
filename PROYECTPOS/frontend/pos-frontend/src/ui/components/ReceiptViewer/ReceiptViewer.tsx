import type { DatosRecibo } from '@domain/types/POSState';
import { formatearPrecio, formatearFecha } from '@ui/utils/formato';
import styles from './ReceiptViewer.module.css';

interface Props {
  datos: DatosRecibo;
  onCerrar: () => void;
}

export function ReceiptViewer({ datos, onCerrar }: Props) {
  const esEfectivo = datos.metodoPago === 'EFECTIVO' || datos.cambio > 0;

  function handleImprimir() {
    window.print();
  }

  return (
    <div className={styles.overlay} role="dialog" aria-modal="true" aria-label="Factura de venta">
      <div className={styles.modal}>
        <div className={styles.acciones}>
          <button className={styles.btnImprimir} onClick={handleImprimir}>
            🖨️ Imprimir
          </button>
          <button className={styles.btnCerrar} onClick={onCerrar} aria-label="Cerrar factura">
            ✕
          </button>
        </div>

        {/* Contenido del recibo */}
        <div className={styles.recibo} id="receipt">
          <p className={styles.titulo}>PUNTO DE VENTA</p>
          <div className={styles.sep} />

          <p>Fecha: {formatearFecha(datos.fechaHora)}</p>
          <p>Cajero: {datos.cajero}</p>
          <p>Venta: {datos.ventaId}</p>

          <div className={styles.sep} />

          {datos.items.map((item, i) => (
            <div key={i} className={styles.item}>
              <span className={styles.itemNombre}>{item.nombre}</span>
              <span>x{item.cantidad}</span>
              <span>{formatearPrecio(item.subtotal)}</span>
            </div>
          ))}

          <div className={styles.sep} />

          <div className={styles.fila}>
            <span>Subtotal:</span>
            <span>{formatearPrecio(datos.subtotal)}</span>
          </div>
          <div className={styles.fila}>
            <span>IVA (19%):</span>
            <span>{formatearPrecio(datos.iva)}</span>
          </div>

          <div className={styles.sep} />

          <div className={`${styles.fila} ${styles.total}`}>
            <span>TOTAL:</span>
            <span>{formatearPrecio(datos.total)}</span>
          </div>

          <div className={styles.sep} />

          <div className={styles.fila}>
            <span>Pago: {datos.metodoPago}</span>
            <span>{formatearPrecio(datos.montoPagado)}</span>
          </div>

          {esEfectivo && datos.cambio > 0 && (
            <div className={styles.fila}>
              <span>Cambio:</span>
              <span>{formatearPrecio(datos.cambio)}</span>
            </div>
          )}

          <div className={styles.sep} />
          <p className={styles.gracias}>¡Gracias por su compra!</p>
        </div>
      </div>
    </div>
  );
}
