import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DealOffer } from '../../interfaces/Deal';
import { formatPrice } from '../../utils/price';
import { StoreIcon } from '../store-icon/store-icon';

@Component({
  selector: 'app-deal-card',
  imports: [RouterLink, StoreIcon],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block' },
  template: `
    <a
      [routerLink]="['/game', offer().deal.game.title]"
      class="group flex h-full flex-col overflow-hidden rounded-xl border border-border bg-card transition-colors hover:border-soft-periwinkle-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
    >
      <div class="relative aspect-video overflow-hidden bg-muted">
        @if (offer().deal.game.imageUrl) {
          <img
            [src]="offer().deal.game.imageUrl"
            [alt]="offer().deal.game.title"
            loading="lazy"
            class="size-full object-cover transition-transform duration-300 group-hover:scale-[1.03]"
          />
        } @else {
          <div class="flex size-full items-center justify-center text-xs text-muted-foreground">
            Sin imagen
          </div>
        }

        @if (offer().discount > 0) {
          <span
            class="absolute left-2.5 top-2.5 rounded-md bg-primary px-2 py-1 text-xs font-bold text-primary-foreground"
          >
            -{{ offer().discount }}%
          </span>
        }
      </div>

      <div class="flex flex-1 flex-col gap-2 p-3">
        <span class="line-clamp-2 text-sm font-semibold leading-snug group-hover:underline">
          {{ offer().deal.game.title }}
        </span>

        <div class="mt-auto flex flex-col gap-1 lg:flex-row lg:items-end lg:justify-between lg:gap-2">
          <span class="min-w-0">
            @if (offer().price.salePrice === 0) {
              <span class="block text-base font-semibold text-soft-periwinkle-200">Gratis</span>
            } @else {
              <span class="block text-base font-semibold">
                {{ formatPrice(offer().price.salePrice, offer().price.currency) }}
              </span>
              @if (offer().price.normalPrice > offer().price.salePrice) {
                <span class="block text-xs text-muted-foreground line-through">
                  {{ formatPrice(offer().price.normalPrice, offer().price.currency) }}
                </span>
              }
            }
          </span>

          <span class="flex min-w-0 items-center gap-1.5 text-xs text-muted-foreground">
            <app-store-icon [name]="offer().price.store.name" />
            <span class="truncate">{{ offer().price.store.name }}</span>
          </span>
        </div>
      </div>
    </a>
  `,
})
export class DealCard {
  readonly offer = input.required<DealOffer>();

  protected readonly formatPrice = formatPrice;
}
