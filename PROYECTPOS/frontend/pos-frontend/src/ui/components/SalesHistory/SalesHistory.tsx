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
  const [desde, setDesde] = useState(() => new Date().toISOString().slice(0, 10));
  const [hasta, setHasta] = useState(() => new Date().toISOString().slice(0, 10));

  const { cargarHistorial } = useHistory(historialPort);

  if (estado !== 'HISTORIAL') return null;

  return (
    <div className={styles.wrapper}>
      <div className={styles.header}>
        <h2 className={styles.titulo}>Historial de ventas</h2>
        <button className={styles.btnVolver} onClick={volverDeHistorial}>
          ← Volver
        </button>
      </div>

      <div className={styles.filtros}>
        <label className={styles.label}>Desde</label>
        <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} className={styles.inputFecha} />
        <label className={styles.label}>Hasta</label>
        <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} className={styles.inputFecha} />
        <button className={styles.btnBuscar} onClick={() => cargarHistorial(desde, hasta)}>
          Buscar
        </button>
      </div>

      {historial.length === 0 ? (
        <p className={styles.vacio}>No hay ventas registradas en el rango seleccionado</p>
      ) : (
        <table className={styles.tabla} aria-label="Historial de ventas">
          <thead>
            <tr>
              <th>ID Venta</th>
              <th>Fecha / Hora</th>
              <th>Total</th>
              <th>Devuelto</th>
              <th>Estado</th>
              <th>Ítems</th>
              <th>Factura</th>
              {onDevolver && <th>Devolución</th>}
            </tr>
          </thead>
          <tbody>
            {historial.map((venta) => {
              const recibo = recibosGuardados[venta.ventaId];
              const tieneDevolucion = venta.estado === 'DEVUELTA' || venta.estado === 'PARCIAL';
              const puedeDevolver = venta.estado !== 'DEVUELTA';
              return (
                <tr key={venta.ventaId} className={tieneDevolucion ? styles.filaDevuelta : ''}>
                  <td>{venta.ventaId}</td>
                  <td>{formatearFecha(venta.fechaHora)}</td>
                  <td>{formatearPrecio(venta.total)}</td>
                  <td>
                    {venta.montoDevuelto
                      ? <span className={styles.montoDevuelto}>−{formatearPrecio(venta.montoDevuelto)}</span>
                      : <span className={styles.sinDevolucion}>—</span>}
                  </td>
                  <td>
                    {venta.estado === 'DEVUELTA' && <span className={styles.badgeDevuelta}>↩ Devuelta</span>}
                    {venta.estado === 'PARCIAL'  && <span className={styles.badgeParcial}>🔄 Parcial</span>}
                    {(!venta.estado || venta.estado === 'COMPLETADA') && <span className={styles.badgeOk}>✓ Completada</span>}
                  </td>
                  <td>{venta.cantidadItems}</td>
                  <td>
                    {recibo ? (
                      <button className={styles.btnFactura} onClick={() => setReciboViendo(recibo)}>
                        🧾 Ver
                      </button>
                    ) : (
                      <span className={styles.sinFactura}>—</span>
                    )}
                  </td>
                  {onDevolver && (
                    <td>
                      {puedeDevolver ? (
                        <button className={styles.btnDevolver} onClick={() => onDevolver(venta.ventaId)}>
                          Devolver
                        </button>
                      ) : (
                        <span className={styles.sinFactura}>—</span>
                      )}
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
