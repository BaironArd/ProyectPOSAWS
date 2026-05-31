import type { IVentaPort, ConfirmarVentaPayload, ConfirmarVentaResult } from '@domain/ports/IVentaPort';
import { toPosApiError } from './toPosApiError';
import { API_BASE_URL } from '../../config';

/**
 * Adaptador que conecta al API Gateway de AWS.
 * Endpoint: POST /api/v1/sales
 *
 * La Lambda espera:
 * {
 *   paymentMethod: string,
 *   amountPaid: number,
 *   items: [{ productId: string, name: string, quantity: number, unitPrice: number }]
 * }
 */
export class VentaAdapter implements IVentaPort {
  async confirmar(payload: ConfirmarVentaPayload): Promise<ConfirmarVentaResult> {
    const body = {
      paymentMethod: payload.metodoPago === 'EFECTIVO'        ? 'CASH'
                   : payload.metodoPago === 'TARJETA_DEBITO'  ? 'CARD'
                   : payload.metodoPago === 'TARJETA_CREDITO' ? 'CARD'
                   : payload.metodoPago === 'TRANSFERENCIA'   ? 'TRANSFER'
                   : 'CASH',
      amountPaid: payload.montoPagado,
      items: payload.carrito.map(i => ({
        productId:  String(i.productoId),
        name:       i.nombre,
        quantity:   i.cantidad,
        unitPrice:  i.precioUnitario,
      })),
    };

    const res = await fetch(`${API_BASE_URL}/sales`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });

    if (!res.ok) throw await toPosApiError(res);

    const data = await res.json() as { success: boolean; data: { id: string } };
    return { ok: true, ventaId: data.data.id };
  }
}

export const ventaAdapter = new VentaAdapter();
