import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';

/**
 * Normalized ITAD shop name -> Simple Icons slug. Simple Icons is used because
 * ITAD does not expose shop logos; shops without an icon in Simple Icons (e.g.
 * Microsoft Store, Fanatical, Gamesplanet) fall back to a generic storefront.
 */
const STORE_SLUGS: Record<string, string> = {
  steam: 'steam',
  gog: 'gogdotcom',
  'gog.com': 'gogdotcom',
  'epic game store': 'epicgames',
  'epic games store': 'epicgames',
  'epic games': 'epicgames',
  epic: 'epicgames',
  'humble store': 'humblebundle',
  'humble bundle': 'humblebundle',
  'ubisoft store': 'ubisoft',
  ubisoft: 'ubisoft',
  'ea app': 'ea',
  'ea store': 'ea',
  ea: 'ea',
  origin: 'origin',
  'electronic arts': 'ea',
  'battle.net': 'battledotnet',
  battlenet: 'battledotnet',
  blizzard: 'battledotnet',
  'itch.io': 'itchdotio',
  itch: 'itchdotio',
  g2a: 'g2a',
};

@Component({
  selector: 'app-store-icon',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'inline-flex size-6 shrink-0 items-center justify-center' },
  template: `
    @if (showIcon()) {
      <img
        [src]="iconUrl()"
        alt=""
        class="size-full object-contain"
        (error)="failedUrl.set(iconUrl())"
      />
    } @else {
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        class="size-full"
        aria-hidden="true"
      >
        <path d="m2 7 4.41-4.41A2 2 0 0 1 7.83 2h8.34a2 2 0 0 1 1.42.59L22 7" />
        <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8" />
        <path d="M15 22v-4a2 2 0 0 0-2-2h-2a2 2 0 0 0-2 2v4" />
        <path d="M2 7h20" />
        <path
          d="M22 7v3a2 2 0 0 1-2 2 2.7 2.7 0 0 1-1.59-.63.7.7 0 0 0-.82 0A2.7 2.7 0 0 1 16 12a2.7 2.7 0 0 1-1.59-.63.7.7 0 0 0-.82 0A2.7 2.7 0 0 1 12 12a2.7 2.7 0 0 1-1.59-.63.7.7 0 0 0-.82 0A2.7 2.7 0 0 1 8 12a2.7 2.7 0 0 1-1.59-.63.7.7 0 0 0-.82 0A2.7 2.7 0 0 1 4 12a2 2 0 0 1-2-2V7"
        />
      </svg>
    }
  `,
})
export class StoreIcon {
  readonly name = input.required<string>();

  // Guardamos la URL que falló (no solo un booleano) para que el icono se
  // reintente cuando la tienda cambia, igual que hace UserAvatar.
  protected readonly failedUrl = signal<string | null>(null);

  protected readonly iconUrl = computed(() => {
    const slug = STORE_SLUGS[StoreIcon.normalize(this.name())];
    return slug ? `https://cdn.simpleicons.org/${slug}/white` : null;
  });

  protected readonly showIcon = computed(() => {
    const url = this.iconUrl();
    return url !== null && url !== this.failedUrl();
  });

  private static normalize(name: string): string {
    return (name ?? '').toLowerCase().replace(/\s+/g, ' ').trim();
  }
}
