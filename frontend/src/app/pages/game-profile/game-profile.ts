import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { toObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, map, of, switchMap, tap } from 'rxjs';
import { GameDetail } from '../../interfaces/GameProfile';
import { API_URL } from '../../config/api';
import { PriceComparison } from '../../components/price-comparison/price-comparison';
import { RouterLink } from '@angular/router';
import { GameService } from '../../services/game-service';
import { FavoriteButton } from '../../components/favorite-button/favorite-button';
import { FavoriteEnterpriseButton } from '../../components/favorite-enterprise-button/favorite-enterprise-button';

@Component({
  selector: 'app-game-profile',
  imports: [PriceComparison, RouterLink, FavoriteButton, FavoriteEnterpriseButton],
  templateUrl: './game-profile.html',
  styleUrl: './game-profile.css',
})
export class GameProfile {
  readonly title = input.required<string>();

  private readonly http = inject(HttpClient);
  private readonly gameService = inject(GameService);

  // Id del juego en ITAD (el de los favoritos). null mientras se busca o si ITAD no lo encuentra:
  // en ese caso la estrella no se muestra, pero el resto de la página funciona igual.
  readonly itadGameId = signal<string | null>(null);

  readonly gameDetail = signal<GameDetail | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly activeScreenshot = signal(0);
  private static readonly screenshots = 3;
  private static readonly tags = 4;

  readonly screenshots = computed(() => this.gameDetail()?.screenshots.slice(0, GameProfile.screenshots) ?? []);
  readonly tags = computed(() => this.gameDetail()?.tags.slice(0, GameProfile.tags) ?? []);

  // The backend ranks credits, so the first company is the recognizable brand.
  readonly primaryCompany = computed(() => this.gameDetail()?.companies?.[0] ?? null);

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

    // Va por separado: si falla, solo desaparece la estrella
    toObservable(this.title)
      .pipe(
        tap(() => this.itadGameId.set(null)),
        switchMap((title) =>
          this.gameService.findItadId(title).pipe(
            map((response) => response.itadGameId),
            catchError(() => of(null)),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((itadGameId) => this.itadGameId.set(itadGameId));
  }
}
