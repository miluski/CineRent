export const API_BASE_URL = import.meta.env.VITE_BACKEND_URL || 'https://localhost:4443';

export const STATIC_BASE_URL = (
  import.meta.env.VITE_BACKEND_URL || 'https://localhost:4443'
).replace(/\/api\/v1\/?$/, '');
