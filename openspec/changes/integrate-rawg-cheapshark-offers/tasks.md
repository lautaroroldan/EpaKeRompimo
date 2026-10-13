# Tasks: Integrate RAWG and CheapShark Offers

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 850–1,100 authored lines |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 contracts/adapters/config → PR 2 resolver/aggregation → PR 3 HTTP/docs/verification |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Focused test command | Runtime harness | Rollback boundary |
|------|------|-----------|----------------------|-----------------|-------------------|
| 1 | Validated provider ports, DTOs, and store configuration | PR 1 | `.\mvnw.cmd -Dtest=*ClientTest,*PropertiesTest test` | Mocked adapter/startup tests; no provider credentials in CI | Client, DTO, config, and related tests |
| 2 | Deterministic resolver and aggregate service | PR 2 | `.\mvnw.cmd -Dtest=*ResolverTest,*AggregationServiceTest test` | MockMvc aggregate scenarios with fake gateways | Matching, service, aggregate DTOs, and tests |
| 3 | Public endpoints, compatibility, docs, verification | PR 3 | `.\mvnw.cmd test` | MockMvc `/search` and `/offers`; live providers are not required | Controller annotations, README, and endpoint tests |

## Phase 1: RED Contracts and Threat Tests

- [ ] 1.1 Write failing MVC tests in `src/test/java/com/lautaroroldan/epakerompimo/controller/GameControllerMvcTest.java` for `/search` precedence, positive IDs, blank/overlong titles, no-call validation, sanitized 502s, and unchanged legacy routes.
- [x] 1.2 Write failing adapter/config tests in `src/test/java/com/lautaroroldan/epakerompimo/client/{rawg,cheapshark}` and `src/test/java/com/lautaroroldan/epakerompimo/config/{CheapSharkOffersPropertiesTest,StoreCatalogStartupTest}.java` for invalid payloads, transport/429/timeout errors, and secret-safe logs.
- [ ] 1.3 Write failing resolver tests in `src/test/java/com/lautaroroldan/epakerompimo/matching/GameOfferResolverTest.java`: a trusted-identifier lookup miss suppresses title fallback, while absence of a trusted identifier permits controlled fallback; cover normalization, scores, metadata, editions, duplicates, ties, and statuses.
- [ ] 1.4 Write failing `GameSearchServiceTest.java` and `GameOffersAggregationServiceTest.java` in `src/test/java/com/lautaroroldan/epakerompimo/service` for RAWG mapping, non-Steam `gameById(gameID)` lookup, identifier deals, post-retrieval filtering, empty-match success, and 502 behavior.

## Phase 2: Providers, Configuration, and Matching

- [x] 2.1 Create validated backend DTOs under `src/main/java/com/lautaroroldan/epakerompimo/dto/game` and gateway seams under `src/main/java/com/lautaroroldan/epakerompimo/client/{rawg,cheapshark}`; preserve provider DTO isolation.
- [x] 2.2 Implement `RAWGClient.java`, `CheapSharkGameClient.java`, `CheapSharkDealClient.java`, `CheapSharkStoreClient.java`, `ApiClientConfig.java`, `application.properties`, `CheapSharkDealFilterDTO.java`, `GlobalExceptionHandler.java`, and `ApiClientException.java` with validation and no retries.
- [ ] 2.3 Implement `matching/GameOfferResolver.java` with verified Steam URL parsing, deterministic title/metadata scoring, edition safeguards, duplicate resolution, and fail-closed statuses.

## Phase 3: Services and HTTP Wiring

- [ ] 3.1 Implement `service/GameSearchService.java` and `service/GameOffersAggregationService.java`, including aggregate mapping, configured-store filtering, identifier/title retrieval paths, and explicit 502 semantics.
- [ ] 3.2 Update `controller/GameController.java` with `/api/games/search` and `/{rawgId}/offers`, OpenAPI annotations, validation, and backward-compatible provider routes.

## Phase 4: Documentation and Verification

- [ ] 4.1 Update `README.md` with endpoint schemas, matching limitations, store configuration/startup validation, RAWG attribution/API-key requirements, and compatibility notes.
- [ ] 4.2 Run `.\mvnw.cmd test`, then `.\mvnw.cmd verify`; confirm all spec scenarios, no provider DTO leakage, and review the final diff against the 400-line decision.
