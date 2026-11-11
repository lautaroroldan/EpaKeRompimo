import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../environments/environment';
import { Enterprise } from '../interfaces/Enterprise';
import { errorMessage } from '../utils/http-error';

// Empresas de un juego (según ITAD). Las empresas son iguales para todas las personas,
// por eso se guardan en memoria sin importar quién tenga la sesión iniciada.
@Injectable({
  providedIn: 'root',
})
export class EnterpriseService {
  private readonly http = inject(HttpClient);

  // Estado (signals privados: solo el service los modifica)
  private readonly _byGame = signal<Record<string, Enterprise[]>>({}); // por id de juego de ITAD
  private readonly _loadingGameIds = signal<ReadonlySet<string>>(new Set());
  private readonly _gameError = signal<string | null>(null);

  // Lo que ven los componentes (solo lectura)
  readonly gameError = this._gameError.asReadonly();

  // undefined = todavía no se cargaron
  enterprisesOf(itadGameId: string): Enterprise[] | undefined {
    return this._byGame()[itadGameId];
  }

  isLoadingGame(itadGameId: string): boolean {
    return this._loadingGameIds().has(itadGameId);
  }

  // GET /api/favorites/games/{itadGameId}/enterprises
  loadForGame(itadGameId: string) {
    this._gameError.set(null);
    if (this._byGame()[itadGameId] !== undefined || this.isLoadingGame(itadGameId)) return;

    this._loadingGameIds.update((ids) => new Set(ids).add(itadGameId));

    const url = `${environment.api}/favorites/games/${encodeURIComponent(itadGameId)}/enterprises`;
    this.http.get<Enterprise[]>(url).subscribe({
      next: (list) => {
        this._byGame.update((byGame) => ({ ...byGame, [itadGameId]: list }));
        this.finishLoading(itadGameId);
      },
      error: (err) => {
        this._gameError.set(errorMessage(err, 'No se pudieron cargar las empresas del juego.'));
        this.finishLoading(itadGameId);
      },
    });
  }

  private finishLoading(itadGameId: string) {
    this._loadingGameIds.update((ids) => {
      const next = new Set(ids);
      next.delete(itadGameId);
      return next;
    });
  }
}
