#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
SQL_FILE="${PROJECT_ROOT}/docs/databases/migrate-rag-schema.sql"
BACKUP_DIR="${PROJECT_ROOT}/logs/db-backups"

RAG_DB_HOST="${RAG_DB_HOST:-localhost}"
RAG_DB_PORT="${RAG_DB_PORT:-3306}"
RAG_DB_NAME="${RAG_DB_NAME:-sxh_rag}"
RAG_DB_USER="${RAG_DB_USER:-root}"
RAG_DB_PASSWORD="${RAG_DB_PASSWORD:-Baoyu273511a}"
RAG_DB_DROP_UNKNOWN="${RAG_DB_DROP_UNKNOWN:-true}"

readonly ALLOWED_TABLES=(
  article_knowledge
  chunk_info
  conversations
  document_vectors
  file_upload
  organization_tags
  users
)

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

contains_allowed_table() {
  local table="$1"
  local allowed
  for allowed in "${ALLOWED_TABLES[@]}"; do
    if [[ "${allowed}" == "${table}" ]]; then
      return 0
    fi
  done
  return 1
}

require_command mysql
require_command mysqldump

if [[ ! -f "${SQL_FILE}" ]]; then
  echo "Migration SQL not found: ${SQL_FILE}" >&2
  exit 1
fi

mkdir -p "${BACKUP_DIR}"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_FILE="${BACKUP_DIR}/${RAG_DB_NAME}-${TIMESTAMP}.sql"

echo "Backing up database ${RAG_DB_NAME} to ${BACKUP_FILE}"
MYSQL_PWD="${RAG_DB_PASSWORD}" mysqldump \
  -h "${RAG_DB_HOST}" \
  -P "${RAG_DB_PORT}" \
  -u "${RAG_DB_USER}" \
  --databases "${RAG_DB_NAME}" > "${BACKUP_FILE}"

echo "Applying schema migration from ${SQL_FILE}"
MYSQL_PWD="${RAG_DB_PASSWORD}" mysql \
  -h "${RAG_DB_HOST}" \
  -P "${RAG_DB_PORT}" \
  -u "${RAG_DB_USER}" \
  "${RAG_DB_NAME}" < "${SQL_FILE}"

if [[ "${RAG_DB_DROP_UNKNOWN}" == "true" ]]; then
  echo "Cleaning unknown tables from ${RAG_DB_NAME}"
  while IFS= read -r table_name; do
    if ! contains_allowed_table "${table_name}"; then
      echo "Dropping unrelated table: ${table_name}"
      MYSQL_PWD="${RAG_DB_PASSWORD}" mysql \
        -h "${RAG_DB_HOST}" \
        -P "${RAG_DB_PORT}" \
        -u "${RAG_DB_USER}" \
        -e "DROP TABLE IF EXISTS \`${RAG_DB_NAME}\`.\`${table_name}\`"
    fi
  done < <(
    MYSQL_PWD="${RAG_DB_PASSWORD}" mysql \
      -h "${RAG_DB_HOST}" \
      -P "${RAG_DB_PORT}" \
      -u "${RAG_DB_USER}" \
      -N -B \
      -e "SELECT table_name FROM information_schema.tables WHERE table_schema = '${RAG_DB_NAME}' ORDER BY table_name"
  )
fi

echo "Final tables in ${RAG_DB_NAME}:"
MYSQL_PWD="${RAG_DB_PASSWORD}" mysql \
  -h "${RAG_DB_HOST}" \
  -P "${RAG_DB_PORT}" \
  -u "${RAG_DB_USER}" \
  -N -B \
  -e "SELECT table_name FROM information_schema.tables WHERE table_schema = '${RAG_DB_NAME}' ORDER BY table_name"

echo "RAG database migration finished."
