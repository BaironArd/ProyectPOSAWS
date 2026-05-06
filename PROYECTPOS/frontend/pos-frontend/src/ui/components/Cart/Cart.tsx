import { usePOSStore } from '@application/store/usePOSStore';
import { formatearPrecio } from '@ui/utils/formato';
import styles from './Cart.module.css';

export function Cart() {
  const carrito = usePOSStore((s) => s.carrito);
  const estado = usePOSStore((s) => s.estado);
  const modificarCantidad = usePOSStore((s) => s.modificarCantidad);
  const eliminarDelCarrito = usePOSStore((s) => s.eliminarDelCarrito);
  const setEstado = usePOSStore((s) => s.setEstado);

  if (estado !== 'CARRITO_ACTIVO' && estado !== 'CALCULANDO_PAGO') return null;
  if (carrito.length === 0) return null;

  return (
    <div className={styles.wrapper}>
      <h3 className={styles.titulo}>Carrito</h3>
      <table className={styles.tabla} aria-label="Carrito de compras">
        <thead>
          <tr>
            <th>Producto</th>
            <th>Precio unit.</th>
            <th>Cantidad</th>
            <th>Subtotal</th>
            <th>Eliminar</th>
          </tr>
        </thead>
        <tbody>
          {carrito.map((item) => (
            <tr key={item.productoId}>
              <td>{item.nombre}</td>
              <td>{formatearPrecio(item.precioUnitario)}</td>
              <td>
                <div className={styles.cantidadControles}>
                  <button
                    className={styles.btnCantidad}
                    onClick={() => modificarCantidad(item.productoId, item.cantidad - 1)}
                    aria-label={`Reducir cantidad de ${item.nombre}`}
                  >
                    −
                  </button>
                  <input
                    type="number"
                    min={0}
                    value={item.cantidad}
                    onChange={(e) => {
                      const val = parseInt(e.target.value, 10);
                      if (!isNaN(val) && val >= 0) {
                        modificarCantidad(item.productoId, val);
                      }
                    }}
                    className={styles.inputCantidad}
                    aria-label={`Cantidad de ${item.nombre}`}
                  />
                  <button
                    className={styles.btnCantidad}
                    onClick={() => modificarCantidad(item.productoId, item.cantidad + 1)}
                    aria-label={`Aumentar cantidad de ${item.nombre}`}
                  >
                    +
                  </button>
                </div>
              </td>
              <td>{formatearPrecio(item.subtotal)}</td>
              <td>
                <button
                  className={styles.btnEliminar}
                  onClick={() => eliminarDelCarrito(item.productoId)}
                  aria-label={`Eliminar ${item.nombre} del carrito`}
                >
                  ✕
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {estado === 'CARRITO_ACTIVO' && (
        <button
          className={styles.btnProceder}
          onClick={() => setEstado('CALCULANDO_PAGO')}
        >
          Proceder al pago
        </button>
      )}
    </div>
  );
}
