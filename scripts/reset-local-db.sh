#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.yml"
DB_USER="assetmap"
DB_PASSWORD="assetmap"

cd "${ROOT_DIR}"

docker compose -f "${COMPOSE_FILE}" down -v
docker compose -f "${COMPOSE_FILE}" up -d

echo "Waiting for MySQL..."
for _ in {1..60}; do
  if docker exec asset-map-mysql mysqladmin ping -u"${DB_USER}" -p"${DB_PASSWORD}" --silent >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

docker exec asset-map-mysql mysqladmin ping -u"${DB_USER}" -p"${DB_PASSWORD}" --silent >/dev/null

echo "Local MySQL is ready on localhost:33308."
echo "Seed is not applied automatically. Start the backend once to create tables, then apply backend/src/main/resources/db/local/seed-minimal.sql manually if needed."
