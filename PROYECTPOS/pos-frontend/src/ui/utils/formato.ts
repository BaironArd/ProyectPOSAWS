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
  return new Intl.DateTimeFormat('es-CO', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(iso));
}
