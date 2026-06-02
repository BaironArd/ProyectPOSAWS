import type { IImpresionPort } from '@domain/ports/IImpresionPort';

export class ImpresionAdapter implements IImpresionPort {
  async imprimir(_ventaId: string): Promise<void> {
    window.print();
  }
}

export const impresionAdapter = new ImpresionAdapter();
