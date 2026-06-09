#!/bin/bash
# Build the alert service image. Defaults to linux/amd64 for deployment to typical Linux hosts.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
IMAGE="${DOCKER_IMAGE:-quachtd/btp-is-alert-service:1.0.0}"
PLATFORM="${DOCKER_PLATFORM:-linux/amd64}"
USE_PREBUILT="${USE_PREBUILT:-false}"

cd "$SCRIPT_DIR"

if [[ "$USE_PREBUILT" == "true" ]]; then
  echo "Building from pre-built JAR (Dockerfile.prebuilt)..."
  mvn -q -DskipTests package
  docker build -f Dockerfile.prebuilt -t "$IMAGE" . --platform "$PLATFORM"
else
  echo "Building multi-stage image for platform $PLATFORM..."
  if docker buildx version >/dev/null 2>&1; then
    docker buildx build \
      --platform "$PLATFORM" \
      -t "$IMAGE" \
      --load \
      .
  else
    docker build -t "$IMAGE" . --platform "$PLATFORM"
  fi
fi

echo "Built $IMAGE"
