export interface Game {
    id: number,
    title: string,
    description: string,
    type: string,
    imageUrl: string | null,
    releaseDate: string,
    tags: string[]
}

export interface GameWithPrices {
    game: Game,
    prices: GamePrice[]
}

export interface GamePrice {
    salePrice: number,
    normalPrice: number,
    store: any,
    providerName: string
}