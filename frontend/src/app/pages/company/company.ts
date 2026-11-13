import { HttpClient } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { catchError, forkJoin, of, switchMap, tap } from 'rxjs';
import { API_URL } from '../../config/api';
import { CompanyDetail, CompanyGame, CompanyGames } from '../../interfaces/Company';
import { GameCard } from '../../components/game-card/game-card';
import { FavoriteEnterpriseButton } from '../../components/favorite-enterprise-button/favorite-enterprise-button';

@Component({
  selector: 'app-company-profile',
  imports: [GameCard, FavoriteEnterpriseButton],
  templateUrl: './company.html',
  styleUrl: './company.css',
})
export class CompanyProfile {
  readonly id = input.required<string>();

  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);

  readonly company = signal<CompanyDetail | null>(null);
  readonly games = signal<CompanyGame[]>([]);
  readonly total = signal(0);
  readonly loading = signal(false);
  readonly loadingMore = signal(false);
  readonly error = signal<string | null>(null);
  readonly logoFailed = signal(false);

  private static readonly pageSize = 24;

  readonly initials = computed(() => {
    const words = (this.company()?.name ?? '').split(/\s+/).filter(Boolean);
    return words.slice(0, 2).map((word) => word.charAt(0)).join('').toUpperCase();
  });

  readonly hasMore = computed(() => this.games().length < this.total());

  constructor() {
    toObservable(this.id)
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.error.set(null);
          this.logoFailed.set(false);
          this.games.set([]);
          this.total.set(0);
        }),
        switchMap((id) =>
          forkJoin({
            company: this.http.get<CompanyDetail>(`${API_URL}/companies/${encodeURIComponent(id)}`),
            games: this.http.get<CompanyGames>(`${API_URL}/companies/${encodeURIComponent(id)}/games`, {
              params: { limit: CompanyProfile.pageSize },
            }),
          }).pipe(
            catchError(() => {
              this.error.set('No pudimos cargar esta empresa.');
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((result) => {
        this.loading.set(false);
        if (!result) {
          return;
        }
        this.company.set(result.company);
        this.games.set(result.games.games);
        this.total.set(result.games.total);
      });
  }

  protected loadMore(): void {
    const company = this.company();
    if (!company || this.loadingMore() || !this.hasMore()) {
      return;
    }

    this.loadingMore.set(true);
    this.http
      .get<CompanyGames>(`${API_URL}/companies/${company.id}/games`, {
        params: { offset: this.games().length, limit: CompanyProfile.pageSize },
      })
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => {
        this.loadingMore.set(false);
        if (!page) {
          return;
        }
        this.games.update((games) => [...games, ...page.games]);
        this.total.set(page.total);
      });
  }
}
