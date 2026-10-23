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
| `BOOTSTRAP_ADMIN_ENABLED` | No | Set to `true` for one-time initial administrator setup; defaults to `false` |
| `BOOTSTRAP_ADMIN_EMAIL` | Only when bootstrap is enabled and no admin exists | Email for the initial administrator |
| `BOOTSTRAP_ADMIN_NAME` | Only when bootstrap is enabled and no admin exists | Display name for the initial administrator |
| `BOOTSTRAP_ADMIN_PASSWORD` | Only when bootstrap is enabled and no admin exists | Initial password; 12–72 UTF-8 bytes; remove after setup |
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

## Run the backend and PostgreSQL with Docker

Docker Compose starts PostgreSQL, the Spring Boot API, and the React frontend served by Nginx. Nginx forwards `/api` requests to Spring Boot, so the browser uses one origin. Copy `.env.example` to `.env`, replace `DB_PASSWORD`, and set `JWT_SECRET` to a randomly generated value of at least 32 bytes. For example, PowerShell can generate a Base64 secret with:

```powershell
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

Start both services from the repository root:

```powershell
docker compose up --build
```

Open the app at `http://localhost:8080`. The API is also directly available at `http://localhost:8081` for local API tooling. PostgreSQL data persists in the `postgres_data` Docker volume. Stop the services with `Ctrl+C`, then run `docker compose down` to remove the containers while keeping the database volume. The Vite development workflow described above remains available. Do not use the example values as production credentials; configure deployment secrets through the host's secret manager.

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
- Public registration always assigns the FARMER role. For the first administrator on a new installation, set `BOOTSTRAP_ADMIN_ENABLED=true` and provide `BOOTSTRAP_ADMIN_EMAIL`, `BOOTSTRAP_ADMIN_NAME`, and `BOOTSTRAP_ADMIN_PASSWORD` in the environment before starting the API. Bootstrap creates an administrator only when no admin exists, refuses to change an existing farmer's role, and logs no password. Disable it and remove its password variable after successful startup. For an existing installation, an administrator can still be provisioned out of band after verifying the account, for example:

  ```sql
  UPDATE farmers SET role = 'ADMIN' WHERE email = 'admin@example.com';
  ```

- Passwords are stored as BCrypt hashes. There is no public administrator registration.

## API overview

| Method and path | Access | Purpose |
|---|---|---|
| `POST /api/auth/register` | Public | Register a farmer |
| `POST /api/auth/login` | Public | Sign in |
| `GET /api/farmer/profile`, `PUT /api/farmer/profile` | FARMER | View and update the signed-in farmer's name, phone, and market location |
| `GET /api/crops`, `GET /api/crops/{id}` | Public | Browse the crop library |
| `POST /api/crops`, `PUT /api/crops/{id}`, `DELETE /api/crops/{id}` | ADMIN | Maintain crop records |
| `GET /api/demand?location={location}` | Public | Browse demand snapshots; location filter is optional |
| `POST /api/demand` | ADMIN | Record a demand snapshot |
| `GET /api/cultivation-plans`, `POST /api/cultivation-plans` | FARMER | List or create the signed-in farmer's plans |
| `PATCH /api/cultivation-plans/{id}/status` | FARMER | Update a plan status |
| `PATCH /api/cultivation-plans/{id}/harvest` | FARMER | Record actual yield and harvest date |
| `PUT /api/cultivation-plans/{id}/harvest-disposition` | FARMER | Update sold, stored, and discarded totals with an audit record |
| `GET /api/cultivation-plans/{id}/harvest-disposition/history` | FARMER | View disposition changes for an owned plan |
| `GET /api/admin/dashboard` | ADMIN | View persisted platform totals and plan statuses |
| `GET /api/admin/farmers?page={page}&size={size}&search={text}` | ADMIN | Search and page through farmer profiles |
| `GET /api/admin/cultivation-plans?page={page}&size={size}&status={status}&search={text}` | ADMIN | Monitor farmer plans with optional status and farmer/crop filters |
| `GET /api/admin/agricultural-statistics` | ADMIN | Review latest crop/location supply signals and grouped harvest outcomes |
| `GET /api/admin/cultivation-plans/{id}/harvest-disposition/history` | ADMIN | Review disposition audit history for a cultivation plan |
| `GET /api/recommendations`, `GET /api/recommendations/{id}` | FARMER | View saved recommendations |
| `POST /api/recommendations/generate` | FARMER | Generate recommendations for the farmer's profile location |

REST responses use DTOs. Incoming request DTOs are validated and errors are returned through centralized API error handling.

When recording a harvest, farmers may also record quantities sold, stored, and discarded. These disposition quantities cannot exceed the actual harvest; any remainder is reported as unallocated. Admin harvest statistics group output by crop and quantity unit. A potential supply surplus is based on the newest demand snapshot for a crop and location and is not a measurement of food waste.

Interactive API documentation is available at `http://localhost:8081/swagger-ui.html`; the generated OpenAPI JSON is at `http://localhost:8081/v3/api-docs`. Swagger UI's **Authorize** dialog accepts the JWT returned by login or registration. The OpenAPI and Swagger UI routes are public in this development setup. Set both `API_DOCS_ENABLED=false` and `SWAGGER_UI_ENABLED=false` in production unless the documentation is intentionally exposed.

## Explainable recommendations

The deterministic score is out of 100: market demand and supply balance contribute up to 40 points, current-season suitability up to 30, and the farmer's average completed-plan harvested-to-planned ratio up to 30. Current seasons are mapped to Kharif (June–October), Rabi (November–March), and Zaid (April–May). Harvest ratios are averaged per plan so quantity values from different plans are not added together. When there is no completed harvest history, that component receives a neutral 15 points. Each response stores component scores, decision, explanation, and the demand-data date used. Demand data for the farmer's profile location is required. Farmers record realized output through `PATCH /api/cultivation-plans/{id}/harvest` so future recommendations can use their history.

Recommendations are planning guidance, not guaranteed yield or market predictions. Farmers can update their market location from the Profile page; the location must match the location labels used by demand records.

## Production readiness

Flyway manages database changes through the scripts in `src/main/resources/db/migration`; the current migration set creates the domain tables and adds indexes for the main demand, cultivation-plan, and recommendation queries. Hibernate uses `ddl-auto=validate` and fails startup if the schema does not match the entity mappings. For an existing database, baseline only after a backup and schema review. For production, store secrets in the hosting environment's secret manager, disable public API documentation unless intentionally exposed, and set production database connection and backup policies. The Docker Compose setup is intended for local evaluation and runs the frontend, backend, and PostgreSQL together. It is not a complete production deployment: TLS termination, database backups, monitoring, and host-specific secret management still need to be configured for the chosen deployment platform.
