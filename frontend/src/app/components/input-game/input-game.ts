import { ChangeDetectionStrategy, Component, DestroyRef, ElementRef, inject, signal } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { GameWithPrices } from '../../interfaces/Game';
import { GameService } from '../../services/game-service';
import { catchError, debounceTime, distinctUntilChanged, of, Subject, switchMap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { Router } from '@angular/router';
import { DEFAULT_COUNTRY } from '../../config/api';
import { formatPrice } from '../../utils/price';

@Component({
  selector: 'app-input-game',
  imports: [HlmInputImports],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './input-game.html',
  styleUrl: './input-game.css',
})
export class InputGame {
  protected input = "";
  protected country = DEFAULT_COUNTRY;

  protected readonly open = signal(false);
  protected readonly loading = signal(false);

  protected readonly router = inject(Router);
  private readonly host = inject(ElementRef<HTMLElement>);
  private readonly document = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);
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

    // Cierra el desplegable al hacer clic fuera del buscador.
    const onDocumentClick = (event: MouseEvent) => {
      if (this.open() && !this.host.nativeElement.contains(event.target as Node)) {
        this.open.set(false);
      }
    };
    this.document.addEventListener('click', onDocumentClick);
    this.destroyRef.onDestroy(() => this.document.removeEventListener('click', onDocumentClick));
  }

  games = signal<GameWithPrices[]>([])
  gameService = inject(GameService);

  protected readonly formatPrice = formatPrice;

  protected async onInputChange(event: Event) {
    this.input = (event.target as HTMLInputElement).value;
    this.open.set(this.input.trim().length > 0);
    this.search.next(this.input)
  }

  protected onEscape() {
    this.open.set(false);
  }

  protected select(title: string) {
    this.open.set(false);
    this.router.navigate(['/game', title]);
  }
}
