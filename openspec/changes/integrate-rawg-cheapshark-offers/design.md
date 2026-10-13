# Design: Integrate RAWG and CheapShark Offers

## Technical Approach

Keep `GameController` thin; the frontend calls only these backend endpoints. Add `service.GameSearchService`, `service.GameOffersAggregationService`, and pure `matching.GameOfferResolver`. Existing clients alone call `RestClient`; adapters validate/map provider DTOs and raise sanitized `ApiClientException`. Strict TDD writes RED tests first.

## Architecture Decisions

| Decision | Choice and rationale |
|---|---|
| Identity | Accept only RAWG `store_id=1`, HTTPS host `store.steampowered.com`, path `/app/{positive decimal}`. Its canonical ID must equal CheapShark `steamAppID`; no RAWG/gameID numeric equality. ID lookup zero => `NO_MATCH`, multiple distinct => `AMBIGUOUS`; neither falls back to title. |
| Title policy | Compare only RAWG `name`, `name_original`, nonblank alternatives with CheapShark `external`/`internalName`. NFKD/lowercase/mark removal/punctuation-to-space/collapse whitespace; exact scores 100/95/90, token-multiset 85/82/80, threshold 85. Edition markers reject unless present in every approved name. |
| Metadata | `ReleasePlatformMetadata` accepts release `YYYY` or ISO date (1970–2100) and canonical `PC, MAC, LINUX, PLAYSTATION, XBOX, NINTENDO, ANDROID, IOS, OTHER`; aliases map deterministically. Same year and intersecting non-empty platform sets are compatible; different years or disjoint sets conflict. Missing is neutral/no bonus; explicit conflict rejects; malformed present metadata is 502. |
| Duplicates | Require nonblank `gameID`, group by trimmed ID. Multiple distinct nonblank Steam IDs mark the group `CONFLICTING_DUPLICATE` and exclude it. Otherwise select one row by descending completeness (valid Steam ID, external, internal, thumbnail), then normalized lexical order ascending; never union titles. |
| Offers/stores | Identifier matches use `dealsBySteamAppId`. A title match uses `gameById(selected.gameID)` and its validated embedded deals. Both filter after retrieval. `cheapshark.offers.enabled-store-ids=1,7,11` binds typed positive unique IDs checked against live `/stores`; unknown/inactive IDs, unavailable endpoint, or malformed catalog fail startup. |
| Failure/security | Transport, timeout, 429, null, malformed, or invalid required field => 502; no retries. Logs may contain provider, operation, status, safe code, and request template only. Never log keys, `.env`, query URLs, headers, bodies, credential-bearing exception text, or credentials. |

## Data Flow

```text
GET /search -> GameSearchService -> RawgGateway.search -> validate/map -> suggestions
GET /{id}/offers -> RAWG details + stores -> resolver -> CheapShark candidate(s)
                               -> steam deals OR game/{gameID} -> filter stores -> DTO
```

```text
Client -> Controller -> OffersService -> RAWG(details, stores)
OffersService -> CheapShark: candidatesBySteamAppId OR candidates(title)
OffersService -> Resolver: RAWG metadata + candidates
Resolver --> OffersService: unique match / NO_MATCH / AMBIGUOUS
OffersService -> CheapShark: deals(appId) OR game(gameID).deals
OffersService --> Controller: filtered backend DTO
```

Identifier misses suppress title fallback. Unique title matches return offers; no match/ambiguity returns 200 with empty offers. Every provider response passes operation-specific validation before use.

## File Changes

| File | Action | Responsibility |
|---|---|---|
| `controller/GameController.java` | Modify | New endpoints; preserve existing routes. |
| `service/*Service.java`, `matching/GameOfferResolver.java` | Create | Orchestration and deterministic resolution. |
| `dto/game/*`, `dto/{rawg,cheapshark}/*` | Create/modify | Backend contract and validated provider models. |
| `client/rawg/*`, `client/cheapshark/*` | Modify | Ports plus search/details/stores, candidates, lookup, deals. |
| `config/*`, `application.properties` | Create/modify | Typed IDs and live catalog startup check. |
| `exception/*`, `README.md`, OpenAPI, `src/test/java/...` | Modify/create | Sanitized errors, docs, strict RED tests. |

## Interfaces / Contracts

```java
interface RawgGateway { RawgPage search(String title); RawgDetails details(int id); List<RawgStoreLink> stores(int id); }
interface CheapSharkGateway { List<Candidate> candidates(String title); List<Candidate> candidatesBySteamAppId(String appId); GameWithDeals gameById(String gameId); List<Deal> dealsBySteamAppId(String appId); List<Store> stores(); }
interface GameOfferResolver { Resolution resolve(RawgDetails rawg, List<RawgStoreLink> stores, List<Candidate> candidates); }
```

Collections are non-null; required IDs, titles, URLs, prices, savings, and store fields are validated. Optional values become null/empty. Match enums remain `IDENTIFIER_MATCH|TITLE_METADATA_MATCH|NO_MATCH|AMBIGUOUS` and methods `RAWG_STEAM_APP_ID|CONTROLLED_TITLE_METADATA|NONE`.

Validation matrix: RAWG search requires page/results and each positive `id` plus nonblank `slug/name`; details requires positive `id`, nonblank `slug/name`, valid optional release/platform entries; stores require results and positive `store_id` plus URL. CheapShark candidates require a list, nonblank `gameID`, a title, and positive optional `steamAppID`; game lookup requires matching game ID and deals; deals require deal ID, positive store ID, URL, and finite nonnegative decimals; stores require positive ID, name, and `isActive` `0|1`. Any violation is 502; semantic duplicate identity conflict is excluded as above.

## Testing Strategy

Strict RED tests use fake ports for metadata/conflicts, duplicates, scores, ties, identifier precedence, `gameById(gameID)` title retrieval, and post-retrieval filtering. Adapter tests cover invalid search, details, stores, candidates, game, and deals payloads; startup tests cover unknown/inactive/unavailable stores; MVC tests cover routing, 400, compatibility, sanitized 502, and no-call validation. Run `./mvnw.cmd test`.

## Threat Matrix

| Boundary | Applicability / safe and failure behavior | Planned RED test |
|---|---|---|
| HTTP routing/query | Applicable: static `/search` precedence, positive IDs, blank/overlong title => 400/no call; provider failure => sanitized 502. | MVC boundary tests |
| Documentation-like paths | N/A: no executable-file classification. | None |
| Git repository selection | N/A: no Git automation. | None |
| Commit state | N/A: no commit automation. | None |
| Push state | N/A: no push automation. | None |
| PR commands | N/A: no PR automation. | None |

## Migration / Rollout

No data migration or flag. Roll back endpoints, services, resolver, DTOs, adapters, configuration, docs, and tests together; remove the new property last. Existing provider routes remain compatible throughout.

## Open Questions

None; all six design blockers are operationally resolved.
