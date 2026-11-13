import { DealOffer, GameDeal } from '../interfaces/Deal';
import { discountPercent } from './price';

/**
 * Flattens the backend payload to the cheapest offer of the game. ITAD already
 * returns one deal per game, but the aggregation is defensive and lets the
 * template stay dumb.
 */
export function toDealOffer(deal: GameDeal): DealOffer | null {
  const price = deal.prices
    .filter((entry) => entry.salePrice !== null && entry.salePrice !== undefined)
    .sort((a, b) => a.salePrice - b.salePrice)[0];

  if (!price) {
    return null;
  }

  return {
    deal,
    price,
    discount: discountPercent(price.salePrice, price.normalPrice, price.discountPercent),
  };
}
