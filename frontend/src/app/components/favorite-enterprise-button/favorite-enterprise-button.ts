import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { FavoriteService } from '../../services/favorite-service';

// Estrella para guardar o quitar una empresa de favoritos.
// Solo se muestra si hay sesión: para buscar no hace falta cuenta.
@Component({
  selector: 'app-favorite-enterprise-button',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (auth.isLoggedIn()) {
    <button type="button" (click)="toggle($event)" [disabled]="favorites.saving()"
        [attr.aria-pressed]="isFavorite()"
        [attr.aria-label]="isFavorite() ? 'Quitar empresa de favoritos' : 'Agregar empresa a favoritos'"
        [title]="isFavorite() ? 'Quitar empresa de favoritos' : 'Agregar empresa a favoritos'"
        class="inline-flex size-8 shrink-0 items-center justify-center rounded-lg text-xl hover:bg-accent disabled:cursor-default disabled:opacity-50"
        [class.text-yellow-500]="isFavorite()">
        <span aria-hidden="true">{{ isFavorite() ? '★' : '☆' }}</span>
    </button>
    }
  `,
})
export class FavoriteEnterpriseButton {
  // id de la empresa en IGDB
  readonly igdbCompanyId = input.required<number>();
  readonly name = input.required<string>();

  protected readonly auth = inject(AuthService);
  protected readonly favorites = inject(FavoriteService);

  protected readonly isFavorite = computed(() => this.favorites.isFavoriteEnterprise(this.igdbCompanyId()));

  protected toggle(event: Event) {
    event.stopPropagation(); // que el click no llegue a la fila del buscador
    this.favorites.toggleEnterprise(this.igdbCompanyId(), this.name());
  }
}