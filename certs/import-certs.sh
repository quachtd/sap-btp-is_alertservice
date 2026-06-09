#!/bin/bash
# Import DigiCert G5 CA chain required by CPI (*.it-cpi034.cfapps...).
# Run from project root: ./certs/import-certs.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home)}"
SYSTEM_CACERTS="$JAVA_HOME/lib/security/cacerts"
CUSTOM_CACERTS="$SCRIPT_DIR/custom-cacerts.jks"
CPI_HOST="plh-integration-dev.it-cpi034.cfapps.us10-002.hana.ondemand.com"

echo "Java: $JAVA_HOME"
echo "Fetching certificate chain from $CPI_HOST ..."

openssl s_client -connect "$CPI_HOST:443" -servername "$CPI_HOST" -showcerts </dev/null 2>/dev/null \
  > "$SCRIPT_DIR/cpi-full-chain.pem"

python3 - "$SCRIPT_DIR" <<'PY'
import re, pathlib, sys
script_dir = pathlib.Path(sys.argv[1])
text = (script_dir / "cpi-full-chain.pem").read_text()
certs = re.findall(r"-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----", text, re.S)
for i, cert in enumerate(certs, 1):
    (script_dir / f"cpi-cert-{i}.pem").write_text(cert + "\n")
print(f"Saved {len(certs)} certificates")
PY

if [[ ! -f "$SCRIPT_DIR/cpi-cert-2.pem" || ! -f "$SCRIPT_DIR/cpi-cert-3.pem" ]]; then
  echo "Failed to extract intermediate/root certificates."
  exit 1
fi

echo ""
echo "=== Option A: project-local truststore (no sudo) ==="
cp "$SYSTEM_CACERTS" "$CUSTOM_CACERTS"
keytool -importcert -alias digicert-g5-intermediate-2021 \
  -file "$SCRIPT_DIR/cpi-cert-2.pem" \
  -keystore "$CUSTOM_CACERTS" -storepass changeit -noprompt
keytool -importcert -alias digicert-g5-root \
  -file "$SCRIPT_DIR/cpi-cert-3.pem" \
  -keystore "$CUSTOM_CACERTS" -storepass changeit -noprompt
echo "Created: $CUSTOM_CACERTS"
echo ""
echo "Run the app with:"
echo "  TRUSTSTORE_OPTS=\"-Djavax.net.ssl.trustStore=$CUSTOM_CACERTS -Djavax.net.ssl.trustStorePassword=changeit\""
echo "  mvn spring-boot:run -Dspring-boot.run.profiles=sample -Dspring-boot.run.jvmArguments=\"\$TRUSTSTORE_OPTS\""

echo ""
echo "=== Option B: system JDK truststore (requires sudo password) ==="
echo "sudo keytool -importcert -alias digicert-g5-intermediate-2021 \\"
echo "  -file $SCRIPT_DIR/cpi-cert-2.pem \\"
echo "  -keystore $SYSTEM_CACERTS -storepass changeit -noprompt"
echo "sudo keytool -importcert -alias digicert-g5-root \\"
echo "  -file $SCRIPT_DIR/cpi-cert-3.pem \\"
echo "  -keystore $SYSTEM_CACERTS -storepass changeit -noprompt"
