/** Error tipado devuelto por los adapters HTTP cuando la API responde con cuerpo `{ error: { codigo, mensaje } }`. */
export class PosApiError extends Error {
  readonly codigo: string;
  readonly httpStatus: number;

  constructor(codigo: string, message: string, httpStatus: number) {
    super(message);
    this.name = 'PosApiError';
    this.codigo = codigo;
    this.httpStatus = httpStatus;
  }
}
