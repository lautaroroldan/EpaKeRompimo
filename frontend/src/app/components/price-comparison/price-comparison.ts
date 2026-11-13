import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { toObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of, switchMap } from 'rxjs';
import { API_URL } from '../../config/api';
import { PriceOffer } from '../../interfaces/PriceOffer';
import { discountPercent as computeDiscount, formatPrice } from '../../utils/price';
import { StoreIcon } from '../store-icon/store-icon';

@Component({
  selector: 'app-price-comparison',
  imports: [StoreIcon],
  templateUrl: './price-comparison.html',
  host: { class: 'block mt-6' },
})
export class PriceComparison {
  readonly title = input.required<string>();

  private readonly http = inject(HttpClient);

  // The backend sorts cheapest first, so the first row is the best offer.
  readonly offers = signal<PriceOffer[]>([]);
  readonly currency = computed(() => this.offers()[0]?.currency ?? null);

  protected readonly formatPrice = formatPrice;

  constructor() {
    toObservable(this.title)
      .pipe(
        switchMap((title) =>
          this.http
            .get<PriceOffer[]>(`${API_URL}/prices`, { params: { title } })
            .pipe(catchError(() => of([]))),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((offers) => this.offers.set(offers));
  }

  // Uses the shop's own discount and falls back to the prices when it reports none.
  protected discountPercent(offer: PriceOffer): number {
    return computeDiscount(offer.salePrice, offer.normalPrice, offer.discountPercent);
  }

  // The cheapest row is the main action, the rest stay secondary.
  protected ctaClasses(best: boolean): string {
    const base =
      'inline-flex h-9 items-center justify-center gap-2 rounded-lg px-3 text-sm font-medium transition-colors';
    return best
      ? `${base} bg-primary text-primary-foreground hover:bg-primary/90`
      : `${base} border border-input hover:bg-accent`;
  }
}
