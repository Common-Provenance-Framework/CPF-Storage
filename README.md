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

## API Usage Manual

This section provides a standalone end-to-end example for calling the CPF-Store API directly:

1. Register organization with id `ORG_ID` (default: `myorg_01`).
2. Sign a JSON document with the organization private key (SHA-256).
3. Base64-encode the document and signature.
4. Upload the new document.

For steps 1-4 below, run commands from the `sandbox` directory unless a step says otherwise.

### 1) Generate certificates (sandbox/demo)

Run from the repository root. This creates the files expected by the API examples and scripts:
- `sandbox/certificates/$ORG_ID.pem`
- `sandbox/certificates/$ORG_ID.key`
- `sandbox/certificates/int1.pem`
- `sandbox/certificates/int2.pem`

#### Prepare sandbox directory
```bash
mkdir -p sandbox && cd sandbox
ORG_ID="myorg_01"
mkdir -p certificates
```

#### Generate or copy root CA
Generate root CA only if necessary.

```bash
# Root CA (local demo only, EC key)
openssl ecparam -name prime256v1 -genkey -noout -out certificates/root_ca.key
openssl req -x509 -new -key certificates/root_ca.key -sha256 -days 3650 \
   -subj "/C=CZ/O=CPF/CN=cpf-root-ca" \
   -out certificates/root_ca.pem
```

If you run Trusted Party from Docker, prefer its CA:

```bash
# Copy Root CA (TrustedParty)
cp ../../CPF-Search-API/prov-storage/trusted_party/config/certificates/trusted_certs/ca.pem certificates/root_ca.pem
cp ../../CPF-Search-API/prov-storage/trusted_party/config/certificates/trusted_keys/ca.key certificates/root_ca.key
```

#### Generate intermediate certificates (EC)

```bash
# Intermediate 1
cat > certificates/v3_int1.ext <<'EOF'
basicConstraints=critical,CA:TRUE,pathlen:1
keyUsage=critical,keyCertSign,cRLSign
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

openssl ecparam -name prime256v1 -genkey -noout -out certificates/int1.key
openssl req -new -key certificates/int1.key \
   -subj "/C=CZ/O=CPF/CN=cpf-int1" \
   -out certificates/int1.csr
openssl x509 -req -in certificates/int1.csr \
   -CA certificates/root_ca.pem -CAkey certificates/root_ca.key -CAcreateserial \
   -out certificates/int1.pem -days 1825 -sha256 \
   -extfile certificates/v3_int1.ext
```

```bash
# Intermediate 2
cat > certificates/v3_int2.ext <<'EOF'
basicConstraints=critical,CA:TRUE,pathlen:0
keyUsage=critical,keyCertSign,cRLSign
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

openssl ecparam -name prime256v1 -genkey -noout -out certificates/int2.key
openssl req -new -key certificates/int2.key \
   -subj "/C=CZ/O=CPF/CN=cpf-int2" \
   -out certificates/int2.csr
openssl x509 -req -in certificates/int2.csr \
   -CA certificates/int1.pem -CAkey certificates/int1.key -CAcreateserial \
   -out certificates/int2.pem -days 1825 -sha256 \
   -extfile certificates/v3_int2.ext
```

#### Generate organization certificate (EC)
```bash
cat > certificates/v3_client.ext <<'EOF'
basicConstraints=critical,CA:FALSE
keyUsage=critical,digitalSignature
extendedKeyUsage=clientAuth
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

openssl ecparam -name prime256v1 -genkey -noout -out "certificates/$ORG_ID.key"
openssl req -new -key "certificates/$ORG_ID.key" \
   -subj "/C=CZ/O=CPF/CN=$ORG_ID" \
   -out "certificates/$ORG_ID.csr"
openssl x509 -req -in "certificates/$ORG_ID.csr" \
   -CA certificates/int2.pem -CAkey certificates/int2.key -CAcreateserial \
   -out "certificates/$ORG_ID.pem" -days 825 -sha256 \
   -extfile certificates/v3_client.ext
```

#### Check your certificates
Optional sanity checks:

```bash
openssl x509 -in "certificates/$ORG_ID.pem" -noout -subject -issuer
openssl x509 -in "certificates/$ORG_ID.pem" -noout -text | grep "Public Key Algorithm"
openssl verify -CAfile certificates/root_ca.pem \
   -untrusted <(cat certificates/int1.pem certificates/int2.pem) \
   "certificates/$ORG_ID.pem"
```

#### Clean certificates directory
Cleanup temporary files (CSRs, extension configs, serial files):

```bash
rm -f certificates/*.csr \
      certificates/*.ext \
      certificates/*.srl
```

### 2) Register Organization

If you run with Docker Compose, use port `8081` instead of `8080`.
This step registers `$ORG_ID` and uploads its client certificate + intermediate chain.



```bash
curl --location "http://localhost:8080/api/v1/organizations" \
  --header "Content-Type: application/json" \
  --data "$(jq -n \
    --arg identifier "$ORG_ID" \
    --arg clientCertificate "$(tr -d '\r' < "./certificates/$ORG_ID.pem")" \
    --arg int1 "$(tr -d '\r' < ./certificates/int1.pem)" \
    --arg int2 "$(tr -d '\r' < ./certificates/int2.pem)" \
    --argjson clearancePeriod 30 \
    '{
      identifier: $identifier,
      clientCertificate: $clientCertificate,
      intermediateCertificates: [$int1, $int2],
      clearancePeriod: $clearancePeriod
    }' \
  )" | jq
```

### 3) Set Document Path

Set `DOC_PATH` to your own valid PROV JSON file before uploading the document.
Working examples of finalized provenance documents are available in the [CPF-Toolbox](https://github.com/Common-Provenance-Framework/CPF-Toolbox/tree/main/cpm-template/src/test/resources) project.

```bash
DOC_PATH="./documents/prov.json"
[ -f "$DOC_PATH" ] || { echo "File not found: $DOC_PATH"; exit 1; }

```

### 4) Upload New Document

This command uses `ORG_ID` and `DOC_PATH`, then generates base64 document content, signature, and timestamp inline.

```bash
curl --location "http://localhost:8080/api/v1/documents" \
  --header 'Accept: application/json' \
  --header 'Content-Type: application/json' \
  --data "$(jq -n \
    --arg organizationIdentifier "$ORG_ID" \
    --arg document "$(openssl base64 -A -in "$DOC_PATH")" \
    --arg documentFormat "json" \
    --arg signature "$(openssl dgst -sha256 -sign "./certificates/$ORG_ID.key" "$DOC_PATH" | openssl base64 -A)" \
    --arg createdOn $(date +%s) \
    --argjson clearancePeriod 30 \
    '{
      organizationIdentifier: $organizationIdentifier,
      document: $document,
      documentFormat: $documentFormat,
      signature: $signature,
      createdOn: $createdOn,
      clearancePeriod: $clearancePeriod
    }' \
  )" | jq
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

