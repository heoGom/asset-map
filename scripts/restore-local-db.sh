#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: ./scripts/restore-local-db.sh backups/asset_map_dump.sql" >&2
  exit 1
fi

DUMP_FILE="$1"
if [[ ! -f "${DUMP_FILE}" ]]; then
  echo "Dump file not found: ${DUMP_FILE}" >&2
  exit 1
fi

DB_NAME="asset_map"
DB_USER="assetmap"
DB_PASSWORD="assetmap"
ROOT_PASSWORD="root"

docker exec asset-map-mysql mysql -uroot -p"${ROOT_PASSWORD}" \
  -e "DROP DATABASE IF EXISTS ${DB_NAME}; CREATE DATABASE ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'%'; FLUSH PRIVILEGES;"

docker exec -i asset-map-mysql mysql -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" < "${DUMP_FILE}"

echo "Restored ${DUMP_FILE} into ${DB_NAME}."
