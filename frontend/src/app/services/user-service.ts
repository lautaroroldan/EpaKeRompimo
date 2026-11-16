import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../config/api';
import { UserResponse, UserUpdateRequest } from '../interfaces/User';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly http = inject(HttpClient);

  findById(id: number) {
    return this.http.get<UserResponse>(`${API_URL}/users/${id}`);
  }

  update(id: number, request: UserUpdateRequest) {
    return this.http.put<UserResponse>(`${API_URL}/users/${id}`, request);
  }

  delete(id: number) {
    return this.http.delete<void>(`${API_URL}/users/${id}`);
  }

  existsByUsername(username: string) {
    return this.http.get<boolean>(`${API_URL}/users/exists/${encodeURIComponent(username)}`);
  }
   verifyEmail(token: string) {
    return this.http.post<void>(`${API_URL}/users/verify-email`, null, { params: { token } });
  }

  resendVerification(id: number) {
    return this.http.post<void>(`${API_URL}/users/${id}/resend-verification`, null);
  }
  requestEmailUnlink(id: number) {
    return this.http.post<void>(`${API_URL}/users/${id}/email/unlink-request`, null);
  }

  unlinkEmail(token: string) {
    return this.http.post<void>(`${API_URL}/users/unlink-email`, null, { params: { token } });
  }

  setNotificationsEnabled(id: number, enabled: boolean) {
    return this.http.put<void>(`${API_URL}/users/${id}/notifications`, { enabled });
  }
}
