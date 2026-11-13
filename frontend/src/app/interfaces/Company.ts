export interface Company {
    id: number,
    name: string,
    developer: boolean,
    publisher: boolean
}

export interface CompanyDetail {
    id: number,
    name: string,
    slug: string | null,
    description: string | null,
    foundedYear: number | null,
    logoUrl: string | null,
    websiteUrl: string | null
}

export interface CompanyGame {
    id: number,
    title: string,
    coverImageUrl: string | null,
    released: string | null
}

export interface CompanyGames {
    total: number,
    games: CompanyGame[]
}
