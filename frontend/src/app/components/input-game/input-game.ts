import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { GameWithPrices } from '../../interfaces/Game';
import { Enterprise } from '../../interfaces/Enterprise';
import { GameService } from '../../services/game-service';
import { EnterpriseService } from '../../services/enterprise-service';
import { catchError, debounceTime, distinctUntilChanged, of, Subject, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { Router } from '@angular/router';
import { FavoriteButton } from '../favorite-button/favorite-button';
import { FavoriteEnterpriseButton } from '../favorite-enterprise-button/favorite-enterprise-button';
import { CompanyLogo } from '../company-logo/company-logo';

@Component({
  selector: 'app-input-game',
  imports: [HlmInputImports, FavoriteButton, FavoriteEnterpriseButton, CompanyLogo],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './input-game.html',
  styleUrl: './input-game.css',
})
export class InputGame {
  protected input = "";
  protected country = "AR";

  protected readonly open = signal(false);
  protected readonly loadingGames = signal(false);
  protected readonly loadingCompanies = signal(false);
  protected readonly loading = computed(() => this.loadingGames() || this.loadingCompanies());

  protected readonly router = inject(Router);
  private search = new Subject<string>()

  games = signal<GameWithPrices[]>([])
  protected readonly companies = signal<Enterprise[]>([]);
  gameService = inject(GameService);
  private readonly enterpriseService = inject(EnterpriseService);

  constructor() {
    // Mismo texto (con debounce) para las dos búsquedas. Van por separado para que,
    // si una tarda, la otra se muestre igual.
    const term$ = this.search.pipe(
      debounceTime(300),
      distinctUntilChanged(),
    );

    // Juegos
    term$
      .pipe(
        switchMap(term => {
          const title = term.trim();
          if (!title) {
            this.loadingGames.set(false);
            return of([] as GameWithPrices[]);   // vacío: no llama al backend
          }
          this.loadingGames.set(true);
          return this.gameService.searchGame(title, this.country).pipe(
            catchError(() => of([] as GameWithPrices[]))   // si falla, sigue funcionando
          );
        }),
        takeUntilDestroyed(),
      ).subscribe(result => {
        this.loadingGames.set(false);
        this.games.set(result);
      })

    // Empresas (el backend ya pide mínimo 2 letras; acá se evita la llamada de más)
    term$
      .pipe(
        switchMap(term => {
          const name = term.trim();
          if (name.length < 2) {
            this.loadingCompanies.set(false);
            return of([] as Enterprise[]);
          }
          this.loadingCompanies.set(true);
          return this.enterpriseService.search(name).pipe(
            catchError(() => of([] as Enterprise[]))   // si falla, sigue funcionando
          );
        }),
        takeUntilDestroyed(),
      ).subscribe(result => {
        this.loadingCompanies.set(false);
        this.companies.set(result);
      })
  }

  protected async onInputChange(event: Event) {
    this.input = (event.target as HTMLInputElement).value;
    this.open.set(this.input.trim().length > 0);
    this.search.next(this.input)
  }

  protected onEscape() {
    this.open.set(false);
  }
}