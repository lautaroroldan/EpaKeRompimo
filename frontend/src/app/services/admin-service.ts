import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../config/api';
import { Game } from '../interfaces/Game';
import { GameDetail } from '../interfaces/GameProfile';
import {
  MatchOverride,
  MatchReport,
  MatchReportResolveRequest,
  MatchReportStatus,
  MatchReportType,
} from '../interfaces/MatchReport';
import { PriceOffer } from '../interfaces/PriceOffer';
import { errorMessage } from '../utils/http-error';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly api = `${API_URL}/admin`;

  private readonly _reports = signal<MatchReport[]>([]);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);
  private readonly _selected = signal<MatchReport | null>(null);

  private readonly _igdbCandidates = signal<GameDetail[]>([]);
  private readonly _itadCandidates = signal<Game[]>([]);
  private readonly _itadPrices = signal<PriceOffer[]>([]);
  private readonly _searching = signal(false);
  private readonly _searchError = signal<string | null>(null);

  private readonly _override = signal<MatchOverride | null>(null);
  private readonly _loadingOverride = signal(false);

  readonly reports = this._reports.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly selected = this._selected.asReadonly();

  readonly igdbCandidates = this._igdbCandidates.asReadonly();
  readonly itadCandidates = this._itadCandidates.asReadonly();
  readonly itadPrices = this._itadPrices.asReadonly();
  readonly searching = this._searching.asReadonly();
  readonly searchError = this._searchError.asReadonly();

  readonly override = this._override.asReadonly();
  readonly loadingOverride = this._loadingOverride.asReadonly();

  loadReports(status?: MatchReportStatus, type?: MatchReportType) {
    this._loading.set(true);
    this._error.set(null);

    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (type) params = params.set('type', type);

    this.http.get<MatchReport[]>(`${this.api}/reports`, { params }).subscribe({
      next: (reports) => {
        this._reports.set(reports);
        // Si el reporte abierto ya no está en la lista filtrada, se cierra el detalle
        const current = this._selected();
        if (current && !reports.some((report) => report.id === current.id)) {
          this.select(null);
        }
        this._loading.set(false);
      },
      error: (err) => {
        this._error.set(errorMessage(err, 'No se pudieron cargar los reportes.'));
        this._loading.set(false);
      },
    });
  }

  select(report: MatchReport | null) {
    this._selected.set(report);
    this.resetSearch();
    this._override.set(null);
    if (report) {
      this.loadOverride(report.gameTitle);
    }
  }

  loadOverride(title: string) {
    this._loadingOverride.set(true);
    this.http
      .get<MatchOverride>(`${this.api}/overrides`, { params: { title } })
      .subscribe({
        next: (override) => {
          this._override.set(override);
          this._loadingOverride.set(false);
        },
        error: () => {
          this._override.set(null);
          this._loadingOverride.set(false);
        },
      });
  }

  searchIgdb(title: string) {
    this.search(title, 'igdb');
  }

  searchItad(title: string) {
    this.search(title, 'itad');
  }

  private search(title: string, provider: 'igdb' | 'itad') {
    if (!title.trim()) return;
    this.resetSearch();
    this._searching.set(true);
    this._searchError.set(null);

    if (provider === 'igdb') {
      this.http
        .get<GameDetail[]>(`${this.api}/candidates/igdb`, { params: { title } })
        .subscribe({
          next: (games) => {
            this._igdbCandidates.set(games);
            this._searching.set(false);
          },
          error: (err) => {
            this._searchError.set(errorMessage(err, 'No se pudo buscar en IGDB.'));
            this._searching.set(false);
          },
        });
      return;
    }

    this.http
      .get<Game[]>(`${this.api}/candidates/itad`, { params: { title } })
      .subscribe({
        next: (games) => {
          this._itadCandidates.set(games);
          this._searching.set(false);
        },
        error: (err) => {
          this._searchError.set(errorMessage(err, 'No se pudo buscar en ITAD.'));
          this._searching.set(false);
        },
      });
  }

  // Ofertas reales del candidato de ITAD, para ver a dónde apunta cada link.
  // Sin país: el backend usa el mismo valor por defecto que la ficha del juego.
  loadItadPrices(itadGameId: string) {
    this._itadPrices.set([]);
    this.http
      .get<PriceOffer[]>(`${this.api}/candidates/itad/${encodeURIComponent(itadGameId)}/prices`)
      .subscribe({
        next: (prices) => this._itadPrices.set(prices),
        error: () => this._itadPrices.set([]),
      });
  }

  resolve(id: number, request: MatchReportResolveRequest): Observable<MatchReport> {
    return this.http.patch<MatchReport>(`${this.api}/reports/${id}`, request);
  }

  clearOverride(title: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/overrides`, { params: { title } });
  }

  resetSearch() {
    this._igdbCandidates.set([]);
    this._itadCandidates.set([]);
    this._itadPrices.set([]);
    this._searchError.set(null);
    this._searching.set(false);
  }
}
