import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Enterprise } from '../../interfaces/Enterprise';
import { GamePrice } from '../../interfaces/Game';
import { EnterpriseService } from '../../services/enterprise-service';
import { FavoriteService } from '../../services/favorite-service';

@Component({
  selector: 'app-favorites',
  imports: [DecimalPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './favorites.html',
})
export class Favorites {
  protected readonly favorites = inject(FavoriteService); // el template lee games(), enterprises()...
  protected readonly enterprises = inject(EnterpriseService); // el template lee enterprisesOf(), gameError()...

  protected readonly noImage =
    'https://static.vecteezy.com/system/resources/thumbnails/022/059/000/small_2x/no-image-available-icon-vector.jpg';

  // Juego favorito que tiene abierto el panel de empresas (uno a la vez)
  protected readonly expandedGame = signal<string | null>(null);

  constructor() {
    this.favorites.clearError();
    this.favorites.loadGames();
    this.favorites.loadEnterprises();
  }

  // De todas las tiendas, la de menor precio
  protected cheapest(prices: GamePrice[]): GamePrice | null {
    if (prices.length === 0) return null;
    return prices.reduce((min, p) => (p.salePrice < min.salePrice ? p : min));
  }

  // Abre o cierra el panel con las empresas del juego (las pide a ITAD al abrir)
  protected toggleCompanies(itadGameId: string) {
    if (this.expandedGame() === itadGameId) {
      this.expandedGame.set(null);
      return;
    }
    this.expandedGame.set(itadGameId);
    this.enterprises.loadForGame(itadGameId);
  }

  protected toggleEnterprise(enterprise: Enterprise) {
    this.favorites.toggleEnterprise(enterprise.itadEnterpriseId, enterprise.name);
  }
}
