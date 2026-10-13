# Proposal: Integrate RAWG and CheapShark Offers

## Intent

Provide a backend-owned contract that combines RAWG game metadata with filtered CheapShark offers without exposing provider DTOs or requiring frontend orchestration. Matching must prefer trusted provider/platform identifiers and fail closed when title-based matching is absent or ambiguous.

## Scope

### In Scope
- Add bounded RAWG-backed backend search for autocomplete suggestions at `GET /api/games/search?title=...`.
- Add `GET /api/games/{rawgId}/offers` returning a stable aggregate response with RAWG details and CheapShark offers.
- Add an application aggregation service, pure resolver, provider adapter support, typed store-ID configuration, validation, error mapping, and focused tests.
- Preserve existing provider-shaped routes and responses for compatibility.

### Out of Scope
- Frontend debounce, rendering, or direct provider access.
- Replacing or removing existing provider-shaped endpoints.
- Inventing a universal cross-provider identifier or accepting ambiguous title matches.
- Changes to provider APIs, persistence, authentication, or price freshness guarantees.

## Capabilities

### New Capabilities
- `game-search-suggestions`: Backend-validated, bounded RAWG search results for autocomplete consumers.
- `game-offers-aggregation`: Stable aggregate game-details/offers contract with deterministic matching, store filtering, and failure semantics.

### Modified Capabilities
- None. No existing source-of-truth specs are present; compatibility is an implementation constraint for current routes.

## Approach

Keep `RAWGClient` and CheapShark clients as provider adapters. Add thin controller boundaries backed by an application aggregation service and a pure resolver. Resolve Steam App IDs only from verified RAWG Steam store links; otherwise score controlled normalized title/metadata candidates and reject ties or unsafe editions. Map provider DTOs into backend-owned DTOs. Configure enabled CheapShark store IDs through `application.properties`, validate them at startup, apply filtering after retrieval, and map CheapShark transport/invalid responses to HTTP 502. A no-match or ambiguous match returns the RAWG aggregate with empty offers and explicit match status.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `controller`, new application/matching packages | New/Modified | Backend search and aggregate boundaries, orchestration, resolver |
| `client/rawg`, `client/cheapshark`, provider DTOs | Modified | Required provider calls and isolated upstream contracts |
| `config`, `application.properties`, `exception` | Modified | Store configuration, validation, and failure mapping |
| `src/test`, `README.md`, OpenAPI metadata | New/Modified | Contract, matching, configuration, failure, and usage coverage |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| RAWG and CheapShark identifiers are confused | High | Require verified platform mapping and resolver tests |
| Title search selects DLC/edition or wrong game | High | Candidate scoring, metadata safeguards, ambiguity rejection |
| Provider rate limits or payload failures | Med | Bounded requests, explicit 502 contract, focused failure tests |

## Rollback Plan

Revert the aggregate/search controllers, service/resolver, configuration binding, DTOs, documentation, and tests as one change. Existing provider adapters and routes remain the compatibility fallback; remove the new property only after restoring any prior configuration behavior.

## Dependencies

- RAWG API key and attribution requirements; CheapShark API availability, descriptive User-Agent, and documented store IDs.

## Success Criteria

- [ ] Specs define the aggregate schema, match status, identifier precedence, ambiguity behavior, and HTTP 502 cases.
- [ ] Design and tests prove provider DTO isolation, configured store filtering, compatibility, and deterministic fail-closed matching.
