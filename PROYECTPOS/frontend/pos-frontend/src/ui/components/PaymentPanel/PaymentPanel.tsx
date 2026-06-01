import { useEffect, useState } from 'react';
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

  // Estado para navegación dentro del panel de pago
  const [focusedElementIndex, setFocusedElementIndex] = useState(0);
  const [metodoSeleccionado, setMetodoSeleccionado] = useState(false);

  // Activar sección al hacer clic
  const handleClick = () => {
    setActiveSection('payment');
  };

  // Calcular tipos de pago
  const esMixto    = metodoPago === 'MIXTO';
  const esEfectivo = metodoPago === 'EFECTIVO';
  const esExacto   = !esEfectivo && !esMixto && metodoPago !== null;

  // Resetear navegación cuando cambia el método o se abre el panel
  useEffect(() => {
    if (estado === 'CALCULANDO_PAGO') {
      setFocusedElementIndex(0);
      setMetodoSeleccionado(false);
    }
  }, [estado]);

  // Calcular elementos navegables según el estado actual
  const getNavigableElements = () => {
    const elements: string[] = [];
    
    // Si no se ha seleccionado método, solo mostrar botones de método
    if (!metodoSeleccionado) {
      METODOS.forEach((m) => elements.push(`metodo-${m.value}`));
      elements.push('metodo-MIXTO');
      return elements;
    }
    
    // Si ya se seleccionó método, mostrar elementos según el tipo
    if (esEfectivo) {
      elements.push('input-efectivo');
      elements.push('btn-confirmar');
    } else if (esMixto) {
      pagos.forEach((_, idx) => {
        elements.push(`pago-select-${idx}`);
        elements.push(`pago-input-${idx}`);
        elements.push(`pago-delete-${idx}`);
      });
      elements.push('btn-agregar-pago');
      if (puedeConfirmar) {
        elements.push('btn-confirmar');
      }
    } else if (esExacto) {
      // Débito, crédito, transferencia → directo a confirmar
      elements.push('btn-confirmar');
    }
    
    return elements;
  };

  // Navegación con teclado en panel de pago
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (estado !== 'CALCULANDO_PAGO') return;
      if (activeSection !== 'payment') return;

      const elements = getNavigableElements();
      const currentElement = elements[focusedElementIndex];

      // Backspace → Retroceder en navegación
      if (e.key === 'Backspace') {
        e.preventDefault();
        
        // Si estamos en la selección de método, no hacer nada
        if (!metodoSeleccionado) return;
        
        // Si estamos en otros elementos, retroceder
        if (focusedElementIndex > 0) {
          setFocusedElementIndex((prev) => prev - 1);
        } else {
          // Si estamos en el primer elemento después de seleccionar método, volver a métodos
          setMetodoSeleccionado(false);
          setFocusedElementIndex(0);
        }
        return;
      }

      // ↑ - Navegar arriba
      if (e.key === 'ArrowUp') {
        e.preventDefault();
        setFocusedElementIndex((prev) => (prev > 0 ? prev - 1 : elements.length - 1));
        return;
      }

      // ↓ - Navegar abajo
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        setFocusedElementIndex((prev) => (prev < elements.length - 1 ? prev + 1 : 0));
        return;
      }

      // Enter - Acción según elemento enfocado
      if (e.key === 'Enter') {
        e.preventDefault();
        
        // Si es un botón de método de pago
        if (currentElement?.startsWith('metodo-')) {
          const metodo = currentElement.replace('metodo-', '') as MetodoPago;
          setMetodoPago(metodo);
          setMetodoSeleccionado(true);
          setFocusedElementIndex(0); // Resetear a primer elemento del siguiente grupo
          return;
        }
        
        // Si es el botón de agregar pago (mixto)
        if (currentElement === 'btn-agregar-pago') {
          agregarPago({ metodo: 'EFECTIVO', monto: 0 });
          return;
        }
        
        // Si es el botón de confirmar
        if (currentElement === 'btn-confirmar' && puedeConfirmar && !procesando) {
          confirmarVenta();
          return;
        }
        
        // Si es un input o select, dejar que el usuario interactúe
        return;
      }

      // Delete - Eliminar pago en mixto
      if (e.key === 'Delete' && currentElement?.startsWith('pago-delete-')) {
        e.preventDefault();
        const idx = parseInt(currentElement.replace('pago-delete-', ''));
        eliminarPago(idx);
        return;
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [estado, activeSection, focusedElementIndex, metodoSeleccionado, metodoPago, pagos, puedeConfirmar, procesando, confirmarVenta, setMetodoPago, agregarPago, eliminarPago, esEfectivo, esMixto, esExacto]);

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
        {METODOS.map((m) => {
          const elementId = `metodo-${m.value}`;
          const isFocused = !metodoSeleccionado && getNavigableElements()[focusedElementIndex] === elementId;
          return (
            <button
              key={m.value}
              className={`${styles.btnMetodo} ${metodoPago === m.value ? styles.activo : ''} ${isFocused ? styles.navegando : ''}`}
              onClick={() => {
                setMetodoPago(m.value);
                setMetodoSeleccionado(true);
                setFocusedElementIndex(0);
              }}
              disabled={procesando}
            >
              {m.label}
            </button>
          );
        })}
        <button
          className={`${styles.btnMetodo} ${esMixto ? styles.activo : ''} ${!metodoSeleccionado && getNavigableElements()[focusedElementIndex] === 'metodo-MIXTO' ? styles.navegando : ''}`}
          onClick={() => {
            setMetodoPago('MIXTO');
            setMetodoSeleccionado(true);
            setFocusedElementIndex(0);
          }}
          disabled={procesando}
        >
          🔀 Mixto
        </button>
      </div>

      {/* ── EFECTIVO: campo de monto + cambio ── */}
      {esEfectivo && metodoSeleccionado && (
        <div className={styles.campoMonto}>
          <label className={styles.label}>Monto recibido</label>
          <input
            type="text"
            inputMode="decimal"
            value={montoPagado || ''}
            onChange={(e) => {
              const val = parseFloat(e.target.value.replace(/[^0-9.]/g, ''));
              setMontoPagado(isNaN(val) || val < 0 ? 0 : val);
            }}
            onFocus={() => {
              const elements = getNavigableElements();
              const inputIndex = elements.indexOf('input-efectivo');
              if (inputIndex !== -1) setFocusedElementIndex(inputIndex);
            }}
            className={`${styles.input} ${getNavigableElements()[focusedElementIndex] === 'input-efectivo' ? styles.inputFocused : ''}`}
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
      {esExacto && metodoSeleccionado && (
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
      {esMixto && metodoSeleccionado && (
        <div className={styles.mixto}>
          {pagos.map((pago, idx) => {
            const selectId = `pago-select-${idx}`;
            const inputId = `pago-input-${idx}`;
            const deleteId = `pago-delete-${idx}`;
            const currentElement = getNavigableElements()[focusedElementIndex];
            
            return (
              <div key={idx} className={styles.filaPago}>
                <select
                  value={pago.metodo}
                  onChange={(e) =>
                    actualizarPago(idx, { ...pago, metodo: e.target.value as Exclude<MetodoPago, 'MIXTO'> })
                  }
                  onFocus={() => {
                    const elements = getNavigableElements();
                    const selectIndex = elements.indexOf(selectId);
                    if (selectIndex !== -1) setFocusedElementIndex(selectIndex);
                  }}
                  className={`${styles.selectMetodo} ${currentElement === selectId ? styles.selectFocused : ''}`}
                  disabled={procesando}
                >
                  {METODOS.map((m) => (
                    <option key={m.value} value={m.value}>{m.label}</option>
                  ))}
                </select>
                <input
                  type="text"
                  inputMode="decimal"
                  value={pago.monto || ''}
                  onChange={(e) => {
                    const val = parseFloat(e.target.value.replace(/[^0-9.]/g, ''));
                    actualizarPago(idx, { ...pago, monto: isNaN(val) || val < 0 ? 0 : val });
                  }}
                  onFocus={() => {
                    const elements = getNavigableElements();
                    const inputIndex = elements.indexOf(inputId);
                    if (inputIndex !== -1) setFocusedElementIndex(inputIndex);
                  }}
                  className={`${styles.inputMixto} ${currentElement === inputId ? styles.inputFocused : ''}`}
                  placeholder="0"
                  disabled={procesando}
                />
                <button
                  className={`${styles.btnEliminarPago} ${currentElement === deleteId ? styles.btnDeleteFocused : ''}`}
                  onClick={() => eliminarPago(idx)}
                  onFocus={() => {
                    const elements = getNavigableElements();
                    const deleteIndex = elements.indexOf(deleteId);
                    if (deleteIndex !== -1) setFocusedElementIndex(deleteIndex);
                  }}
                  disabled={procesando}
                  aria-label="Eliminar pago"
                >✕</button>
              </div>
            );
          })}

          <button
            className={`${styles.btnAgregarPago} ${getNavigableElements()[focusedElementIndex] === 'btn-agregar-pago' ? styles.btnAgregarFocused : ''}`}
            onClick={() => agregarPago({ metodo: 'EFECTIVO', monto: 0 })}
            onFocus={() => {
              const elements = getNavigableElements();
              const btnIndex = elements.indexOf('btn-agregar-pago');
              if (btnIndex !== -1) setFocusedElementIndex(btnIndex);
            }}
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
        className={`${styles.btnConfirmar} ${metodoSeleccionado && getNavigableElements()[focusedElementIndex] === 'btn-confirmar' ? styles.btnConfirmarFocused : ''}`}
        onClick={confirmarVenta}
        onFocus={() => {
          const elements = getNavigableElements();
          const btnIndex = elements.indexOf('btn-confirmar');
          if (btnIndex !== -1) setFocusedElementIndex(btnIndex);
        }}
        disabled={!puedeConfirmar || procesando}
        aria-busy={procesando}
      >
        {procesando ? 'Procesando...' : 'Confirmar venta'}
      </button>
    </div>
  );
}
