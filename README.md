# ANNRAKSH

ANNRAKSH is a Java full stack agricultural planning platform. It helps farmers compare local crop demand and supply, record cultivation plans and harvests, and review transparent, rule-based crop recommendations intended to help reduce overproduction and food waste.

## Technology

- Backend: Java 17, Spring Boot, Spring Web, Spring Data JPA, Hibernate, Spring Security, JWT, Bean Validation, Maven
- Database: PostgreSQL
- Frontend: React, JavaScript, Axios, React Router, Vite

## Prerequisites

- JDK 17
- Apache Maven 3.9 or newer
- PostgreSQL, with a database named `annraksh`
- Node.js `^20.19.0` or `>=22.12.0` and npm (required by the current Vite release)

The backend defaults to PostgreSQL at `jdbc:postgresql://localhost:5432/annraksh` as user `postgres`. Override these defaults with the environment variables below when needed.

| Variable | Required | Default / purpose |
|---|---:|---|
| `DB_URL` | No | `jdbc:postgresql://localhost:5432/annraksh` |
| `DB_USERNAME` | No | `postgres` |
| `DB_PASSWORD` | Yes | Password for the PostgreSQL user |
| `JWT_SECRET` | Yes | Private signing key; at least 32 UTF-8 bytes |
| `FLYWAY_BASELINE_ON_MIGRATE` | Normally no | Set to `true` for the one-time baseline of a pre-existing Hibernate-created schema; see below |
| `API_DOCS_ENABLED` | No | OpenAPI JSON endpoint toggle; enabled by default |
| `SWAGGER_UI_ENABLED` | No | Swagger UI toggle; enabled by default |
| `VITE_API_BASE_URL` | No | Frontend API base URL at build time; local Vite proxy is used when unset |

Never commit real passwords, signing keys, or populated local `.env` files. The checked-in application configuration requires `DB_PASSWORD` and `JWT_SECRET` to be supplied by the environment.

## Run locally (Windows PowerShell)

Create the database once if needed:

```sql
CREATE DATABASE annraksh;
```

In the first terminal, from the repository root, set the backend environment and start Spring Boot:

```powershell
$env:DB_PASSWORD = "your-local-postgres-password"
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
mvn spring-boot:run
```

The API listens on `http://localhost:8081`.

For an empty database, Flyway creates the schema automatically. If you already ran an earlier ANNRAKSH version that let Hibernate create the tables, back up the database and use the one-time baseline setting before starting the application:

```powershell
$env:FLYWAY_BASELINE_ON_MIGRATE = "true"
mvn spring-boot:run
```

Flyway records the existing schema at baseline version `1`, skips the initial table-creation migration, then applies later migrations. Hibernate validates the mappings during startup. After a successful startup, remove the setting from the terminal with `Remove-Item Env:FLYWAY_BASELINE_ON_MIGRATE`; the Flyway history table prevents a repeat baseline. Confirm that the existing schema corresponds to the current entity mappings before baselining. Do not baseline an unknown or mismatched production database.

In a second terminal, install the locked frontend dependencies and start Vite:

```powershell
Set-Location frontend
npm ci
npm run dev
```

Open `http://localhost:5173`. During local development, Vite forwards `/api` requests to `http://localhost:8081`, so no frontend API environment variable or separate CORS configuration is needed.

For a production frontend build, run `npm run build` in `frontend/`. Set `VITE_API_BASE_URL` before the build only when the API is served from a different base URL. A separately hosted frontend and API must be configured to allow the browser origin through CORS, or be placed behind a reverse proxy that serves them under one origin. This repository does not yet include production CORS or reverse-proxy configuration.

## Tests and builds

From the repository root:

```powershell
mvn test
```

From `frontend/`:

```powershell
npm ci
npm run build
```

## Authentication and roles

- `POST /api/auth/register` creates a FARMER account and returns a bearer token.
- `POST /api/auth/login` authenticates an account and returns a bearer token.
- Send the token with protected requests as `Authorization: Bearer <accessToken>`.
- Public registration always assigns the FARMER role. To provision an administrator, first register a trusted account, then promote it out of band, for example:

  ```sql
  UPDATE farmers SET role = 'ADMIN' WHERE email = 'admin@example.com';
  ```

- Passwords are stored as BCrypt hashes. There is no public administrator registration.

## API overview

| Method and path | Access | Purpose |
|---|---|---|
| `POST /api/auth/register` | Public | Register a farmer |
| `POST /api/auth/login` | Public | Sign in |
| `GET /api/crops`, `GET /api/crops/{id}` | Public | Browse the crop library |
| `POST /api/crops`, `PUT /api/crops/{id}`, `DELETE /api/crops/{id}` | ADMIN | Maintain crop records |
| `GET /api/demand?location={location}` | Public | Browse demand snapshots; location filter is optional |
| `POST /api/demand` | ADMIN | Record a demand snapshot |
| `GET /api/cultivation-plans`, `POST /api/cultivation-plans` | FARMER | List or create the signed-in farmer's plans |
| `PATCH /api/cultivation-plans/{id}/status` | FARMER | Update a plan status |
| `PATCH /api/cultivation-plans/{id}/harvest` | FARMER | Record actual yield and harvest date |
| `GET /api/recommendations`, `GET /api/recommendations/{id}` | FARMER | View saved recommendations |
| `POST /api/recommendations/generate` | FARMER | Generate recommendations for the farmer's profile location |

REST responses use DTOs. Incoming request DTOs are validated and errors are returned through centralized API error handling.

Interactive API documentation is available at `http://localhost:8081/swagger-ui.html`; the generated OpenAPI JSON is at `http://localhost:8081/v3/api-docs`. Swagger UI's **Authorize** dialog accepts the JWT returned by login or registration. The OpenAPI and Swagger UI routes are public in this development setup. Set both `API_DOCS_ENABLED=false` and `SWAGGER_UI_ENABLED=false` in production unless the documentation is intentionally exposed.

## Explainable recommendations

The deterministic score is out of 100: market demand and supply balance contribute up to 40 points, current-season suitability up to 30, and the farmer's average completed-plan harvested-to-planned ratio up to 30. Current seasons are mapped to Kharif (June–October), Rabi (November–March), and Zaid (April–May). Harvest ratios are averaged per plan so quantity values from different plans are not added together. When there is no completed harvest history, that component receives a neutral 15 points. Each response stores component scores, decision, explanation, and the demand-data date used. Demand data for the farmer's profile location is required. Farmers record realized output through `PATCH /api/cultivation-plans/{id}/harvest` so future recommendations can use their history.

Recommendations are planning guidance, not guaranteed yield or market predictions.

## Production readiness

Flyway manages database changes through the scripts in `src/main/resources/db/migration`; the current migration set creates the domain tables and adds indexes for the main demand, cultivation-plan, and recommendation queries. Hibernate uses `ddl-auto=validate` and fails startup if the schema does not match the entity mappings. For an existing database, baseline only after a backup and schema review. For production, store secrets in the hosting environment's secret manager, configure the allowed frontend origin or a same-origin reverse proxy, disable public API documentation unless intentionally exposed, and set production database connection and backup policies. No Docker image or deployment manifests are included yet.
