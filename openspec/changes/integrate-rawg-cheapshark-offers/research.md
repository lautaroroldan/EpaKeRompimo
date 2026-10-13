# Research: RAWG and CheapShark provider contracts

## Outcome

`done` — official provider documentation covers the requested provider contracts and limitations. Cross-provider identity mapping and the aggregate HTTP 502 behavior remain application-owned decisions.

## Sources

- **S1 — RAWG API documentation**: https://rawg.io/apidocs
  - API access, usage, attribution, and service overview.
- **S2 — RAWG OpenAPI reference**: https://api.rawg.io/docs/
  - Game details, search, platforms, stores, and game-store link schemas.
- **S3 — CheapShark API documentation**: https://www.cheapshark.com/api
  - Games, deals, stores, rate limits, caching, and price freshness. The canonical documentation link is https://apidocs.cheapshark.com/.

## Validated claims

1. RAWG game details use `GET /games/{id}`; the path accepts a numeric ID or slug. The response documents `id`, `slug`, `name`, `name_original`, `alternative_names`, `platforms`, and other metadata. (S2)
2. RAWG store links use `GET /games/{game_pk}/stores`; results expose `id`, `game_id`, `store_id`, and `url`. (S2)
3. RAWG search supports `search`, `search_precise`, `search_exact`, `stores`, and `exclude_stores`, with paginated results. (S2)
4. CheapShark game search uses `GET /api/1.0/games` with `title` or `steamAppID`; title search is case-insensitive, supports exact matching, and allows up to 60 results. (S3)
5. CheapShark game-search results expose `gameID`, `steamAppID`, `external`, `internalName`, `cheapest`, `cheapestDealID`, and `thumb`; `steamAppID` may be null. (S3)
6. CheapShark game lookup returns game information and associated deals. Deals expose `storeID`, `dealID`, `price`, `retailPrice`, and `savings`. (S3)
7. CheapShark deal listing supports comma-separated `storeID` filtering, pagination, `steamAppID`, title matching, exact matching, `onSale`, `steamworks`, and `maxAge`. (S3)
8. CheapShark store filtering is ID-based. The store endpoint exposes `storeID`, `storeName`, `isActive`, and image metadata; documented examples identify Steam as `1` and Epic Games Store as `25`. (S3)
9. CheapShark requires no API key but requires a descriptive `User-Agent`; excessive requests can produce HTTP 429 and temporary blocking, with `Retry-After` reporting the remaining block time. (S3)
10. CheapShark prices are generally refreshed about once per hour, may fluctuate, and are expressed in USD. (S3)
11. The official documentation does not define a shared identifier between RAWG `id`/`slug`/`game_id` and CheapShark `gameID`/`steamAppID`, nor a canonical cross-provider title-matching algorithm. (S1, S2, S3)

## Product and architecture decisions

- The aggregate endpoint is `GET /api/games/{rawgId}/offers`.
- CheapShark transport or invalid-response failures map to HTTP 502 for the aggregate request.
- Matching is application-owned: prefer a trusted provider/platform identifier, especially Steam App ID; otherwise use controlled title/metadata matching and reject ambiguity.
- Store filtering is application-owned and configured through `application.properties`.

## Gaps and risks

- Provider documentation does not define the application's 502 mapping, timeout behavior, retry policy, or invalid-payload classification.
- RAWG and CheapShark identifiers must not be assumed interchangeable.
- Title fallback can select DLCs, editions, bundles, sequels, or similarly named games; ambiguity must fail closed.
- CheapShark prices can be stale or fluctuate between updates, and server-side aggregation increases rate-limit exposure.
