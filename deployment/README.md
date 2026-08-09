# Deployment CPF-Store with NRO-Service form prebuild images


## Generate certificates
### Prepare certificates directories

```
cd deployment
mkdir -p certs/trusted
mkdir -p certs/nro
mkdir -p certs/organization
mkdir -p certs/intermediate
```

### Generate root CA

```
openssl ecparam -name prime256v1 -genkey -noout -out ./certs/ca.key
openssl req -x509 -new -key ./certs/ca.key -sha256 -days 3650 \
   -subj "/C=CZ/O=CPF/CN=cpf-root-ca" \
   -out ./certs/trusted/ca.pem
```

### Generate NRO Service certificate (EC)
#### Generate certificate
```
cat > certs/nro/v3_nro.ext <<'EOF'
basicConstraints=critical,CA:FALSE
keyUsage=critical,digitalSignature
extendedKeyUsage=clientAuth
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

openssl ecparam -name prime256v1 -genkey -noout -out ./certs/nro/nro.key

openssl req -new -key ./certs/nro/nro.key \
   -subj "/C=CZ/O=CPF/CN=NRO-Service" \
   -out ./certs/nro/nro.csr

openssl x509 -req -in ./certs/nro/nro.csr \
   -CA ./certs/trusted/ca.pem -CAkey ./certs/ca.key -CAcreateserial \
   -out ./certs/nro/nro.pem -days 825 -sha256 \
   -extfile certs/nro/v3_nro.ext
```

#### Check your certificates
Optional sanity checks:
```
openssl x509 -in ./certs/nro/nro.pem -noout -subject -issuer
openssl x509 -in ./certs/nro/nro.pem -noout -text | grep "Public Key Algorithm"
openssl verify -CAfile ./certs/trusted/ca.pem ./certs/nro/nro.pem
```

#### Clean certificates directory
Cleanup temporary files (CSRs, extension configs, serial files):
```
rm -f ./certs/nro/nro.csr \
      ./certs/nro/v3_nro.ext \
      ./certs/trusted/ca.srl
```

### Generate intermediate certificates (EC)
```
# Intermediate 1
cat > ./certs/intermediate/v3_int1.ext <<'EOF'
basicConstraints=critical,CA:TRUE,pathlen:1
keyUsage=critical,keyCertSign,cRLSign
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

openssl ecparam -name prime256v1 -genkey -noout -out ./certs/intermediate/int1.key
openssl req -new -key ./certs/intermediate/int1.key \
   -subj "/C=CZ/O=CPF/CN=cpf-int1" \
   -out ./certs/intermediate/int1.csr
openssl x509 -req -in ./certs/intermediate/int1.csr \
   -CA ./certs/trusted/ca.pem -CAkey ./certs/ca.key -CAcreateserial \
   -out ./certs/intermediate/int1.pem -days 1825 -sha256 \
   -extfile ./certs/intermediate/v3_int1.ext
```

```
# Intermediate 2
cat > ./certs/intermediate/v3_int2.ext <<'EOF'
basicConstraints=critical,CA:TRUE,pathlen:0
keyUsage=critical,keyCertSign,cRLSign
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

openssl ecparam -name prime256v1 -genkey -noout -out ./certs/intermediate/int2.key
openssl req -new -key ./certs/intermediate/int2.key \
   -subj "/C=CZ/O=CPF/CN=cpf-int2" \
   -out ./certs/intermediate/int2.csr
openssl x509 -req -in ./certs/intermediate/int2.csr \
   -CA ./certs/intermediate/int1.pem -CAkey ./certs/intermediate/int1.key -CAcreateserial \
   -out ./certs/intermediate/int2.pem -days 1825 -sha256 \
   -extfile ./certs/intermediate/v3_int2.ext
```

### Generate organization certificate (EC)
#### Prepare organization id
```
export ORG_ID="6fb292aa-ee38-48ae-998f-079ad9d01e7c"
```
#### Generate certificate
```
cat > ./certs/organization/v3_client.ext <<'EOF'
basicConstraints=critical,CA:FALSE
keyUsage=critical,digitalSignature
extendedKeyUsage=clientAuth
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
EOF

openssl ecparam -name prime256v1 -genkey -noout -out "./certs/organization/$ORG_ID.key"
openssl req -new -key "./certs/organization/$ORG_ID.key" \
   -subj "/C=CZ/O=CPF/CN=$ORG_ID" \
   -out "./certs/organization/$ORG_ID.csr"
openssl x509 -req -in "./certs/organization/$ORG_ID.csr" \
   -CA ./certs/intermediate/int2.pem -CAkey ./certs/intermediate/int2.key -CAcreateserial \
   -out "./certs/organization/$ORG_ID.pem" -days 825 -sha256 \
   -extfile ./certs/organization/v3_client.ext
```

#### Check your certificates
Optional sanity checks:
```
openssl x509 -in "./certs/organization/$ORG_ID.pem" -noout -subject -issuer
openssl x509 -in "./certs/organization/$ORG_ID.pem" -noout -text | grep "Public Key Algorithm"
openssl verify -CAfile ./certs/trusted/ca.pem \
   -untrusted <(cat ./certs/intermediate/int1.pem ./certs/intermediate/int2.pem) \
   "./certs/organization/$ORG_ID.pem"
```

#### Clean certificates directory
Cleanup temporary files (CSRs, extension configs, serial files):
```
rm -f ./certs/trusted/ca.srl \
      ./certs/intermediate/int1.csr \
      ./certs/intermediate/int1.srl \
      ./certs/intermediate/v3_int1.ext \
      ./certs/intermediate/int2.csr \
      ./certs/intermediate/int2.srl \
      ./certs/intermediate/v3_int2.ext \
      "./certs/organization/$ORG_ID.csr" \
      ./certs/organization/v3_client.ext

```

### Run in docker
Run commands from the `./deployment` directory.

>[!CAUTION]
>`STORE_URL` is required by compose and must match the URL where this Store service is reachable by your clients.
>Examples:
>- Local machine: `http://localhost:8081/api/v1/`
>- Sandbox/server: `http://<server-ip-or-dns>:8081/api/v1/`
>- Reverse proxy/TLS: `https://store.example.com/api/v1/`

```bash
cd ./deployment
export STORE_URL=http://localhost:8081/api/v1/
docker compose up --detach
```

One-shot alternative:

```bash
STORE_URL=http://localhost:8081/api/v1/ docker compose up --detach
```