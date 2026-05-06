/**
 * Formatea un número como precio en pesos colombianos.
 * Ejemplo: 100000 → "$100.000"
 */
export function formatearPrecio(valor: number): string {
  return '$' + valor.toLocaleString('es-CO', { maximumFractionDigits: 0 });
}

/**
 * Formatea una fecha ISO 8601 a formato legible.
 * Ejemplo: "2025-01-15T10:30:00Z" → "15/01/2025 10:30"
 */
export function formatearFecha(iso: string): string {
  const fecha = new Date(iso);
  const dia = String(fecha.getDate()).padStart(2, '0');
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const anio = fecha.getFullYear();
  const hora = String(fecha.getHours()).padStart(2, '0');
  const min = String(fecha.getMinutes()).padStart(2, '0');
  return `${dia}/${mes}/${anio} ${hora}:${min}`;
}
