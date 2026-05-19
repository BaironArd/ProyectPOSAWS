import { PosApiError } from '@domain/errors/PosApiError';

type ErrorBody = { error?: { codigo?: string; mensaje?: string } };

export async function toPosApiError(res: Response): Promise<PosApiError> {
  try {
    const j = (await res.json()) as ErrorBody;
    const codigo = j.error?.codigo ?? 'ERROR_DESCONOCIDO';
    const mensaje = j.error?.mensaje ?? res.statusText ?? 'Error';
    return new PosApiError(codigo, mensaje, res.status);
  } catch {
    return new PosApiError(
      'ERROR_DESCONOCIDO',
      res.statusText || 'Error de comunicación con el servidor',
      res.status
    );
  }
}
