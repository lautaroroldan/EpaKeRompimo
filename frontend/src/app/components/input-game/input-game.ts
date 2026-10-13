import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { GameWithPrices } from '../../interfaces/Game';
import { GameService } from '../../services/game-service';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HlmInputImports } from '@spartan-ng/helm/input';

@Component({
  selector: 'app-input-game',
  imports: [HlmInputImports,],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './input-game.html',
  styleUrl: './input-game.css',
})
export class InputGame {
  protected input = "";
  protected country = "AR";

  private search = new Subject<string>()

  constructor() {
    this.search
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(term => this.gameService.searchGame(term, this.country)),
        takeUntilDestroyed(),
      ).subscribe(result => this.games.set(result))
  }

  games = signal<GameWithPrices[]>([])
  gameService = inject(GameService);

  protected async onInputChange(event: Event) {
    this.input = (event.target as HTMLInputElement).value;
    this.search.next(this.input)
  }
}
