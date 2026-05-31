/**
 * Cliente HTTP simplificado para AWS API Gateway.
 * Sin autenticación JWT — los endpoints son públicos.
 */
export async function httpFetch(url: string, options: RequestInit = {}): Promise<Response> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> ?? {}),
  };

  return fetch(url, { ...options, headers });
}
