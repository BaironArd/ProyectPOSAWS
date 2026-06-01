import { useEffect } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import { useFocusManager } from '@application/hooks/useFocusManager';
import { usePayment } from '@application/hooks/usePayment';
import type { IVentaPort } from '@domain/ports/IVentaPort';
import type { MetodoPago } from '@domain/types/POSState';
import { formatearPrecio } from '@ui/utils/formato';
import styles from './PaymentPanel.module.css';

const METODOS: { value: Exclude<MetodoPago, 'MIXTO'>; label: string }[] = [
  { value: 'EFECTIVO',       label: '💵 Efectivo' },
  { value: 'TARJETA_DEBITO', label: '💳 Débito' },
  { value: 'TARJETA_CREDITO',label: '💳 Crédito' },
  { value: 'TRANSFERENCIA',  label: '🏦 Transferencia' },
];

interface Props { ventaPort: IVentaPort; }

export function PaymentPanel({ ventaPort }: Props) {
  const estado      = usePOSStore((s) => s.estado);
  const resumen     = usePOSStore((s) => s.resumen);
  const metodoPago  = usePOSStore((s) => s.metodoPago);
  const pagos       = usePOSStore((s) => s.pagos);
  const montoPagado = usePOSStore((s) => s.montoPagado);
  const cambio      = usePOSStore((s) => s.cambio);

  const setMetodoPago  = usePOSStore((s) => s.setMetodoPago);
  const setMontoPagado = usePOSStore((s) => s.setMontoPagado);
  const agregarPago    = usePOSStore((s) => s.agregarPago);
  const eliminarPago   = usePOSStore((s) => s.eliminarPago);
  const actualizarPago = usePOSStore((s) => s.actualizarPago);

  const activeSection = useFocusManager((s) => s.activeSection);
  const setActiveSection = useFocusManager((s) => s.setActiveSection);

  const { confirmarVenta, procesando, puedeConfirmar } = usePayment(ventaPort);

  // Activar sección al hacer clic
  const handleClick = () => {
    setActiveSection('payment');
  };

  // Navegación con teclado en panel de pago
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (estado !== 'CALCULANDO_PAGO') return;
      if (activeSection !== 'payment') return;

      // Enter → Confirmar venta
      if (e.key === 'Enter') {
        // Solo si no estamos en un input
        const target = e.target as HTMLElement;
        if (target.tagName === 'INPUT') return;
        
        e.preventDefault();
        if (puedeConfirmar && !procesando) {
          confirmarVenta();
        }
        return;
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [estado, activeSection, puedeConfirmar, procesando, confirmarVenta]);

  // Seleccionar EFECTIVO por defecto al abrir el panel y activar sección
  useEffect(() => {
    if (estado === 'CALCULANDO_PAGO') {
      if (!metodoPago) {
        setMetodoPago('EFECTIVO');
      }
      setActiveSection('payment'); // Auto-activar sección de pago
    }
  }, [estado, metodoPago, setMetodoPago, setActiveSection]);

  // Para débito/crédito/transferencia: el monto pagado ES el total exacto
  useEffect(() => {
    if (
      metodoPago &&
      metodoPago !== 'EFECTIVO' &&
      metodoPago !== 'MIXTO'
    ) {
      setMontoPagado(resumen.total);
    }
  }, [metodoPago, resumen.total, setMontoPagado]);

  if (estado !== 'CALCULANDO_PAGO' && estado !== 'PROCESANDO') return null;

  const esMixto    = metodoPago === 'MIXTO';
  const esEfectivo = metodoPago === 'EFECTIVO';
  const esExacto   = !esEfectivo && !esMixto && metodoPago !== null;

  // MIXTO
  const sumaPagos          = pagos.reduce((s, p) => s + p.monto, 0);
  const mixtoInsuficiente  = esMixto && sumaPagos < resumen.total;
  const cambioMixto        = esMixto && sumaPagos > resumen.total ? sumaPagos - resumen.total : 0;
  const tieneEfectivoMixto = esMixto && pagos.some(p => p.metodo === 'EFECTIVO');

  return (
    <div className={styles.wrapper} onClick={handleClick}>
      <h3 className={styles.titulo}>Método de pago</h3>

      {/* ── Selector de método ── */}
      <div className={styles.metodos}>
        {METODOS.map((m) => (
          <button
            key={m.value}
            className={`${styles.btnMetodo} ${metodoPago === m.value ? styles.activo : ''}`}
            onClick={() => setMetodoPago(m.value)}
            disabled={procesando}
          >
            {m.label}
          </button>
        ))}
        <button
          className={`${styles.btnMetodo} ${esMixto ? styles.activo : ''}`}
          onClick={() => setMetodoPago('MIXTO')}
          disabled={procesando}
        >
          🔀 Mixto
        </button>
      </div>

      {/* ── EFECTIVO: campo de monto + cambio ── */}
      {esEfectivo && (
        <div className={styles.campoMonto}>
          <label className={styles.label}>Monto recibido</label>
          <input
            type="number"
            min={0}
            value={montoPagado || ''}
            onChange={(e) => {
              const val = parseFloat(e.target.value);
              setMontoPagado(isNaN(val) || val < 0 ? 0 : val);
            }}
            className={styles.input}
            placeholder={formatearPrecio(resumen.total)}
            disabled={procesando}
            autoFocus
          />
          <p className={`${styles.cambio} ${montoPagado < resumen.total ? styles.insuficiente : styles.suficiente}`}>
            {montoPagado < resumen.total
              ? `Monto insuficiente — faltan ${formatearPrecio(resumen.total - montoPagado)}`
              : `Cambio: ${formatearPrecio(cambio)}`}
          </p>
        </div>
      )}

      {/* ── DÉBITO / CRÉDITO / TRANSFERENCIA: cobro exacto, sin campo ── */}
      {esExacto && (
        <div className={styles.infoExacto}>
          <p className={styles.labelExacto}>Total a cobrar</p>
          <p className={styles.montoExacto}>{formatearPrecio(resumen.total)}</p>
          <p className={styles.notaExacto}>
            {metodoPago === 'TARJETA_DEBITO'   && 'El datáfono cargará el monto exacto a la tarjeta débito.'}
            {metodoPago === 'TARJETA_CREDITO'  && 'El datáfono cargará el monto exacto a la tarjeta crédito.'}
            {metodoPago === 'TRANSFERENCIA'    && 'El cliente transferirá el monto exacto.'}
          </p>
        </div>
      )}

      {/* ── MIXTO: filas de pago ── */}
      {esMixto && (
        <div className={styles.mixto}>
          {pagos.map((pago, idx) => (
            <div key={idx} className={styles.filaPago}>
              <select
                value={pago.metodo}
                onChange={(e) =>
                  actualizarPago(idx, { ...pago, metodo: e.target.value as Exclude<MetodoPago, 'MIXTO'> })
                }
                className={styles.selectMetodo}
                disabled={procesando}
              >
                {METODOS.map((m) => (
                  <option key={m.value} value={m.value}>{m.label}</option>
                ))}
              </select>
              <input
                type="number"
                min={0}
                value={pago.monto || ''}
                onChange={(e) => {
                  const val = parseFloat(e.target.value);
                  actualizarPago(idx, { ...pago, monto: isNaN(val) || val < 0 ? 0 : val });
                }}
                className={styles.inputMixto}
                placeholder="0"
                disabled={procesando}
              />
              <button
                className={styles.btnEliminarPago}
                onClick={() => eliminarPago(idx)}
                disabled={procesando}
                aria-label="Eliminar pago"
              >✕</button>
            </div>
          ))}

          <button
            className={styles.btnAgregarPago}
            onClick={() => agregarPago({ metodo: 'EFECTIVO', monto: 0 })}
            disabled={procesando}
          >
            + Agregar pago
          </button>

          <p className={`${styles.cambio} ${mixtoInsuficiente ? styles.insuficiente : styles.suficiente}`}>
            {mixtoInsuficiente
              ? `Faltan ${formatearPrecio(resumen.total - sumaPagos)}`
              : tieneEfectivoMixto && cambioMixto > 0
              ? `Cubierto ✓ — Cambio efectivo: ${formatearPrecio(cambioMixto)}`
              : `Cubierto ✓`}
          </p>
        </div>
      )}

      <button
        className={styles.btnConfirmar}
        onClick={confirmarVenta}
        disabled={!puedeConfirmar || procesando}
        aria-busy={procesando}
      >
        {procesando ? 'Procesando...' : 'Confirmar venta'}
      </button>
    </div>
  );
}
