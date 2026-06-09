#!/bin/bash
# Run the alert service container with externalized config from .env and mounted truststore.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="${ENV_FILE:-$SCRIPT_DIR/.env}"
TRUSTSTORE="${TRUSTSTORE:-$SCRIPT_DIR/certs/custom-cacerts.jks}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing $ENV_FILE. Copy .env.example to .env and fill in values."
  exit 1
fi

if [[ ! -f "$TRUSTSTORE" ]]; then
  echo "Truststore not found at $TRUSTSTORE. Run: ./certs/import-certs.sh"
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

: "${DOCKER_IMAGE:?Set DOCKER_IMAGE in .env (e.g. <dockerhub-user>/btp-is-alert-service:latest)}"

TRUSTSTORE_PASSWORD="${TRUSTSTORE_PASSWORD:-changeit}"
export JAVA_OPTS="-Djavax.net.ssl.trustStore=/app/certs/custom-cacerts.jks -Djavax.net.ssl.trustStorePassword=${TRUSTSTORE_PASSWORD}"

docker run --rm \
  --env-file "$ENV_FILE" \
  -e JAVA_OPTS \
  -v "$TRUSTSTORE:/app/certs/custom-cacerts.jks:ro" \
  "$DOCKER_IMAGE"
