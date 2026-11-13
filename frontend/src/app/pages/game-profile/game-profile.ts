import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { toObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of, switchMap, tap } from 'rxjs';
import { GameDetail } from '../../interfaces/GameProfile';
import { API_URL } from '../../config/api';
import { PriceComparison } from '../../components/price-comparison/price-comparison';

@Component({
  selector: 'app-game-profile',
  imports: [PriceComparison],
  templateUrl: './game-profile.html',
  styleUrl: './game-profile.css',
})
export class GameProfile {
  readonly title = input.required<string>();

  private readonly http = inject(HttpClient);

  readonly gameDetail = signal<GameDetail | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly activeScreenshot = signal(0);
  private static readonly screenshots = 3;
  private static readonly tags = 4;

  readonly screenshots = computed(() => this.gameDetail()?.screenshots.slice(0, GameProfile.screenshots) ?? []);
  readonly tags = computed(() => this.gameDetail()?.tags.slice(0, GameProfile.tags) ?? []);

  constructor() {
    toObservable(this.title)
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.error.set(null);
        }),
        switchMap((title) =>
          this.http.get<GameDetail>(`${API_URL}/details?title=${encodeURIComponent(title)}`).pipe(
            catchError(() => {
              this.error.set(`No pudimos cargar "${title}".`);
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((gameDetail) => {
        this.gameDetail.set(gameDetail);
        this.activeScreenshot.set(0);
        this.loading.set(false);
      });
  }
}
