#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docs/docker-compose.yaml"

MYSQL_PASSWORD="SxhRag2025"
ELASTIC_PASSWORD="SxhRag2025"
MINIO_USER="admin"
MINIO_PASSWORD="SxhRag2025"
MINIO_API="http://localhost:19000"
MINIO_BUCKET="uploads"

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD=(docker compose -f "$COMPOSE_FILE")
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose -f "$COMPOSE_FILE")
else
  echo "未找到 docker compose 或 docker-compose" >&2
  exit 1
fi

retry_until() {
  local name="$1"
  local timeout_seconds="$2"
  shift 2

  local start_time
  start_time=$(date +%s)

  until "$@" >/dev/null 2>&1; do
    local now
    now=$(date +%s)
    if [ $((now - start_time)) -ge "$timeout_seconds" ]; then
      echo "等待 $name 超时" >&2
      return 1
    fi
    sleep 2
  done

  echo "$name 已就绪"
}

echo "启动 Docker 依赖服务..."
"${COMPOSE_CMD[@]}" up -d

retry_until "MySQL" 120 docker exec mysql mysqladmin ping -uroot "-p$MYSQL_PASSWORD" --silent
retry_until "Redis" 60 docker exec redis redis-cli "-a$MYSQL_PASSWORD" ping
retry_until "MinIO" 60 curl -fsS "$MINIO_API/minio/health/live"
retry_until "Kafka" 120 docker exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
retry_until "Elasticsearch" 180 curl -fsS -u "elastic:$ELASTIC_PASSWORD" http://localhost:9200/_cluster/health

echo "初始化 MySQL 数据库..."
docker exec mysql mysql -uroot "-p$MYSQL_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS sxh_rag DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo "初始化 MinIO bucket..."
if docker exec minio sh -lc "command -v mc >/dev/null 2>&1"; then
  docker exec minio sh -lc "mc alias set local $MINIO_API $MINIO_USER $MINIO_PASSWORD >/dev/null && mc mb --ignore-existing local/$MINIO_BUCKET >/dev/null"
else
  docker run --rm minio/mc sh -c "mc alias set local http://host.docker.internal:19000 $MINIO_USER $MINIO_PASSWORD >/dev/null && mc mb --ignore-existing local/$MINIO_BUCKET >/dev/null"
fi

echo "依赖服务启动完成"
