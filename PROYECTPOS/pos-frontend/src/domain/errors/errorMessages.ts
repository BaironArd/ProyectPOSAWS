/**
 * Mensajes legibles por código de error del backend (SPEC-BE-005 / SPEC-007).
 * Si no hay entrada, se usa el mensaje que envía la API.
 */
const MAP: Record<string, string> = {
  PRODUCTO_NO_ENCONTRADO: 'El producto no existe o fue eliminado.',
  STOCK_INSUFICIENTE: 'No hay stock suficiente para uno o más productos.',
  VENTA_MONTO_INSUFICIENTE: 'El monto pagado es menor que el total de la venta.',
  QUERY_DEMASIADO_CORTA: 'Escribe al menos 2 caracteres para buscar.',
  CARRITO_VACIO: 'No hay productos en el carrito.',
  CANTIDAD_INVALIDA: 'Hay cantidades inválidas en el carrito.',
  VALIDACION_FALLIDA: 'Los datos enviados no son válidos.',
  VENTA_NO_ENCONTRADA: 'No se encontró esa venta.',
  CONFLICTO_STOCK: 'Otro proceso modificó el inventario. Intenta de nuevo.',
  VENTA_DUPLICADA: 'Esta venta ya fue registrada.',
  CREDENCIALES_INVALIDAS: 'Usuario o contraseña incorrectos.',
  TOKEN_INVALIDO: 'Sesión expirada o token inválido. Vuelve a iniciar sesión.',
  ACCESO_DENEGADO: 'No tienes permiso para esta operación.',
  VENTA_YA_DEVUELTA: 'Esta venta ya fue devuelta.',
  VENTA_NO_DEVOLVIBLE: 'Esta venta no se puede devolver en su estado actual.',
  PRODUCTO_DUPLICADO: 'Ya existe un producto activo con ese nombre.',
  ERROR_INTERNO: 'Error en el servidor. Intenta más tarde.',
  LOAD_FAILED: 'No se pudieron cargar los productos.',
  HISTORIAL_NO_DISPONIBLE: 'No se pudo cargar el historial de ventas.',
  INVENTARIO_ERROR: 'No se pudo cargar el inventario.',
  REPORTE_ERROR: 'No se pudo generar el reporte.',
  DEVOLUCION_ERROR: 'No se pudieron cargar los datos de la devolución.',
  DEVOLUCION_FALLIDA: 'No se pudo completar la devolución.',
  CONFIRMACION_FALLIDA: 'No se pudo confirmar la venta.',
  IMPRESION_ERROR: 'No se pudo imprimir el recibo.',
  ERROR_DESCONOCIDO: 'Ha ocurrido un error inesperado.',
};

export function mensajeErrorApi(codigo: string, mensajeBackend: string): string {
  const local = MAP[codigo];
  if (local) return local;
  const trimmed = mensajeBackend.trim();
  if (trimmed.length > 0) return trimmed;
  return MAP['ERROR_DESCONOCIDO'] ?? 'Ha ocurrido un error inesperado.';
}
