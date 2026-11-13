import { Game, GamePrice } from './Game';

// game y prices vienen del backend al listar (se consultan a ITAD en el momento)
export interface FavoriteGame {
  id: number;
  itadGameId: string;
  game: Game | null;
  prices: GamePrice[];
}

export interface FavoriteEnterprise {
  id: number;
  igdbCompanyId: number;
  name: string;
  logoUrl: string | null; // logo de IGDB; null si IGDB no tiene
}
