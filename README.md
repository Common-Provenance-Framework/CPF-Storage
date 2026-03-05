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