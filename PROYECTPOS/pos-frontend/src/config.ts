/**
 * Configuración centralizada del API Gateway.
 *
 * La variable de entorno VITE_API_BASE_URL debe apuntar hasta /api/v1:
 *   VITE_API_BASE_URL=https://<id>.execute-api.us-east-1.amazonaws.com/Prod/api/v1
 *
 * Los adaptadores construyen las rutas finales añadiendo el recurso:
 *   ${API_BASE_URL}/products   → GET  /api/v1/products
 *   ${API_BASE_URL}/sales      → POST /api/v1/sales
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ||
  'https://your-api-id.execute-api.us-east-1.amazonaws.com/Prod/api/v1';
