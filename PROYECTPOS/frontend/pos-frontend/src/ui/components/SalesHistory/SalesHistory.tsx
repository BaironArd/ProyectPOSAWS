import { useState } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import { useHistory } from '@application/hooks/useHistory';
import type { IVentaHistorialPort } from '@domain/ports/IVentaHistorialPort';
import type { DatosRecibo } from '@domain/types/POSState';
import { formatearPrecio, formatearFecha } from '@ui/utils/formato';
import { ReceiptViewer } from '../ReceiptViewer/ReceiptViewer';
import styles from './SalesHistory.module.css';

interface Props {
  historialPort: IVentaHistorialPort;
  onDevolver?: (ventaId: string) => void;
}

export function SalesHistory({ historialPort, onDevolver }: Props) {
  const estado = usePOSStore((s) => s.estado);
  const historial = usePOSStore((s) => s.historial);
  const recibosGuardados = usePOSStore((s) => s.recibosGuardados);
  const volverDeHistorial = usePOSStore((s) => s.volverDeHistorial);

  const [reciboViendo, setReciboViendo] = useState<DatosRecibo | null>(null);

  useHistory(historialPort);

  if (estado !== 'HISTORIAL') return null;

  return (
    <div className={styles.wrapper}>
      <div className={styles.header}>
        <h2 className={styles.titulo}>Historial de ventas</h2>
        <button className={styles.btnVolver} onClick={volverDeHistorial}>
          ← Volver
        </button>
      </div>

      {historial.length === 0 ? (
        <p className={styles.vacio}>No hay ventas registradas en este turno</p>
      ) : (
        <table className={styles.tabla} aria-label="Historial de ventas">
          <thead>
            <tr>
              <th>ID Venta</th>
              <th>Fecha / Hora</th>
              <th>Total</th>
              <th>Ítems</th>
              <th>Factura</th>
              {onDevolver && <th>Devolución</th>}
            </tr>
          </thead>
          <tbody>
            {historial.map((venta) => {
              const recibo = recibosGuardados[venta.ventaId];
              return (
                <tr key={venta.ventaId}>
                  <td>{venta.ventaId}</td>
                  <td>{formatearFecha(venta.fechaHora)}</td>
                  <td>{formatearPrecio(venta.total)}</td>
                  <td>{venta.cantidadItems}</td>
                  <td>
                    {recibo ? (
                      <button
                        className={styles.btnFactura}
                        onClick={() => setReciboViendo(recibo)}
                      >
                        🧾 Ver
                      </button>
                    ) : (
                      <span className={styles.sinFactura}>—</span>
                    )}
                  </td>
                  {onDevolver && (
                    <td>
                      <button
                        className={styles.btnDevolver}
                        onClick={() => onDevolver(venta.ventaId)}
                      >
                        Devolver
                      </button>
                    </td>
                  )}
                </tr>
              );
            })}
          </tbody>
        </table>
      )}

      {/* Modal de factura */}
      {reciboViendo && (
        <ReceiptViewer
          datos={reciboViendo}
          onCerrar={() => setReciboViendo(null)}
        />
      )}
    </div>
  );
}
