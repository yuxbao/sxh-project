#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_DIR="$ROOT_DIR/logs/pids"
REDIS_PASSWORD="${REDIS_PASSWORD:-273511}"

stop_pid_file() {
  local name="$1"
  local file="$2"

  if [ ! -f "$file" ]; then
    return
  fi

  local pid
  pid="$(cat "$file")"

  if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
    echo "停止 $name (PID: $pid)"
    kill "$pid" 2>/dev/null || true
  fi

  rm -f "$file"
}

stop_pid_file "MinIO" "$PID_DIR/minio.pid"
stop_pid_file "Kafka" "$PID_DIR/kafka.pid"
stop_pid_file "Elasticsearch" "$PID_DIR/elasticsearch.pid"

if [ -f "$PID_DIR/redis.pid" ]; then
  if command -v redis-cli >/dev/null 2>&1; then
    redis-cli -a "$REDIS_PASSWORD" -h 127.0.0.1 -p 6379 shutdown >/dev/null 2>&1 || true
  fi
  rm -f "$PID_DIR/redis.pid"
fi

if [ -f "$PID_DIR/mysql.brew_service" ] && command -v brew >/dev/null 2>&1; then
  service_name="$(cat "$PID_DIR/mysql.brew_service")"
  if [ -n "$service_name" ]; then
    echo "停止 MySQL 服务: $service_name"
    brew services stop "$service_name" >/dev/null || true
  fi
  rm -f "$PID_DIR/mysql.brew_service"
fi

echo "本机依赖停止命令已执行"
