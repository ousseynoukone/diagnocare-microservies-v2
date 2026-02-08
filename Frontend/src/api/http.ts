import { tokenStorage } from './storage';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8765';

export interface ApiEnvelope<T> {
  message?: string;
  statusCode?: number;
  data?: T;
}

export class ApiError extends Error {
  status?: number;
  constructor(message: string, status?: number) {
    super(message);
    this.status = status;
  }
}

async function parseJson<T>(response: Response): Promise<ApiEnvelope<T>> {
  const text = await response.text();
  if (!text) {
    return {};
  }
  try {
    return JSON.parse(text) as ApiEnvelope<T>;
  } catch {
    return {};
  }
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
  withAuth = true
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> | undefined)
  };

  if (withAuth) {
    const token = tokenStorage.getAccessToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers
  });

  const payload = await parseJson<T>(response);
  if (!response.ok) {
    throw new ApiError(payload.message || 'Erreur API', response.status);
  }

  return (payload.data ?? ({} as T));
}
