#!/bin/bash
# Run with sample_input.json + custom truststore for CPI SSL.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TRUSTSTORE="$SCRIPT_DIR/certs/custom-cacerts.jks"

if [[ ! -f "$TRUSTSTORE" ]]; then
  echo "Custom truststore not found. Run: ./certs/import-certs.sh"
  exit 1
fi

TRUSTSTORE_OPTS="-Djavax.net.ssl.trustStore=$TRUSTSTORE -Djavax.net.ssl.trustStorePassword=changeit"

ENV_FILE="${ENV_FILE:-$SCRIPT_DIR/.env}"
if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
else
  echo "No .env file found. CPI OAuth credentials (CPI_CLIENT_ID, CPI_CLIENT_SECRET) must be exported."
fi

mvn spring-boot:run \
  -Dspring-boot.run.profiles=sample \
  -Dspring-boot.run.jvmArguments="$TRUSTSTORE_OPTS"
