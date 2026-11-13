import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  afterNextRender,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { DealCard } from '../../components/deal-card/deal-card';
import { InputGame } from '../../components/input-game/input-game';
import { StoreIcon } from '../../components/store-icon/store-icon';
import { DealOffer, GameDeal } from '../../interfaces/Deal';
import { DealService } from '../../services/deal-service';
import { toDealOffer } from '../../utils/deals';
import { formatPrice } from '../../utils/price';

interface StoreStat {
  name: string;
  count: number;
  bestDiscount: number;
}

@Component({
  selector: 'app-home',
  imports: [DealCard, InputGame, StoreIcon, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Home {
  private readonly dealService = inject(DealService);
  private readonly destroyRef = inject(DestroyRef);

  private static readonly bestDeals = 9;
  private static readonly freeDeals = 6;
  private static readonly stores = 8;

  protected readonly skeletons = Array.from({ length: 8 });

  protected readonly deals = signal<GameDeal[]>([]);
  protected readonly loading = signal(true);
  protected readonly failed = signal(false);

  /** Every deal flattened to its cheapest offer, games only. */
  protected readonly offers = computed<DealOffer[]>(() =>
    this.deals()
      // ITAD mixes DLCs, packages and software bundles into the deals list.
      .filter((deal) => deal.game.type === 'game')
      .map(toDealOffer)
      .filter((offer): offer is DealOffer => offer !== null),
  );

  /** Biggest discounts first. The first one is featured in the hero. */
  protected readonly topOffers = computed(() =>
    this.offers()
      .filter((offer) => offer.discount > 0)
      .sort((a, b) => b.discount - a.discount)
      .slice(0, Home.bestDeals),
  );

  protected readonly heroOffer = computed(() => this.topOffers()[0] ?? null);
  protected readonly gridOffers = computed(() => this.topOffers().slice(1));

  protected readonly freeOffers = computed(() =>
    this.offers()
      .filter((offer) => offer.price.salePrice === 0)
      .slice(0, Home.freeDeals),
  );

  protected readonly storeStats = computed<StoreStat[]>(() => {
    const stats = new Map<string, StoreStat>();

    for (const offer of this.offers()) {
      const name = offer.price.store.name;
      const current = stats.get(name);
      if (current) {
        current.count += 1;
        current.bestDiscount = Math.max(current.bestDiscount, offer.discount);
      } else {
        stats.set(name, { name, count: 1, bestDiscount: offer.discount });
      }
    }

    return [...stats.values()]
      .sort((a, b) => b.count - a.count || b.bestDiscount - a.bestDiscount)
      .slice(0, Home.stores);
  });

  protected readonly currency = computed(
    () => this.offers()[0]?.price.currency ?? 'USD',
  );

  protected readonly formatPrice = formatPrice;

  constructor() {
    // The home is prerendered at build time. Prices are volatile and the
    // backend is not available during the build, so the data is fetched by the
    // browser after hydration and the HTML ships with skeletons instead.
    afterNextRender(() => this.load());
  }

  protected load(): void {
    this.loading.set(true);
    this.failed.set(false);

    this.dealService
      .findDeals()
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((deals) => {
        this.loading.set(false);
        if (deals === null) {
          this.failed.set(true);
          return;
        }
        this.deals.set(deals);
      });
  }
}
