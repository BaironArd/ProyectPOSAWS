import { createPortal } from 'react-dom';
import type { DatosRecibo } from '@domain/types/POSState';
import { formatearPrecio, formatearFecha } from '@ui/utils/formato';
import styles from './ReceiptPortal.module.css';

interface Props {
  datos: DatosRecibo;
}

/**
 * Portal que renderiza el recibo directamente en el body para impresión.
 * Esto evita problemas con estilos de impresión que ocultan contenedores padres.
 */
export function ReceiptPortal({ datos }: Props) {
  const esEfectivo = datos.metodoPago === 'EFECTIVO' || datos.cambio > 0;

  // El recibo se renderiza en un portal directamente en el body
  // para que no sea afectado por estilos de impresión de componentes padres
  return createPortal(
    <div id="receipt" className={styles.recibo}>
      <div className={styles.contenido}>
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
    </div>,
    document.body
  );
}
