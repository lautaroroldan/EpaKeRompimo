export interface DealGame {
  id: string;
  title: string;
  description?: string;
  type?: string;
  imageUrl: string | null;
  releaseDate?: string;
  tags?: string[];
}

export interface DealStore {
  name: string;
  urlStore: string;
  urlLogo: string;
}

export interface DealPrice {
  salePrice: number;
  normalPrice: number;
  store: DealStore;
  providerName: string;
  discountPercent: number | null;
  currency: string | null;
}

/** Payload of GET /api/deals: one game and the best offer the provider found for it. */
export interface GameDeal {
  game: DealGame;
  prices: DealPrice[];
}

/** A deal flattened to the single best offer, ready to render. */
export interface DealOffer {
  deal: GameDeal;
  price: DealPrice;
  discount: number;
}
