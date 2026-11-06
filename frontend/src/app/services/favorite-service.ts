import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { effect, inject, Injectable, signal, untracked } from '@angular/core';
import { environment } from '../../environments/environment';
import { FavoriteEnterprise, FavoriteGame } from '../interfaces/Favorite';
import { errorMessage } from '../utils/http-error';
import { AuthService } from './auth-service';

@Injectable({
  providedIn: 'root',
})
export class FavoriteService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly api = `${environment.api}/favorites`;
  private readonly country = 'AR'; // el mismo que usa el buscador

  // Estado (signals privados: solo el service los modifica)
  private readonly _gameIds = signal<ReadonlySet<string>>(new Set()); // ids de ITAD, para pintar las estrellas
  private readonly _games = signal<FavoriteGame[]>([]); // lista completa (la pantalla de favoritos)
  private readonly _enterprises = signal<FavoriteEnterprise[]>([]);
  private readonly _loading = signal(false);
  private readonly _saving = signal(false);
  private readonly _error = signal<string | null>(null);

  // Lo que ven los componentes (solo lectura)
  readonly games = this._games.asReadonly();
  readonly enterprises = this._enterprises.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly saving = this._saving.asReadonly();
  readonly error = this._error.asReadonly();

  constructor() {
    // Al loguearse trae los ids de favoritos; al cerrar sesión limpia todo,
    // así otra persona que entre después no ve los favoritos de la anterior.
    effect(() => {
      const loggedIn = this.auth.isLoggedIn();
      untracked(() => (loggedIn ? this.loadGameIds() : this.reset()));
    });
  }

  clearError() {
    this._error.set(null);
  }

  // ---------- Juegos ----------

  // Se lee dentro de un template: al ser un signal, la vista se actualiza sola
  isFavoriteGame(itadGameId: string): boolean {
    return this._gameIds().has(itadGameId);
  }

  // GET /api/favorites/games/ids (liviano: no consulta a ITAD)
  loadGameIds() {
    this.http.get<string[]>(`${this.api}/games/ids`).subscribe({
      next: (ids) => this._gameIds.set(new Set(ids)),
      error: () => {
        // si falla, simplemente no se marcan las estrellas
      },
    });
  }

  // GET /api/favorites/games (con título, imagen y precios actuales)
  loadGames() {
    this._loading.set(true);
    this._error.set(null);

    const params = new HttpParams().set('country', this.country);
    this.http.get<FavoriteGame[]>(`${this.api}/games`, { params }).subscribe({
      next: (games) => {
        this._games.set(games);
        this._gameIds.set(new Set(games.map((g) => g.itadGameId)));
        this._loading.set(false);
      },
      error: (err) => {
        this._error.set(errorMessage(err, 'No se pudieron cargar tus juegos favoritos.'));
        this._loading.set(false);
      },
    });
  }

  toggleGame(itadGameId: string) {
    if (this.isFavoriteGame(itadGameId)) {
      this.removeGame(itadGameId);
    } else {
      this.addGame(itadGameId);
    }
  }

  // POST /api/favorites/games
  addGame(itadGameId: string) {
    this._saving.set(true);
    this._error.set(null);

    this.http.post<FavoriteGame>(`${this.api}/games`, { itadGameId }).subscribe({
      next: () => {
        this.markGame(itadGameId);
        this._saving.set(false);
      },
      error: (err) => {
        // 409: ya estaba guardado, la estrella tiene que quedar marcada igual
        if (err instanceof HttpErrorResponse && err.status === 409) {
          this.markGame(itadGameId);
        } else {
          this._error.set(errorMessage(err, 'No se pudo guardar el juego en favoritos.'));
        }
        this._saving.set(false);
      },
    });
  }

  // DELETE /api/favorites/games/{itadGameId}
  removeGame(itadGameId: string) {
    this._saving.set(true);
    this._error.set(null);

    this.http.delete<void>(`${this.api}/games/${encodeURIComponent(itadGameId)}`).subscribe({
      next: () => {
        this.unmarkGame(itadGameId);
        this._saving.set(false);
      },
      error: (err) => {
        // 404: ya no estaba guardado, lo sacamos de la pantalla igual
        if (err instanceof HttpErrorResponse && err.status === 404) {
          this.unmarkGame(itadGameId);
        } else {
          this._error.set(errorMessage(err, 'No se pudo quitar el juego de favoritos.'));
        }
        this._saving.set(false);
      },
    });
  }

  // ---------- Empresas ----------

  // GET /api/favorites/enterprises
  loadEnterprises() {
    this.http.get<FavoriteEnterprise[]>(`${this.api}/enterprises`).subscribe({
      next: (list) => this._enterprises.set(list),
      error: (err) =>
        this._error.set(errorMessage(err, 'No se pudieron cargar tus empresas favoritas.')),
    });
  }

  // POST /api/favorites/enterprises
  addEnterprise(rawgEnterpriseId: number) {
    this._saving.set(true);
    this._error.set(null);

    this.http.post<FavoriteEnterprise>(`${this.api}/enterprises`, { rawgEnterpriseId }).subscribe({
      next: (created) => {
        this._enterprises.update((list) => [created, ...list]);
        this._saving.set(false);
      },
      error: (err) => {
        this._error.set(errorMessage(err, 'No se pudo guardar la empresa en favoritos.'));
        this._saving.set(false);
      },
    });
  }

  // DELETE /api/favorites/enterprises/{rawgEnterpriseId}
  removeEnterprise(rawgEnterpriseId: number) {
    this._saving.set(true);
    this._error.set(null);

    this.http.delete<void>(`${this.api}/enterprises/${rawgEnterpriseId}`).subscribe({
      next: () => {
        this.dropEnterprise(rawgEnterpriseId);
        this._saving.set(false);
      },
      error: (err) => {
        if (err instanceof HttpErrorResponse && err.status === 404) {
          this.dropEnterprise(rawgEnterpriseId);
        } else {
          this._error.set(errorMessage(err, 'No se pudo quitar la empresa de favoritos.'));
        }
        this._saving.set(false);
      },
    });
  }

  // ---------- helpers privados ----------

  private markGame(itadGameId: string) {
    this._gameIds.update((ids) => new Set(ids).add(itadGameId));
  }

  private unmarkGame(itadGameId: string) {
    this._gameIds.update((ids) => {
      const next = new Set(ids);
      next.delete(itadGameId);
      return next;
    });
    this._games.update((list) => list.filter((g) => g.itadGameId !== itadGameId));
  }

  private dropEnterprise(rawgEnterpriseId: number) {
    this._enterprises.update((list) => list.filter((e) => e.rawgEnterpriseId !== rawgEnterpriseId));
  }

  private reset() {
    this._gameIds.set(new Set());
    this._games.set([]);
    this._enterprises.set([]);
    this._error.set(null);
  }
}
