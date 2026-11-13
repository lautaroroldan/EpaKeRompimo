export interface PriceOffer {
    salePrice: number,
    normalPrice: number,
    discountPercent: number | null,
    currency: string | null,
    providerName: string,
    store: {
        name: string,
        urlStore: string,
        urlLogo: string
    }
}
