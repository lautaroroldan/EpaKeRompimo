import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, PLATFORM_ID, signal } from '@angular/core';
import { tap } from 'rxjs';
import { API_URL } from '../config/api';
import {
  LoginRequest,
  LoginResponse,
  Session,
  UserRequest,
  UserResponse,
} from '../interfaces/User';

const STORAGE_KEY = 'epakerompimo.session';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  // La app tiene SSR: en el servidor no existe localStorage, así que solo lo usamos en el navegador
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  private readonly _session = signal<Session | null>(null);
  readonly session = this._session.asReadonly();
  readonly isLoggedIn = computed(() => this._session() !== null);
  readonly token = computed(() => this._session()?.token ?? null);

  constructor() {
    if (this.isBrowser) {
      this._session.set(this.readStorage());
    }
  }

  login(credentials: LoginRequest) {
    return this.http.post<LoginResponse>(`${API_URL}/users/login`, credentials).pipe(
      tap((res) =>
        this.setSession({
          token: res.token,
          id: res.idUsuario,
          username: res.username,
          icon: res.icon,
        }),
      ),
    );
  }

  register(request: UserRequest) {
    return this.http.post<UserResponse>(`${API_URL}/users`, request);
  }

  logout() {
    this._session.set(null);
    this.removeStorage();
  }

  // Después de editar el perfil, actualiza lo que se muestra sin tener que volver a loguearse
  updateSession(data: Pick<Session, 'username' | 'icon'>) {
    const current = this._session();
    if (current) {
      this.setSession({ ...current, ...data });
    }
  }

  private setSession(session: Session) {
    this._session.set(session);
    if (this.isBrowser) {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
      } catch {
        // modo privado o storage lleno: la sesión sigue viva en memoria
      }
    }
  }

  private readStorage(): Session | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return null;

      const s = JSON.parse(raw) as Session;
      const valid =
        typeof s.token === 'string' && typeof s.id === 'number' && typeof s.username === 'string';
      if (!valid || this.isExpired(s.token)) {
        this.removeStorage();
        return null;
      }
      return s;
    } catch {
      return null;
    }
  }

  private removeStorage() {
    if (!this.isBrowser) return;
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      // nada que hacer
    }
  }

  // El JWT lleva su vencimiento ("exp", en segundos) en la parte del medio, codificada en Base64
  private isExpired(token: string): boolean {
    try {
      const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const { exp } = JSON.parse(atob(payload)) as { exp?: number };
      return !exp || exp * 1000 <= Date.now();
    } catch {
      return true;
    }
  }
}
