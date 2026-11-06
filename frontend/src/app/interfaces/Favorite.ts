import { Game, GamePrice } from './Game';

// game y prices vienen del backend al listar (se consultan a ITAD en el momento)
export interface FavoriteGame {
  id: number;
  itadGameId: string;
  rawgGameId: number | null;
  game: Game | null;
  prices: GamePrice[];
}

export interface FavoriteEnterprise {
  id: number;
  rawgEnterpriseId: number;
}
