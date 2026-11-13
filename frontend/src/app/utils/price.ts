const PRICE_LOCALE = 'es-AR';
const FALLBACK_CURRENCY = 'USD';

/** Formats a store price with the currency the provider quoted it in. */
export function formatPrice(amount: number, currency: string | null | undefined): string {
  try {
    return new Intl.NumberFormat(PRICE_LOCALE, {
      style: 'currency',
      currency: currency || FALLBACK_CURRENCY,
    }).format(amount);
  } catch {
    return `${amount.toFixed(2)} ${currency ?? ''}`.trim();
  }
}

/**
 * Uses the discount reported by the provider and falls back to the prices when
 * it reports none, so every caller shows the same number.
 */
export function discountPercent(
  salePrice: number,
  normalPrice: number,
  reported?: number | null,
): number {
  if (reported) {
    return reported;
  }
  return normalPrice > salePrice ? Math.round((1 - salePrice / normalPrice) * 100) : 0;
}
