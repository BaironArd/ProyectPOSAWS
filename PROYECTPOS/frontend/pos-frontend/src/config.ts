/**
 * Configuración del API Gateway
 * 
 * La URL base se obtiene de la variable de entorno VITE_API_BASE_URL
 * Si no está definida, usa un valor por defecto para desarrollo local
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 
  'https://your-api-id.execute-api.us-east-1.amazonaws.com/Prod';

/**
 * Endpoints del API
 */
export const API_ENDPOINTS = {
  productos: `${API_BASE_URL}/api/v1/productos`,
  ventas: `${API_BASE_URL}/api/v1/ventas`,
} as const;
