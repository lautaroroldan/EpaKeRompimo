import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { GameWithPrices } from '../../interfaces/Game';
import { GameService } from '../../services/game-service';
import { catchError, debounceTime, distinctUntilChanged, of, Subject, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { Router } from '@angular/router';

@Component({
  selector: 'app-input-game',
  imports: [HlmInputImports],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './input-game.html',
  styleUrl: './input-game.css',
})
export class InputGame {
  protected input = "";
  protected country = "AR";

  protected readonly open = signal(false);
  protected readonly loading = signal(false);

  protected readonly router = inject(Router);
  private search = new Subject<string>()

  constructor() {
    this.search
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(term => {
          const title = term.trim();
          if (!title) {
            this.loading.set(false);
            return of([] as GameWithPrices[]);   // vacío: no llama al backend
          }
          this.loading.set(true);
          return this.gameService.searchGame(title, this.country).pipe(
            catchError(() => of([] as GameWithPrices[]))   // si falla, sigue funcionando
          );
        }),
        takeUntilDestroyed(),
      ).subscribe(result => {
        this.loading.set(false);
        this.games.set(result);
      })
  }

  games = signal<GameWithPrices[]>([])
  gameService = inject(GameService);

  protected async onInputChange(event: Event) {
    this.input = (event.target as HTMLInputElement).value;
    this.open.set(this.input.trim().length > 0);
    this.search.next(this.input)
  }

  protected onEscape() {
    this.open.set(false);
  }
}
