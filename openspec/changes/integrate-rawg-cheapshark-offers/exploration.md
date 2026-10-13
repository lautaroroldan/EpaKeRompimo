## Exploration: integrate-rawg-cheapshark-offers

### Current State

The backend is a Java 21 / Spring Boot 4.1.1 / Maven Spring MVC application using provider-specific `RestClient` adapters. `GameController` calls `RAWGClient` and `CheapSharkGameClient` directly; there is no application service or aggregation layer yet.

- RAWG support already covers game search, details, DLCs, and game-store links. `RAWGGameStoreDTO` exposes RAWG's `game_id`, `store_id`, and URL, but the project has no cross-provider resolver.
- CheapShark support already covers game title search, deals, and stores. `CheapSharkGameDTO` exposes `gameID`, `steamAppID`, `external`, and `internalName`; `CheapSharkDealFilterDTO` already supports `steamAppID` and a comma-separated store filter.
- Existing routes are provider-shaped: `GET /api/games/games` searches RAWG, `GET /api/games/{id}` returns RAWG details, `GET /api/games/` searches CheapShark, and `GET /api/deals/` proxies CheapShark deals. The future frontend should not need to call these provider-specific routes directly.
- `ApiClientConfig` creates two qualified clients. Only `rawg.api.key` and shared HTTP timeouts are configured in `application.properties`; CheapShark store selection is currently hard-coded in `CheapSharkDealFilterDTO`.
- `GlobalExceptionHandler` maps `RestClient` failures and `ApiClientException` to `502`, validation failures to `400`, and unexpected failures to `500`. Provider clients currently let `RestClient` exceptions propagate.
- Testing currently consists of one `@SpringBootTest` context smoke test. `openspec/config.yaml` enables strict TDD and declares `./mvnw.cmd test` as the test command, but no focused client, matching, service, controller, or contract tests exist.
- The working tree already contains user changes for RAWG game-store retrieval (`RAWGClient`, `RAWGGameStoreDTO`, and `StoreController`). These must be preserved and reviewed as existing work, not overwritten by the change.

Relevant external contract evidence:

- RAWG requires an API key on every request and supports precise/exact search options; its details and game-store endpoints are the source of metadata and provider links: <https://rawg.io/apidocs> and <https://api.rawg.io/docs/>.
- CheapShark game search returns candidates with `gameID`, optional `steamAppID`, and title fields. Its game-by-ID response returns a game plus deal records, while the deals endpoint supports `steamAppID` filtering. Live examples were checked against `https://www.cheapshark.com/api/1.0/games` and `/deals`.

### Affected Areas

- `src/main/java/com/lautaroroldan/epakerompimo/controller/GameController.java` — add backend-owned search and aggregate-detail boundaries without making the frontend depend on provider routes.
- `src/main/java/com/lautaroroldan/epakerompimo/client/rawg/RAWGClient.java` — retain/use RAWG detail and store-link calls; ensure the adapter exposes the data needed for matching.
- `src/main/java/com/lautaroroldan/epakerompimo/client/cheapshark/CheapSharkGameClient.java` — support candidate lookup and, if selected, game-by-ID/deal retrieval needed for non-Steam matches.
- `src/main/java/com/lautaroroldan/epakerompimo/client/cheapshark/CheapSharkDealClient.java` — apply configured store IDs consistently to aggregated offers.
- `src/main/java/com/lautaroroldan/epakerompimo/dto/rawg/*` and `src/main/java/com/lautaroroldan/epakerompimo/dto/cheapshark/*` — keep upstream DTOs separate from a new backend aggregate response; do not expose provider DTO coupling as the frontend contract.
- New application/service and matching packages under `src/main/java/com/lautaroroldan/epakerompimo/` — orchestrate RAWG detail, RAWG store identifiers, CheapShark candidates/deals, deterministic matching, filtering, and response mapping.
- `src/main/java/com/lautaroroldan/epakerompimo/config/ApiClientConfig.java` and `src/main/resources/application.properties` — bind a typed CheapShark enabled-store configuration, preferably using canonical CheapShark store IDs such as `1,7,11`.
- `src/main/java/com/lautaroroldan/epakerompimo/exception/*` — preserve the existing error envelope and define whether provider-specific aggregation failures are hard failures or explicit degraded responses.
- `src/test/java/...` — add unit tests for normalization/matching, adapter contract tests, controller boundary tests, configuration binding tests, and aggregate failure scenarios.
- `README.md` and generated OpenAPI metadata — document the new backend-only endpoints, aggregate response, store configuration, matching limitations, and RAWG attribution/API-key requirements.

### Approaches

1. **Controller orchestration** — inject both provider clients into `GameController` and perform matching and response composition there.
   - Pros: small initial diff; follows the current direct-controller style.
   - Cons: mixes HTTP concerns, provider contracts, configuration, matching policy, and failure semantics; difficult to test and likely duplicates logic across search/detail endpoints.
   - Effort: Low initially, High to maintain.

2. **Application aggregation service with a dedicated resolver** — add a service used by thin controllers, keep provider clients as adapters, isolate normalization/candidate scoring in a pure resolver, and map results to backend DTOs.
   - Pros: explicit endpoint boundary; matching rules are independently testable; upstream DTOs remain isolated; configuration and partial/no-match behavior are centralized; future providers can be added without expanding controllers.
   - Cons: more classes and response types; requires an explicit contract for no-match, ambiguity, and upstream failure.
   - Effort: Medium.

### Recommendation

Use the application aggregation service and dedicated resolver. Preserve the existing provider-shaped routes for compatibility, but add backend-owned boundaries for the future frontend:

- `GET /api/games/search?title=...` — bounded RAWG-backed suggestions for typeahead. The backend should validate non-blank input and cap the result size; frontend debounce/rendering remains out of scope.
- `GET /api/games/{rawgId}/offers` — returns one backend aggregate containing RAWG details and filtered CheapShark offers. Keep the existing `GET /api/games/{id}` RAWG-only response unchanged unless a later proposal explicitly accepts a breaking contract.

Matching should be deterministic and fail closed:

1. Prefer a verified provider/platform identifier, especially a Steam App ID obtained from a RAWG Steam store link and matched to CheapShark `steamAppID`.
2. Otherwise query bounded CheapShark candidates by title and compare controlled normalized forms of RAWG `name`, `name_original`, and permitted alternative names against CheapShark `external`/`internalName`, with edition/DLC safeguards and compatible platform/release metadata where available.
3. Accept only a unique candidate above a documented threshold. If no candidate qualifies or the best candidates tie, omit offers rather than guessing. A CheapShark result such as a DLC, expansion, remaster, or similarly titled game must not be silently attached to the base game.

Recommended configuration shape is a typed property such as `cheapshark.offers.enabled-store-ids=1,7,11` in `application.properties`, with startup validation for malformed/unknown IDs. The existing default list in `CheapSharkDealFilterDTO` should be replaced or made subordinate to this single configuration source.

Recommended failure contract:

- RAWG detail failure: return the existing `502` upstream error because the selected game cannot be represented.
- CheapShark no match or ambiguous match: return `200` with the RAWG details and an empty offers collection plus an explicit internal/contract-level match status; do not leak a guessed match.
- CheapShark transport or response failure: choose one documented policy before specification. The safer default for a price page is `502`; a degraded `200` is acceptable only if the aggregate response clearly distinguishes unavailable offers from an empty result.
- Store filtering must happen in the backend after matching/deal retrieval, and disabled stores must never appear in the aggregate response.

### Risks

- RAWG `game_id` is not universally interchangeable with a Steam App ID. The implementation must identify the RAWG store/platform before treating the value as a Steam identifier; arbitrary numeric equality is unsafe.
- CheapShark title search includes editions and DLCs alongside base games. Normalization alone is insufficient; candidate scoring, edition exclusion, and ambiguity rejection need explicit scenarios.
- RAWG game details, RAWG store links, CheapShark candidate search, and CheapShark deals can fail independently. Sequential calls increase latency; parallelization is possible only after the identifier dependency and failure contract are defined.
- CheapShark store IDs are provider data and may change or be inactive. Configuration should use IDs validated against the stores endpoint or a documented allow-list, and an empty configured list needs a defined behavior.
- Existing paths are inconsistent (`/games/games`, `/games/`, and `/stores/{gamePk}`), so endpoint naming and backward compatibility must be settled before implementation.
- `RAWGGameDetailsDTO` and other records use provider-specific field names and broad upstream payloads. Returning them directly would couple the frontend contract to RAWG and make future provider changes expensive.
- RAWG attribution, API-key requirements, rate limits, and CheapShark availability need to be reflected in documentation and operational behavior.
- The current test suite cannot prove matching correctness; strict TDD requires adding focused seams before wiring the aggregate flow.

### Ready for Proposal

Yes, with three decisions made explicit in the proposal/specification: the final aggregate endpoint name/response schema, whether CheapShark transport failure is `502` or an explicitly marked degraded `200`, and the exact RAWG store/platform identifier mapping accepted as a Steam App ID. The recommended service/resolver approach and backend-only boundary are sufficiently concrete for proposal and specification work.
