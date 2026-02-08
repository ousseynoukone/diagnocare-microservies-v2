import { apiRequest } from './http';
import { tokenStorage } from './storage';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  lang: string;
  password: string;
  roleId: number;
}

export interface AuthResponse {
  token: string;
  tokenValidity: number;
  refreshToken: string;
  refreshTokenValidity: number;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    phoneNumber?: string;
    lang?: string;
  };
}

export interface Role {
  id: number;
  name: string;
}

export async function login(payload: LoginRequest): Promise<AuthResponse> {
  const data = await apiRequest<AuthResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload)
  }, false);
  tokenStorage.setAccessToken(data.token);
  tokenStorage.setRefreshToken(data.refreshToken);
  tokenStorage.setUser(data.user);
  return data;
}

export async function register(payload: RegisterRequest): Promise<AuthResponse> {
  const data = await apiRequest<AuthResponse>('/api/v1/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload)
  }, false);
  if (data.token && data.refreshToken) {
    tokenStorage.setAccessToken(data.token);
    tokenStorage.setRefreshToken(data.refreshToken);
    tokenStorage.setUser(data.user);
  }
  return data;
}

export async function refreshToken(): Promise<AuthResponse> {
  const refreshTokenValue = tokenStorage.getRefreshToken();
  if (!refreshTokenValue) {
    throw new Error('Refresh token manquant');
  }
  const data = await apiRequest<AuthResponse>('/api/v1/auth/refresh-token', {
    method: 'POST',
    body: JSON.stringify({ refreshToken: refreshTokenValue })
  }, false);
  tokenStorage.setAccessToken(data.token);
  tokenStorage.setRefreshToken(data.refreshToken);
  tokenStorage.setUser(data.user);
  return data;
}

export async function fetchRoles(): Promise<Role[]> {
  return apiRequest<Role[]>('/api/v1/auth/roles', {
    method: 'GET'
  }, false);
}

export function logout() {
  tokenStorage.clear();
}
