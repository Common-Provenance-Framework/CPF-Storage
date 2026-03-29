# CPF-Store
Store service of the Common Provenance Framework.

## Run with Docker Compose

This repository contains a `docker-compose.yaml` that runs:
- CPF Store (`cpf-store`)
- Neo4j (`cpf-neo4j`)
- Trusted Party (`cpf-trusted-party`)
- PostgreSQL for Trusted Party (`cpf-postgres`)

### Prerequisites

1. Docker Engine + Docker Compose installed on the machine where you run the stack.
2. Both repositories cloned next to each other (sibling folders), because Trusted Party is built from `../CPF-Search-API/prov-storage/`.

Example:

```bash
git clone git@github.com:Common-Provenance-Framework/CPF-Search-API.git
git clone git@github.com:Common-Provenance-Framework/CPF-Storage.git
```

### Build and start

Run commands from the `CPF-Storage` root directory.

`STORE_URL` is required by compose and must match the URL where this Store service is reachable by your clients.

Examples:
- Local machine: `http://localhost:8081/api/v1/`
- Sandbox/server: `http://<server-ip-or-dns>:8081/api/v1/`
- Reverse proxy/TLS: `https://store.example.com/api/v1/`

```bash
cd CPF-Storage
export STORE_URL=http://localhost:8081/api/v1/
docker compose up --build --detach
```

One-shot alternative:

```bash
STORE_URL=http://localhost:8081/api/v1/ docker compose up --build --detach
```

### Verify services

```bash
docker compose ps
curl http://localhost:8081/health/
docker compose logs -f cpf-store
```

> Note: `http://localhost:8081/health/` is not implemented yet.
> At this stage, the following response is accepted:
>
> ```json
> {
>   "code": "InternalError",
>   "message": "*** Unhandled Server Error ***"
> }
> ```

### Stop stack

```bash
docker compose down
```

### Remove all data volumes (Neo4j + PostgreSQL)

```bash
docker compose down -v
```

## API Documentation (OpenAPI / Swagger)

CPF-Store exposes interactive API documentation powered by [SpringDoc OpenAPI](https://springdoc.org/).

### Endpoints

| Resource | URL (dev default) | URL (Docker Compose) |
|---|---|---|
| Swagger UI | `http://localhost:8080/swagger-ui.html` | `http://localhost:8081/swagger-ui.html` |
| OpenAPI JSON spec | `http://localhost:8080/v3/api-docs` | `http://localhost:8081/v3/api-docs` |

### Using Swagger UI

1. Start the application (standalone or via Docker Compose).
2. Open the Swagger UI URL in your browser.
3. Browse the three API groups using the top-right **Select a definition** dropdown:
   - **Documents** — endpoints for storing and retrieving provenance documents (`/api/v1/documents/**`)
   - **Organizations** — endpoints for managing organizations (`/api/v1/organizations/**`)
   - **Meta Documents** — endpoints for querying document metadata (`/api/v1/documents/meta/**`)

### Configuring the target server

The Swagger UI exposes a **Servers** dropdown that lets you change the target host and scheme without editing any files:

- **scheme** — `http` (default) or `https`
- **host** — hostname and port, e.g. `localhost:8080` (default) or `myserver.example.com`

Select the appropriate values before using **Try it out** to send requests to the correct environment.

### Authentication

All endpoints are documented with a **Bearer JWT** security scheme (`bearerAuth`). To authorize requests in the UI:

1. Click the **Authorize** button (lock icon) in the top-right corner of Swagger UI.
2. Enter your JWT token in the `bearerAuth` field (without the `Bearer ` prefix).
3. Click **Authorize**, then **Close**.

Subsequent **Try it out** requests will include the `Authorization: Bearer <token>` header automatically.

> Note: Security is documented in the OpenAPI spec but not yet enforced server-side. The `Authorize` step only affects Swagger UI request headers.

