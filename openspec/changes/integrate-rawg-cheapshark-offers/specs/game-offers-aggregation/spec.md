# Game Offers Aggregation Specification

## Purpose

Expose stable RAWG details plus filtered CheapShark offers with deterministic, application-owned, fail-closed matching.

## Requirements

### Requirement: Stable aggregate contract

The system MUST expose `GET /api/games/{rawgId}/offers`. HTTP 200 MUST contain `{ "game": { "rawgId": number, "slug": string, "title": string, "description": string, "released": string|null, "coverImageUrl": string|null, "platforms": [string] }, "offers": [...], "match": { "status": "IDENTIFIER_MATCH"|"TITLE_METADATA_MATCH"|"NO_MATCH"|"AMBIGUOUS", "method": "RAWG_STEAM_APP_ID"|"CONTROLLED_TITLE_METADATA"|"NONE" } }`. Each offer MUST contain `dealId`, `storeId`, `storeName`, `priceUsd`, `retailPriceUsd`, `savingsPercent`, and `url`. Optional values MUST be null or empty arrays, never omitted. Provider DTOs MUST NOT be exposed.

#### Scenario: Aggregate a uniquely matched game

- GIVEN RAWG details and a uniquely matched CheapShark game
- WHEN the endpoint is called
- THEN it returns HTTP 200 with the RAWG game, filtered offers, and the applicable match status/method.

#### Scenario: Preserve existing details behavior

- GIVEN a client calls `GET /api/games/{id}`
- WHEN aggregation is available
- THEN that route remains RAWG-only with its existing contract; only `/offers` returns the aggregate.

### Requirement: Identifier-first, application-owned matching

Matching MUST first use a Steam App ID only when a RAWG store link is verified as Steam and its URL supplies that ID, matching CheapShark `steamAppID` exactly. RAWG IDs, `game_id`, slugs, CheapShark `gameID`, and numeric equality MUST NOT be assumed interchangeable. This is an application rule, not a provider-defined relationship.

#### Scenario: Verified identifier wins

- GIVEN a verified RAWG Steam link and an exact CheapShark `steamAppID` match
- WHEN offers are resolved
- THEN the identifier match is selected without title fallback
- AND its method is `RAWG_STEAM_APP_ID`.

### Requirement: Controlled fallback and fail-closed resolution

Without a trusted identifier, the resolver MUST use bounded CheapShark title candidates, controlled normalization of RAWG title/approved alternatives against `external`/`internalName`, and compatible release/platform metadata when available. DLC, expansion, bundle, remaster, edition, or incompatible candidates MUST be excluded. A documented deterministic score threshold MUST reject ties and zero qualifying candidates.

#### Scenario: No match or ambiguity returns an explicit empty result

- GIVEN no candidate qualifies, or multiple candidates tie above the threshold
- WHEN the endpoint is called
- THEN it returns HTTP 200 with the RAWG game and `offers: []`
- AND status is `NO_MATCH` or `AMBIGUOUS` with method `NONE`.

### Requirement: Typed store configuration and filtering

Enabled store IDs MUST come from typed `cheapshark.offers.enabled-store-ids` in `application.properties` (for example `1,7,11`). The list MUST be non-empty, positive, and duplicate-free; malformed configuration MUST fail startup. Filtering MUST occur after retrieval, and unconfigured stores MUST NOT appear.

#### Scenario: Disabled stores are removed

- GIVEN CheapShark returns deals for enabled and disabled stores
- WHEN the aggregate is built
- THEN only configured store IDs appear
- AND an empty filtered result remains a successful matched response.

### Requirement: Provider failure and security semantics

RAWG detail/store failures and CheapShark transport, timeout, rate-limit, or invalid-response failures MUST return HTTP 502 with the existing upstream-error envelope. Credentials MUST remain server-side and MUST NOT appear in responses or logs.

#### Scenario: CheapShark is unavailable

- GIVEN RAWG data is available but CheapShark transport or payload validation fails
- WHEN `/offers` is called
- THEN it returns HTTP 502, not a guessed or silently empty success response.
