# EpaKeRompimo

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue)

A REST API backend that aggregates video game data from two external providers:

- **[CheapShark](https://www.cheapshark.com/api)** — deals, prices and stores.
- **[RAWG](https://rawg.io/apidocs)** — game metadata and DLC/additional content.

The project is designed to be the backend for a future frontend (web or mobile): it
exposes clean JSON endpoints, centralizes error handling and is ready to grow with
user accounts, favorites, search history and a recommendation engine.

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

- Search deals across multiple stores through CheapShark.
- Rich deal filtering (price range, Metacritic score, sale status, sorting, pagination, ...).
- Search game metadata and DLCs through RAWG.
- List the stores supported by CheapShark.
- Centralized, consistent JSON error responses.
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
| Validation | Jakarta Bean Validation |
| Boilerplate | Lombok |
| API docs | springdoc-openapi (Swagger UI) |
| Build | Maven (wrapper included) |
| Testing | JUnit 5, Spring Boot Test |

## Architecture

```
                          HTTP / JSON
   Client (future frontend) ──────────────┐
                                          ▼
                                 ┌──────────────────┐
                                 │   Controllers    │  /api/deals, /api/games, /api/stores
                                 └────────┬─────────┘
                                          │
                                 ┌────────▼─────────┐
                                 │  Clients (Rest)  │  one adapter per provider
                                 └───┬──────────┬───┘
                                     │          │
                         ┌───────────▼──┐   ┌───▼──────────┐
                         │  CheapShark  │   │     RAWG     │
                         │  public API  │   │   public API │
                         └──────────────┘   └──────────────┘

                 GlobalExceptionHandler maps upstream/validation
                 errors into a single ErrorResponseDTO shape.
```

The codebase is organized **by layer, with a per-provider subpackage** for the
external integrations (`client/cheapshark`, `client/rawg`, `dto/cheapshark`,
`dto/rawg`). This keeps controllers provider-agnostic while the adapters stay
isolated and swappable.

## API reference

Base path: `/api`. All responses are JSON.

### Deals — CheapShark

`GET /api/deals/`

| Parameter | Type | Description |
|---|---|---|
| `title` | string | Filter by game title. |
| `storeIDs` | string | Comma-separated store IDs. Defaults to the main stores. |
| `exact` | integer | `1` to match the title exactly. Defaults to `1`. |
| `pageNumber` | integer | Page number (0-based). |
| `pageSize` | integer | Number of results per page. |
| `sortBy` | enum | `DealRating`, `Title`, `Savings`, `Price`, `Metacritic`, `Reviews`, `ReviewCount`, `Release`, `Store`, `Recent`. |
| `desc` | integer | `1` for descending order. |
| `lowerPrice` | number | Minimum price. |
| `upperPrice` | number | Maximum price. |
| `metacritic` | integer | Minimum Metacritic score. |
| `steamRating` | number | Minimum Steam rating. |
| `minimumReviewCount` | integer | Minimum review count. |
| `maxAge` | integer | Max age of the deal in minutes. |
| `steamAppID` | string | Filter by Steam App ID. |
| `AAA` | string | `1` to only include AAA titles. |
| `steamworks` | string | `1` to only include Steamworks titles. |
| `onSale` | integer | `1` to only include games currently on sale. |

**Example**

```
GET /api/deals/?title=witcher&upperPrice=20&sortBy=DealRating&pageSize=5
```

```json
[
  {
    "title": "The Witcher 3: Wild Hunt",
    "salePrice": "7.99",
    "normalPrice": "39.99",
    "savings": "80.020",
    "storeID": "1"
  }
]
```

### Games — CheapShark

`GET /api/games/?title={title}` — search games on CheapShark.

```json
[
  {
    "gameID": "612",
    "steamAppID": "292030",
    "cheapest": "7.99",
    "cheapestDealID": "abc123",
    "external": "The Witcher 3: Wild Hunt",
    "internalName": "THEWITCHER3WILDHUNT",
    "thumb": "https://..."
  }
]
```

### Games — RAWG (paginated)

`GET /api/games/games?title={title}` — search games on RAWG.

`GET /api/games/dlcs?game_pk={id}` — list DLCs / additional content for a game.

Both return a pagination envelope:

```json
{
  "count": 1234,
  "next": "https://api.rawg.io/api/games?page=2...",
  "previous": null,
  "results": [
    {
      "id": 3498,
      "slug": "grand-theft-auto-v",
      "name": "Grand Theft Auto V",
      "released": "2013-09-17",
      "tba": false,
      "background_image": "https://...",
      "rating": 4.47,
      "metacritic": 92,
      "playtime": 74,
      "updated": "2024-01-01T00:00:00"
    }
  ]
}
```

### Game details — RAWG

`GET /api/games/{id}` — full details for a single game, where `{id}` is the RAWG
game id (the `id` field from a search result).

```json
{
  "id": 3498,
  "slug": "grand-theft-auto-v",
  "name": "Grand Theft Auto V",
  "name_original": "Grand Theft Auto V",
  "description": "<p>...</p>",
  "metacritic": 92,
  "released": "2013-09-17",
  "tba": false,
  "background_image": "https://...",
  "rating": 4.47,
  "playtime": 74,
  "website": "https://www.rockstargames.com/V/",
  "esrb_rating": { "id": 4, "slug": "mature", "name": "Mature" },
  "platforms": [
    {
      "platform": { "id": 4, "slug": "pc", "name": "PC" },
      "released_at": "2015-04-14",
      "requirements": { "minimum": "...", "recommended": "..." }
    }
  ]
}
```

### Stores — CheapShark

`GET /api/stores/`

```json
[
  {
    "storeID": "1",
    "storeName": "Steam",
    "isActive": 1,
    "images": {
      "banner": "https://...",
      "logo": "https://...",
      "icon": "https://..."
    }
  }
]
```

### Errors

Every error uses the same shape:

```json
{
  "timestamp": "2026-09-10T11:50:02.950Z",
  "status": 400,
  "error": "Validation failed",
  "message": "One or more parameters are invalid",
  "path": "/api/games/",
  "details": {
    "title": "must not be blank"
  }
}
```

| Status | Meaning |
|---|---|
| `400` | Validation error or invalid parameter. |
| `502` | The upstream API (CheapShark/RAWG) failed or is unreachable. |
| `500` | Unexpected server error. |

### API documentation

Once the app is running:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

## Getting started

### Prerequisites

- JDK 21+
- Maven is **not** required — use the included wrapper (`mvnw` / `mvnw.cmd`).
- A RAWG API key (free): <https://rawg.io/apidocs>

### 1. Clone

```bash
git clone https://github.com/lautaroroldan/EpaKeRompimo.git
cd EpaKeRompimo
```

### 2. Configure your environment

```bash
cp .env.example .env
# then edit .env and fill in your values
```

The optional `.env` file is loaded automatically in development
(`spring.config.import=optional:file:.env[.properties]`) and is git-ignored.
In production or CI, export the same variables as real environment variables.

### 3. Run

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API will be available at <http://localhost:8080>.

## Configuration

| Variable | Description | Default |
|---|---|---|
| `RAWG_API_KEY` | API key for RAWG. **Required** for RAWG endpoints. | *(empty)* |

Relevant `application.properties` keys:

- `spring.http.client.connect-timeout` / `read-timeout` — timeouts for external calls.

## Project structure

```
com.lautaroroldan.epakerompimo
├── EpaKeRompimoApplication.java
├── config/                 # beans and clients wiring
│   └── ApiClientConfig.java
├── controller/             # HTTP endpoints
│   ├── DealController.java
│   ├── GameController.java
│   └── StoreController.java
├── client/                 # external API adapters (RestClient)
│   ├── cheapshark/
│   │   ├── CheapSharkDealClient.java
│   │   ├── CheapSharkGameClient.java
│   │   └── CheapSharkStoreClient.java
│   └── rawg/
│       └── RAWGClient.java
├── dto/                    # request/response models
│   ├── cheapshark/
│   └── rawg/
├── enums/                  # domain enums (stores, sorting)
├── exception/              # error handling
│   ├── ApiClientException.java
│   ├── ErrorResponseDTO.java
│   └── GlobalExceptionHandler.java
├── model/        (roadmap) # JPA entities
├── repository/   (roadmap) # Spring Data repositories
├── service/      (roadmap) # business logic + recommendations
└── security/     (roadmap) # JWT authentication
```

## Testing

```bash
# Linux/macOS
./mvnw test

# Windows
.\mvnw.cmd test
```

Tests run as a Spring context smoke test and require no database.

## Roadmap

- [ ] Add persistence (Spring Data JPA + MySQL + Flyway).
- [ ] User accounts with credentials (JWT stateless authentication).
- [ ] Favorites / wishlist persistence.
- [ ] Search history.
- [ ] Content-based recommendation engine (RAWG genres/tags + user favorites).
- [ ] Deal caching to reduce calls to external APIs.
- [ ] CORS configuration for the future frontend.
- [ ] Docker Compose setup for MySQL.
- [ ] More tests (client and controller layers).

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE).

## Author

**Lautaro Roldan**

- GitHub: [@lautaroroldan](https://github.com/lautaroroldan)
- Email: lautaronicolasroldan@gmail.com
