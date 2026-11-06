import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { FavoriteService } from '../../services/favorite-service';

// Estrella para guardar o quitar un juego de favoritos.
// Solo se muestra si hay sesión: para buscar juegos no hace falta cuenta.
@Component({
  selector: 'app-favorite-button',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (auth.isLoggedIn()) {
    <button type="button" (click)="toggle($event)" [disabled]="favorites.saving()"
        [attr.aria-pressed]="isFavorite()"
        [attr.aria-label]="isFavorite() ? 'Quitar de favoritos' : 'Agregar a favoritos'"
        [title]="isFavorite() ? 'Quitar de favoritos' : 'Agregar a favoritos'"
        class="inline-flex size-8 shrink-0 items-center justify-center rounded-lg text-xl hover:bg-accent disabled:opacity-50"
        [class.text-yellow-500]="isFavorite()">
        <span aria-hidden="true">{{ isFavorite() ? '★' : '☆' }}</span>
    </button>
    }
  `,
})
export class FavoriteButton {
  // id del juego en ITAD (game.game.id en los resultados del buscador)
  readonly itadGameId = input.required<string>();

  protected readonly auth = inject(AuthService);
  protected readonly favorites = inject(FavoriteService);

  protected readonly isFavorite = computed(() => this.favorites.isFavoriteGame(this.itadGameId()));

  protected toggle(event: Event) {
    event.stopPropagation(); // que el click no llegue a la fila del buscador
    this.favorites.toggleGame(this.itadGameId());
  }
}
