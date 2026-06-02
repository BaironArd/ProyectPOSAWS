import { PosApiError } from '@domain/errors/PosApiError';

// La Lambda puede devolver el error como string o como objeto { codigo, mensaje }
type ErrorBody = {
  success?: boolean;
  error?: string | { codigo?: string; mensaje?: string };
};

export async function toPosApiError(res: Response): Promise<PosApiError> {
  try {
    const j = (await res.json()) as ErrorBody;

    // Formato real de la Lambda: { "error": "mensaje string", "success": false }
    if (typeof j.error === 'string') {
      // Intentar mapear mensajes conocidos de la Lambda a códigos internos
      const msg = j.error;
      let codigo = 'ERROR_DESCONOCIDO';
      if (msg.toLowerCase().includes('monto insuficiente'))  codigo = 'VENTA_MONTO_INSUFICIENTE';
      else if (msg.toLowerCase().includes('stock'))          codigo = 'STOCK_INSUFICIENTE';
      else if (msg.toLowerCase().includes('no encontrad'))   codigo = 'PRODUCTO_NO_ENCONTRADO';
      else if (msg.toLowerCase().includes('ya fue pagada'))  codigo = 'VENTA_DUPLICADA';
      else if (res.status === 400)                           codigo = 'VALIDACION_FALLIDA';
      else if (res.status === 404)                           codigo = 'VENTA_NO_ENCONTRADA';
      else if (res.status >= 500)                            codigo = 'ERROR_INTERNO';
      return new PosApiError(codigo, msg, res.status);
    }

    // Formato extendido: { "error": { "codigo": "...", "mensaje": "..." } }
    if (typeof j.error === 'object' && j.error !== null) {
      const codigo  = j.error.codigo  ?? 'ERROR_DESCONOCIDO';
      const mensaje = j.error.mensaje ?? res.statusText ?? 'Error';
      return new PosApiError(codigo, mensaje, res.status);
    }

    return new PosApiError('ERROR_DESCONOCIDO', res.statusText || 'Error', res.status);
  } catch {
    return new PosApiError(
      'ERROR_DESCONOCIDO',
      res.statusText || 'Error de comunicación con el servidor',
      res.status
    );
  }
}
