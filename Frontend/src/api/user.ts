import { apiRequest } from './http';

export interface UserUpdateRequest {
  email?: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  lang?: string;
  password?: string;
}

export async function updateUser(userId: number, payload: UserUpdateRequest) {
  return apiRequest(`/api/v1/auth/users/${userId}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export async function deleteUser(userId: number) {
  return apiRequest(`/api/v1/auth/users/${userId}`, {
    method: 'DELETE'
  });
}
