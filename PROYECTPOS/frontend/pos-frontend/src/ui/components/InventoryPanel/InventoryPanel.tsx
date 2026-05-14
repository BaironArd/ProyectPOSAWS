import { useState } from 'react';
import { usePOSStore } from '@application/store/usePOSStore';
import { useInventory } from '@application/hooks/useInventory';
import type { IInventarioPort, NuevoProducto } from '@domain/ports/IInventarioPort';
import { formatearPrecio } from '@ui/utils/formato';
import styles from './InventoryPanel.module.css';

interface Props { inventarioPort: IInventarioPort; }

const VACIO: NuevoProducto = { nombre: '', precio: 0, stock: 0 };

export function InventoryPanel({ inventarioPort }: Props) {
  const estado = usePOSStore((s) => s.estado);
  const setEstado = usePOSStore((s) => s.setEstado);
  const { productos, cargando, crear, actualizar, toggleActivo } = useInventory(inventarioPort);

  const [modal, setModal] = useState(false);
  const [form, setForm] = useState<NuevoProducto>(VACIO);
  const [editId, setEditId] = useState<number | null>(null);
  const [errorForm, setErrorForm] = useState<string | null>(null);

  if (estado !== 'INVENTARIO') return null;

  const precioValido = form.precio > 0;
  const puedeGuardar = form.nombre.trim().length > 0 && precioValido;

  async function handleGuardar() {
    setErrorForm(null);
    try {
      if (editId !== null) {
        await actualizar(editId, form);
      } else {
        await crear(form);
      }
      setModal(false);
      setForm(VACIO);
      setEditId(null);
    } catch (err) {
      setErrorForm(err instanceof Error ? err.message : 'Error al guardar');
    }
  }

  return (
    <div className={styles.wrapper}>
      <div className={styles.header}>
        <h2 className={styles.titulo}>Inventario</h2>
        <div className={styles.acciones}>
          <button className={styles.btnNuevo} onClick={() => { setForm(VACIO); setEditId(null); setModal(true); }}>
            + Nuevo producto
          </button>
          <button className={styles.btnVolver} onClick={() => setEstado('IDLE')}>← Volver</button>
        </div>
      </div>

      {cargando ? (
        <p className={styles.cargando}>Cargando inventario...</p>
      ) : (
        <table className={styles.tabla} aria-label="Inventario de productos">
          <thead>
            <tr><th>Código</th><th>Nombre</th><th>Precio</th><th>Stock</th><th>Estado</th><th>Acciones</th></tr>
          </thead>
          <tbody>
            {productos.map((p) => (
              <tr key={p.id} className={!p.activo ? styles.inactivo : ''}>
                <td className={styles.codigo}>{p.id}</td>
                <td>{p.nombre}</td>
                <td>{formatearPrecio(p.precio)}</td>
                <td>{p.stock}</td>
                <td><span className={p.activo ? styles.badgeActivo : styles.badgeInactivo}>{p.activo ? 'Activo' : 'Inactivo'}</span></td>
                <td>
                  <button className={styles.btnEditar} onClick={() => { setForm({ nombre: p.nombre, precio: p.precio, stock: p.stock }); setEditId(p.id); setModal(true); }}>Editar</button>
                  <button className={styles.btnToggle} onClick={() => toggleActivo(p.id)}>{p.activo ? 'Desactivar' : 'Activar'}</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {modal && (
        <div className={styles.overlay} role="dialog" aria-modal="true" aria-label="Formulario de producto">
          <div className={styles.modal}>
            <h3>{editId ? 'Editar producto' : 'Nuevo producto'}</h3>
            <label className={styles.label}>Nombre</label>
            <input className={styles.input} value={form.nombre} onChange={(e) => setForm({ ...form, nombre: e.target.value })} />
            <label className={styles.label}>Precio</label>
            <input className={styles.input} type="number" min={1} value={form.precio || ''} onChange={(e) => setForm({ ...form, precio: parseFloat(e.target.value) || 0 })} />
            {!precioValido && form.precio !== 0 && <p className={styles.errorCampo}>El precio debe ser mayor a 0</p>}
            <label className={styles.label}>Stock</label>
            <input className={styles.input} type="number" min={0} value={form.stock} onChange={(e) => setForm({ ...form, stock: parseInt(e.target.value) || 0 })} />
            {errorForm && <p className={styles.errorCampo}>{errorForm}</p>}
            <div className={styles.modalAcciones}>
              <button className={styles.btnGuardar} onClick={handleGuardar} disabled={!puedeGuardar}>Guardar</button>
              <button className={styles.btnCancelarModal} onClick={() => setModal(false)}>Cancelar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
