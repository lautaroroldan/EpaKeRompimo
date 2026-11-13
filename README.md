# EpaKeRompimo

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue)

A REST API backend that aggregates video game data from two external providers:

- **[ITAD](https://isthereanydeal.com/)** — game catalog, store prices and deals.
- **[IGDB](https://api-docs.igdb.com)** — game metadata and details.

The project is designed to be the backend for a frontend (web or mobile): it exposes
clean JSON endpoints, centralizes error handling and includes user accounts with
stateless JWT authentication.

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [API reference](#api-reference)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [Project structure](#project-structure)
- [Testing](#testing)
- [Roadmap](#roadmap)
- [License](#license)
- [Author](#author)

---

## Features

- Search games by title through ITAD and get the price offered by each store.
- List current deals with pagination.
- Retrieve game details and metadata through IGDB.
- User accounts with stateless JWT authentication.
- Centralized, consistent JSON error responses (RFC 7807 `ProblemDetail`).
- Request validation with clear `400 Bad Request` messages.
- Interactive API documentation with Swagger UI.
- External API keys kept out of the repository.

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 (Spring Framework 7) |
| Web | Spring MVC (`spring-boot-starter-webmvc`) |
| HTTP client | `RestClient` |
| Security | Spring Security + JWT (`jjwt`) |
| Persistence | Spring Data JPA (H2 by default, MySQL supported) |
| Validation | Jakarta Bean Validation |
| Boilerplate | Lombok |
| API docs | springdoc-openapi (Swagger UI) |
| Build | Maven (wrapper included) |
| Testing | JUnit 5, Spring Boot Test |

## Architecture

```
                        HTTP / JSON
   Client (frontend) ───────────────────┐
                                        ▼
                               ┌──────────────────┐
                               │   Controllers    │  /api/games · /api/deals · /api/details · /api/users
                               └────────┬─────────┘
                                        │
                               ┌────────▼─────────┐
                               │ GameSearchService│  facade + orchestration
                               └────────┬─────────┘
                    ┌───────────────────┼───────────────────┐
                    ▼                   ▼                   ▼
          GameCatalogService   PriceAggregationService   GameDetailService
                    │                   │                   │
          GameCatalogProvider      PriceProvider        GameInfoProvider
                    │                   │                   │
                 ITAD API            ITAD API            IGDB API
```

The codebase is organized **by layer, with a per-provider subpackage** for the
external integrations (`provider/price`, `provider/gamecatalog`, `provider/info`).
Each provider sits behind an interface (`PriceProvider`, `GameCatalogProvider`,
`GameInfoProvider`), so the service layer stays provider-agnostic and new sources
can be added without touching the controllers.

> Adding a new price source is a matter of creating a `@Component` that implements
> `PriceProvider`: Spring injects every implementation into
> `PriceAggregationService` automatically.

## API reference

Base path: `/api`. All responses are JSON.

### Search games and prices — ITAD

`GET /api/games`

| Parameter | Type | Description |
|---|---|---|
| `country` | string | Country code used to resolve prices (e.g. `AR`, `US`). |
| `title` | string | Game title to search for. |

**Example**

```
GET /api/games?country=AR&title=witcher
```

```json
[
  {
    "game": {
      "id": "018d937f-...",
      "title": "The Witcher 3: Wild Hunt",
      "description": "",
      "type": "game",
      "imageUrl": "https://...",
      "releaseDate": "",
      "tags": [""]
    },
    "prices": [
      {
        "salePrice": 7.99,
        "normalPrice": 39.99,
        "store": {
          "name": "Steam",
          "urlStore": "https://store.steampowered.com/...",
          "urlLogo": ""
        },
        "providerName": "ITAD"
      }
    ]
  }
]
```

### Deals — ITAD

`GET /api/deals`

| Parameter | Type | Description |
|---|---|---|
| `country` | string | Country code used to resolve prices. |
| `offset` | integer | Pagination offset. |
| `limit` | integer | Maximum number of results. |

Returns the same `GameWithPricesDTO` shape as `/api/games`.

### Game details — IGDB

`GET /api/details`

| Parameter | Type | Description |
|---|---|---|
| `title` | string | Game title to look up. |

```json
{
  "externalId": 1029,
  "slug": "grand-theft-auto-v",
  "title": "Grand Theft Auto V",
  "description": "Grand Theft Auto V is an open world action-adventure video game...",
  "released": "2013-09-17",
  "coverImageUrl": "https://images.igdb.com/igdb/image/upload/t_cover_big/....jpg",
  "platforms": ["PC (Microsoft Windows)", "PlayStation 4"],
  "screenshots": ["https://images.igdb.com/igdb/image/upload/t_screenshot_big/....jpg"],
  "trailerUrl": "https://www.youtube.com/embed/....",
  "tags": ["Action", "Adventure", "Open world"]
}
```

> `description` is IGDB's `summary` (falling back to `storyline`), so it is
> noticeably shorter than a full store page description. IGDB returns plain
> text, not HTML.
>
> `tags` merges IGDB's `genres`, `themes` and `keywords` (deduplicated, capped
> at 12). Genres and themes are curated; keywords are user-contributed and can
> be noisy.
>
> Screenshots and videos are only expanded for this endpoint: the detail lookup
> resolves the match first and then fetches the full record by id, because
> expanding media for every search result would multiply the payload.
>
> When several games match the search, an exact title match (ignoring accents,
> case and punctuation) wins; otherwise IGDB's own relevance order is used.

### Users

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/users` | List users. |
| `GET` | `/api/users/{id}` | Get a user by id. |
| `POST` | `/api/users` | Register a user. |
| `PUT` | `/api/users/{id}` | Update a user. |
| `DELETE` | `/api/users/{id}` | Delete a user. |
| `POST` | `/api/users/login` | Authenticate and obtain a JWT. |
| `GET` | `/api/users/exists/{username}` | Check whether a username is taken. |

`POST /api/users`, `POST /api/users/login` and `GET /api/users/exists/{username}`
are public. Every other endpoint requires a `Bearer` token.

### Errors

Errors follow RFC 7807 (`ProblemDetail`):

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "title: must not be blank",
  "instance": "/api/games"
}
```

| Status | Meaning |
|---|---|
| `400` | Validation error or invalid parameter. |
| `401` | Invalid credentials or missing/invalid token. |
| `403` | Operation not allowed for the authenticated user. |
| `404` | Resource not found. |
| `409` | Username already taken or data integrity conflict. |
| `500` | Unexpected server error. |

### API documentation

Once the app is running:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

## Getting started

### Prerequisites

- JDK 21+
- Maven is **not** required — use the included wrapper (`mvnw` / `mvnw.cmd`).
- An ITAD API key: <https://isthereanydeal.com/>
- IGDB credentials: create a Twitch developer app with Client Type
  `Confidential` at <https://dev.twitch.tv/console/apps>, then take its Client ID
  and Client Secret.

### 1. Clone

```bash
git clone https://github.com/lautaroroldan/EpaKeRompimo.git
cd EpaKeRompimo
```

### 2. Configure your environment

```bash
cp .env.example backend/.env
# then edit backend/.env and fill in your values
```

The optional `.env` file is loaded automatically in development
(`spring.config.import=optional:file:.env[.properties]`, resolved from `backend/`) and is git-ignored.
In production or CI, export the same variables as real environment variables.

### 3. Run

On Linux/macOS:

```bash
cd backend
./mvnw spring-boot:run
```

On Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The API will be available at <http://localhost:8080>.

## Configuration

| Variable | Description | Default |
|---|---|---|
| `ITAD_API_KEY` | API key for ITAD. **Required** for price and catalog endpoints. | *(empty)* |
| `ITAD_CLIENT_ID` | ITAD client id used by the integration. | *(empty)* |
| `ITAD_CLIENT_SECRET` | ITAD client secret. | *(empty)* |
| `IGDB_CLIENT_ID` | Twitch application Client ID. **Required** for the details endpoint. | *(empty)* |
| `IGDB_CLIENT_SECRET` | Twitch application Client Secret. **Required** for the details endpoint. | *(empty)* |
| `JWT_SECRET` | Base64 secret (>= 32 bytes) used to sign JWTs. | dev-only default |
| `DB_URL` | JDBC URL. | `jdbc:h2:file:./data/epakerompimo;MODE=MySQL` |
| `DB_USERNAME` | Database user. | `sa` |
| `DB_PASSWORD` | Database password. | *(empty)* |

Relevant `application.properties` keys:

- `spring.http.client.connect-timeout` / `read-timeout` — timeouts for external calls.
- `spring.jpa.hibernate.ddl-auto` — schema generation (`update` in development).

IGDB has no API key: the backend exchanges `IGDB_CLIENT_ID` +
`IGDB_CLIENT_SECRET` for a Twitch OAuth2 token and caches it in memory until it
is close to expiry, so no manual refresh is needed. Keep in mind IGDB allows
**4 requests per second** per application.

## Project structure

```
backend/src/main/java/com/lautaroroldan/epakerompimo
├── EpaKeRompimoApplication.java
├── config/                 # beans, security and CORS wiring
│   ├── ApiClientConfig.java
│   ├── CorsConfig.java
│   └── SecurityConfig.java
├── controller/             # HTTP endpoints
│   ├── DealController.java
│   ├── DetailController.java
│   ├── GameController.java
│   ├── StoreController.java
│   └── UserController.java
├── client/                 # shared RestClient helper
│   └── AbstractApiClient.java
├── provider/               # external API adapters, one package per concern
│   ├── gamecatalog/        # GameCatalogProvider + ITAD DTOs
│   ├── info/               # GameInfoProvider + IGDB adapter/DTOs
│   └── price/              # PriceProvider + ITAD adapter/DTOs
├── service/                # business logic
│   ├── GameCatalogService.java
│   ├── GameDetailService.java
│   ├── GameSearchService.java
│   ├── GameSearchServiceManager.java
│   ├── PriceAggregationService.java
│   ├── UserService.java
│   └── UserServiceManager.java
├── dto/                    # request/response models
├── entity/                 # JPA entities
├── repository/             # Spring Data repositories
├── security/               # JWT filter and helpers
└── exceptions/             # error handling
```

## Testing

```bash
# Linux/macOS
cd backend && ./mvnw test

# Windows
cd backend
.\mvnw.cmd test
```

## Roadmap

- [ ] Add focused unit tests for the service layer.
- [ ] Cache ITAD responses to reduce external calls.
- [ ] Isolate provider failures so one unavailable source does not abort aggregation.
- [ ] Add a console store provider (e.g. PlayStation Store / Xbox) as a new `PriceProvider`.
- [ ] Docker Compose setup for MySQL.
- [ ] Persist favorites / wishlist and search history.
- [ ] Content-based recommendation engine (IGDB genres/tags + user favorites).

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE).

## Author

**Lautaro Roldan**

- GitHub: [@lautaroroldan](https://github.com/lautaroroldan)
- Email: lautaronicolasroldan@gmail.com
