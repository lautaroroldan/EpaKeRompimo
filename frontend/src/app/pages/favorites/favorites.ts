import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { GamePrice } from '../../interfaces/Game';
import { FavoriteService } from '../../services/favorite-service';
import { CompanyLogo } from '../../components/company-logo/company-logo';
import { formatPrice } from '../../utils/price';

@Component({
  selector: 'app-favorites',
  imports: [RouterLink, CompanyLogo],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './favorites.html',
})
export class Favorites {
  protected readonly favorites = inject(FavoriteService); // el template lee games(), enterprises(), loading(), error()...

  protected readonly noImage =
    'https://static.vecteezy.com/system/resources/thumbnails/022/059/000/small_2x/no-image-available-icon-vector.jpg';

  constructor() {
    this.favorites.clearError();
    this.favorites.loadGames();
    this.favorites.loadEnterprises();
  }

  // De todas las tiendas, la de menor precio (ignora las que no tienen precio)
  protected cheapest(prices: GamePrice[]): (GamePrice & { salePrice: number }) | null {
    const withPrice = prices.filter(
      (price): price is GamePrice & { salePrice: number } => price.salePrice != null,
    );
    if (withPrice.length === 0) return null;
    return withPrice.reduce((min, p) => (p.salePrice < min.salePrice ? p : min));
  }

  protected readonly formatPrice = formatPrice;
}
