import { usePOSStore } from '@application/store/usePOSStore';
import { useRefund } from '@application/hooks/useRefund';
import type { IDevolucionPort } from '@domain/ports/IDevolucionPort';
import { formatearPrecio } from '@ui/utils/formato';
import styles from './RefundPanel.module.css';

interface Props {
  devolucionPort: IDevolucionPort;
}

export function RefundPanel({ devolucionPort }: Props) {
  const estado = usePOSStore((s) => s.estado);
  const ventaIdActual = usePOSStore((s) => s.ventaIdActual);
  const setEstado = usePOSStore((s) => s.setEstado);

  const {
    procesar,
    procesando,
    cargandoItems,
    items,
    devolucion,
    setCantidadDevolver,
    subtotalDevolucion,
    ivaDevolucion,
    montoADevolver,
    hayItemsSeleccionados,
  } = useRefund(devolucionPort);

  if (estado !== 'DEVOLUCION') return null;

  // ── Resultado exitoso ──
  if (devolucion) {
    return (
      <div className={styles.exito}>
        <span className={styles.exitoIcono}>✅</span>
        <h2>Devolución procesada</h2>
        <p>
          Devolver <strong>{formatearPrecio(devolucion.montoDevuelto)}</strong> al cliente.
        </p>
        <p className={styles.estadoBadge}>
          {devolucion.estado === 'PARCIAL' ? '🔄 Devolución parcial' : '↩️ Devolución total'}
        </p>
        <button className={styles.btnCerrar} onClick={() => setEstado('IDLE')}>
          Cerrar
        </button>
      </div>
    );
  }

  // ── Cargando ítems ──
  if (cargandoItems) {
    return (
      <div className={styles.wrapper}>
        <p className={styles.cargando}>Cargando ítems de la venta...</p>
      </div>
    );
  }

  // ── Sin venta seleccionada ──
  if (!ventaIdActual) {
    return (
      <div className={styles.wrapper}>
        <h2 className={styles.titulo}>Devolución de venta</h2>
        <p className={styles.sinVenta}>No hay ninguna venta seleccionada para devolver.</p>
        <button className={styles.btnCancelar} onClick={() => setEstado('IDLE')}>
          Volver
        </button>
      </div>
    );
  }

  const esTotal = items.every(i => i.cantidadDevolver === i.cantidad);
  const esParcial = hayItemsSeleccionados && !esTotal;

  return (
    <div className={styles.wrapper}>
      <div className={styles.header}>
        <h2 className={styles.titulo}>Devolución de venta</h2>
        <button className={styles.btnCancelar} onClick={() => setEstado('IDLE')} disabled={procesando}>
          ← Cancelar
        </button>
      </div>

      <p className={styles.ventaId}>Venta: <strong>{ventaIdActual}</strong></p>
      <p className={styles.instruccion}>
        Ajusta las cantidades a devolver por ítem. Pon 0 para no devolver ese producto.
      </p>

      <table className={styles.tabla} aria-label="Ítems de la venta">
        <thead>
          <tr>
            <th>Producto</th>
            <th>Comprado</th>
            <th>Precio unit.</th>
            <th>A devolver</th>
            <th>Subtotal dev.</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={item.productoId} className={item.cantidadDevolver === 0 ? styles.filaExcluida : ''}>
              <td>{item.nombre}</td>
              <td className={styles.centrado}>{item.cantidad}</td>
              <td>{formatearPrecio(item.precioUnitario)}</td>
              <td className={styles.centrado}>
                <div className={styles.cantidadControl}>
                  <button
                    className={styles.btnCantidad}
                    onClick={() => setCantidadDevolver(item.productoId, item.cantidadDevolver - 1)}
                    disabled={procesando || item.cantidadDevolver <= 0}
                    aria-label="Reducir cantidad"
                  >−</button>
                  <span className={styles.cantidadValor}>{item.cantidadDevolver}</span>
                  <button
                    className={styles.btnCantidad}
                    onClick={() => setCantidadDevolver(item.productoId, item.cantidadDevolver + 1)}
                    disabled={procesando || item.cantidadDevolver >= item.cantidad}
                    aria-label="Aumentar cantidad"
                  >+</button>
                </div>
              </td>
              <td className={styles.subtotalDev}>
                {item.cantidadDevolver > 0
                  ? formatearPrecio(item.precioUnitario * item.cantidadDevolver)
                  : <span className={styles.noDevuelve}>—</span>}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className={styles.resumenDevolucion}>
        <div className={styles.filaResumen}>
          <span>Subtotal:</span>
          <span>{formatearPrecio(subtotalDevolucion)}</span>
        </div>
        <div className={styles.filaResumen}>
          <span>IVA (19%):</span>
          <span>{formatearPrecio(ivaDevolucion)}</span>
        </div>
        <div className={`${styles.filaResumen} ${styles.filaTotal}`}>
          <span>{esParcial ? '🔄 Total a devolver (parcial):' : '↩️ Total a devolver:'}</span>
          <span className={styles.montoDevolver}>{formatearPrecio(montoADevolver)}</span>
        </div>
      </div>

      <div className={styles.acciones}>
        <button
          className={styles.btnConfirmar}
          onClick={procesar}
          disabled={procesando || !hayItemsSeleccionados}
          aria-busy={procesando}
        >
          {procesando ? 'Procesando...' : `Confirmar devolución${esParcial ? ' parcial' : ''}`}
        </button>
      </div>
    </div>
  );
}
