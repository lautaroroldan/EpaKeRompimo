import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { GamePrice } from '../../interfaces/Game';
import { FavoriteService } from '../../services/favorite-service';

@Component({
  selector: 'app-favorites',
  imports: [DecimalPipe, HlmInputImports],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './favorites.html',
})
export class Favorites {
  protected readonly favorites = inject(FavoriteService); // el template lee games(), enterprises()...

  protected readonly noImage =
    'https://static.vecteezy.com/system/resources/thumbnails/022/059/000/small_2x/no-image-available-icon-vector.jpg';

  protected readonly enterpriseError = signal<string | null>(null);

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

  // TEMPORAL: las empresas vienen de RAWG y todavía no se pueden buscar por nombre,
  // así que por ahora se agregan por su ID. Se reemplaza cuando esté la integración con RAWG.
  protected addEnterprise(raw: string) {
    const id = Number(raw);
    if (!Number.isInteger(id) || id <= 0) {
      this.enterpriseError.set('Ingresá un ID numérico válido.');
      return;
    }
    this.enterpriseError.set(null);
    this.favorites.addEnterprise(id);
  }
}
