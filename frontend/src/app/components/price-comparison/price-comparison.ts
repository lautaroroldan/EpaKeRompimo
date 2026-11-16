import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { toObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { catchError, of, switchMap } from 'rxjs';
import { API_URL } from '../../config/api';
import { PriceOffer } from '../../interfaces/PriceOffer';
import { MatchReportType } from '../../interfaces/MatchReport';
import { discountPercent as computeDiscount, formatPrice } from '../../utils/price';
import { StoreIcon } from '../store-icon/store-icon';
import { MatchReportDialog } from '../match-report-dialog/match-report-dialog';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-price-comparison',
  imports: [StoreIcon, MatchReportDialog],
  templateUrl: './price-comparison.html',
  host: { class: 'block mt-6' },
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PriceComparison {
  readonly title = input.required<string>();
  // Se reenvían al reporte para que el admin sepa qué matcheo se estaba usando
  readonly itadGameId = input<string | null>(null);
  readonly igdbGameId = input<number | null>(null);

  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  // The backend sorts cheapest first, so the first row is the best offer.
  readonly offers = signal<PriceOffer[]>([]);
  readonly currency = computed(() => this.offers()[0]?.currency ?? null);

  // Oferta cuyo link se está reportando (null = diálogo cerrado)
  protected readonly reportOffer = signal<PriceOffer | null>(null);
  protected readonly storeLinkType: MatchReportType = 'STORE_LINK_MISMATCH';

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

  // Reportar requiere sesión: sin cuenta se manda al login.
  protected openReport(offer: PriceOffer) {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.reportOffer.set(offer);
  }

  protected closeReport() {
    this.reportOffer.set(null);
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
