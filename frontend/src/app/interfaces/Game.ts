export interface Game {
    id: string,
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
    salePrice: number | null,
    normalPrice: number | null,
    store: {
        name: string,
        urlStore: string,
        urlLogo: string
    },
    providerName: string,
    discountPercent: number | null,
    currency: string | null
}
