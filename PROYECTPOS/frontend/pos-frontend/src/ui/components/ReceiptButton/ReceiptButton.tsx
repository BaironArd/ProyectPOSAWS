import { usePOSStore } from '@application/store/usePOSStore';
import type { IImpresionPort } from '@domain/ports/IImpresionPort';
import styles from './ReceiptButton.module.css';

interface Props {
  impresionPort: IImpresionPort;
  /** Datos completos de la venta (ya no se usan aquí, el recibo se renderiza en ReceiptPortal) */
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

/**
 * Botón para imprimir el recibo.
 * El contenido del recibo se renderiza en ReceiptPortal para evitar
 * problemas con estilos de impresión.
 */
export function ReceiptButton({ impresionPort: _, datosVenta: __ }: Props) {
  const estado = usePOSStore((s) => s.estado);

  if (estado !== 'VENTA_COMPLETA') return null;

  function handleImprimir() {
    window.print();
  }

  return (
    <button className={styles.btn} onClick={handleImprimir} aria-label="Imprimir recibo">
      🖨️ Imprimir recibo
    </button>
  );
}
