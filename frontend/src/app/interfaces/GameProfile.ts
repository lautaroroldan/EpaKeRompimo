export interface GameDetail {
    externalId: number,
    slug: string,
    title: string,
    description: string,
    released: string,
    coverImageUrl: string,
    platforms: string[],
    screenshots: string[],
    trailerUrl: string,
    tags: string[]
}
