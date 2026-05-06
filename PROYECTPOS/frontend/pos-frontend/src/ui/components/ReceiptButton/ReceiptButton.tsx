import { usePOSStore } from '@application/store/usePOSStore';
import { useReceipt } from '@application/hooks/useReceipt';
import type { IImpresionPort } from '@domain/ports/IImpresionPort';
import styles from './ReceiptButton.module.css';

interface Props { impresionPort: IImpresionPort; }

export function ReceiptButton({ impresionPort }: Props) {
  const estado = usePOSStore((s) => s.estado);
  const { imprimir } = useReceipt(impresionPort);

  if (estado !== 'VENTA_COMPLETA') return null;

  return (
    <button className={styles.btn} onClick={imprimir} aria-label="Imprimir recibo">
      🖨️ Imprimir recibo
    </button>
  );
}
