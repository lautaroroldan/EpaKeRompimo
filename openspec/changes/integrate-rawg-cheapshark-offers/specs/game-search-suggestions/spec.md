# Game Search Suggestions Specification

## Purpose

Provide a bounded backend-owned RAWG search contract without exposing provider DTOs or requiring frontend provider access.

## Requirements

### Requirement: Stable validated suggestion endpoint

The system MUST expose `GET /api/games/search?title={query}`. HTTP 200 MUST return `{ "items": [...] }`; every item MUST contain `rawgId` (number), `slug` (string), `title` (string), and `thumbnailUrl` (string or null). Provider-specific fields MUST NOT be required.

#### Scenario: Return bounded suggestions

- GIVEN a valid, non-blank title query
- WHEN the endpoint is called
- THEN it returns HTTP 200 with at most 10 items in RAWG relevance order
- AND every item uses the stable shape.

#### Scenario: Reject invalid input

- GIVEN a blank, whitespace-only, or longer-than-100-character `title`
- WHEN the endpoint is called
- THEN it returns HTTP 400 using the existing API error envelope
- AND it MUST NOT call RAWG.

### Requirement: Bounded provider search

The backend MUST trim and validate the query before calling RAWG and enforce the 10-item bound server-side. It MUST NOT accept provider pagination or arbitrary provider parameters. RAWG failures, malformed responses, and timeouts MUST map to HTTP 502 using the existing upstream-error envelope.

#### Scenario: Provider failure is isolated

- GIVEN a valid query and RAWG is unavailable or returns an invalid payload
- WHEN the endpoint is called
- THEN it returns HTTP 502
- AND it MUST NOT expose credentials, raw exception details, or provider DTOs.

### Requirement: Backend boundary and compatibility

Only the backend provider adapter MAY access RAWG. Existing provider-shaped routes, including `GET /api/games/games` and `GET /api/games/{id}`, MUST retain their paths and response contracts; this capability MUST NOT replace or remove them.

#### Scenario: Existing route remains unchanged

- GIVEN a client calls an existing provider-shaped game route
- WHEN this capability is deployed
- THEN the route remains available with its prior response contract
- AND the new suggestion contract is used only through `/api/games/search`.
