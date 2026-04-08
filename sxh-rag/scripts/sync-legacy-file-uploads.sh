#!/usr/bin/env bash
set -euo pipefail

SRC_DB="${SRC_DB:-PaiSmart}"
DST_DB="${DST_DB:-pai_coding}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-Baoyu273511a}"

export MYSQL_PWD="$MYSQL_PASSWORD"

mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" <<SQL
INSERT INTO \`${DST_DB}\`.file_upload
  (file_md5, file_name, total_size, status, user_id, org_tag, is_public, created_at, merged_at)
SELECT
  src.file_md5,
  src.file_name,
  src.total_size,
  src.status,
  src.user_id,
  src.org_tag,
  src.is_public,
  src.created_at,
  src.merged_at
FROM \`${SRC_DB}\`.file_upload src
LEFT JOIN \`${DST_DB}\`.file_upload dst
  ON CONVERT(dst.file_md5 USING utf8mb4) COLLATE utf8mb4_unicode_ci = src.file_md5 COLLATE utf8mb4_unicode_ci
WHERE dst.file_md5 IS NULL
  AND src.file_name NOT LIKE 'sxh-article-%';
SQL

echo "Legacy file uploads synced from ${SRC_DB}.file_upload to ${DST_DB}.file_upload"
