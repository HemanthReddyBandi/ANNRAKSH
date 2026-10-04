# ANNRAKSH
ANNRAKSH is a Java Full Stack agricultural planning platform that helps reduce overproduction and food waste through demand-aware crop recommendations, cultivation planning, and explainable market insights.

## Local backend configuration

The backend runs on port `8081`. Set the database password and a private JWT signing key in the environment before starting the application. `JWT_SECRET` must contain at least 32 UTF-8 bytes; this PowerShell command creates a fresh 256-bit value encoded as text:

```powershell
$env:DB_PASSWORD = "your-local-postgres-password"
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
mvn spring-boot:run
```

`DB_URL` and `DB_USERNAME` can override the local PostgreSQL defaults. Do not commit these values. Hibernate schema update is enabled for local development only; use managed schema migrations before production deployment.

## Authentication

- `POST /api/auth/register` creates a FARMER account and returns a bearer token.
- `POST /api/auth/login` authenticates an existing account and returns a bearer token.
- Send protected API requests with `Authorization: Bearer <accessToken>`.
- Public registration always assigns the FARMER role. To provision the first administrator, promote a trusted account out of band, for example: `UPDATE farmers SET role = 'ADMIN' WHERE email = 'admin@example.com';`. Never add public administrator registration.

## Explainable recommendations

Farmers can generate a fresh recommendation snapshot with `POST /api/recommendations/generate`; the API uses the authenticated farmer's profile location. The deterministic score is out of 100: market demand and supply balance contribute up to 40 points, current-season suitability up to 30, and the farmer's average completed-plan harvested-to-planned ratio up to 30. The current season is mapped to Kharif (June–October), Rabi (November–March), and Zaid (April–May). The per-plan ratios are averaged so records using different quantity units are not added together. When there is no completed harvest history, that component receives a neutral 15 points. Each response stores the component scores, decision, explanation, and demand-data date used. A current demand record for the farmer's location is required. Farmers record realized output with `PATCH /api/cultivation-plans/{id}/harvest` so future recommendations can use actual yields.
