#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKUP_DIR="${ROOT_DIR}/backups"
DB_NAME="asset_map"
DB_USER="assetmap"
DB_PASSWORD="assetmap"
LABEL="${1:-manual}"
TIMESTAMP="$(date +"%Y%m%d-%H%M%S")"
OUTPUT_FILE="${BACKUP_DIR}/asset_map_${LABEL}_${TIMESTAMP}.sql"

mkdir -p "${BACKUP_DIR}"

docker exec asset-map-mysql mysqldump \
  -u"${DB_USER}" \
  -p"${DB_PASSWORD}" \
  --single-transaction \
  --no-tablespaces \
  --routines \
  --triggers \
  "${DB_NAME}" > "${OUTPUT_FILE}"

echo "Backup written to ${OUTPUT_FILE}"
