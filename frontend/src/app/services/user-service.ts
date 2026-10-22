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
}
