#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/logs/local"
PID_DIR="$ROOT_DIR/logs/pids"

MYSQL_PASSWORD="${MYSQL_PASSWORD:-Baoyu273511a}"
REDIS_PASSWORD="${REDIS_PASSWORD:-273511}"
MINIO_ROOT_USER="${MINIO_ROOT_USER:-minioadmin}"
MINIO_ROOT_PASSWORD="${MINIO_ROOT_PASSWORD:-minioadmin}"
MINIO_DATA_DIR="${MINIO_DATA_DIR:-$HOME/minio/data}"
MINIO_API_URL="${MINIO_API_URL:-http://localhost:9000}"
MINIO_CONSOLE_PORT="${MINIO_CONSOLE_PORT:-9001}"
MINIO_BUCKET="${MINIO_BUCKET:-uploads}"

KAFKA_HOME="${KAFKA_HOME:-$HOME/dev/env/kafka/kafka_2.13-3.9.0}"
KAFKA_CONFIG="${KAFKA_CONFIG:-$KAFKA_HOME/config/kraft/server.properties}"
KAFKA_CLUSTER_ID_FILE="$PID_DIR/kafka-cluster.id"

ES_HOME="${ES_HOME:-$HOME/dev/env/elasticsearch/elasticsearch-8.10.0}"
ES_PASSWORD="${ES_PASSWORD:-mRj-QzairyPuVMFaA4Q+}"
ES_URL="${ES_URL:-https://localhost:9200}"

mkdir -p "$LOG_DIR" "$PID_DIR" "$MINIO_DATA_DIR" "$LOG_DIR/redis-data"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "未找到命令: $1" >&2
    exit 1
  fi
}

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

start_mysql() {
  require_command mysql
  require_command mysqladmin

  if mysqladmin ping -h 127.0.0.1 -uroot "-p$MYSQL_PASSWORD" --silent >/dev/null 2>&1; then
    echo "MySQL 已在运行"
    return
  fi

  if command -v brew >/dev/null 2>&1; then
    if brew services list | grep -E '^mysql@8\.0\s' >/dev/null 2>&1; then
      echo "启动本机 MySQL 服务: mysql@8.0"
      brew services start mysql@8.0 >/dev/null
      echo "mysql@8.0" > "$PID_DIR/mysql.brew_service"
    elif brew services list | grep -E '^mysql\s' >/dev/null 2>&1; then
      echo "启动本机 MySQL 服务: mysql"
      brew services start mysql >/dev/null
      echo "mysql" > "$PID_DIR/mysql.brew_service"
    else
      echo "未检测到可启动的 Homebrew MySQL 服务，请先手动启动 MySQL" >&2
      exit 1
    fi
  else
    echo "MySQL 未运行，且未检测到 brew，无法自动启动" >&2
    exit 1
  fi

  retry_until "MySQL" 120 mysqladmin ping -h 127.0.0.1 -uroot "-p$MYSQL_PASSWORD" --silent
  mysql -h 127.0.0.1 -uroot "-p$MYSQL_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS sxh_rag DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
}

start_redis() {
  require_command redis-cli
  require_command redis-server

  if redis-cli -a "$REDIS_PASSWORD" -h 127.0.0.1 -p 6379 ping >/dev/null 2>&1; then
    echo "Redis 已在运行"
    return
  fi

  echo "启动本机 Redis..."
  redis-server \
    --port 6379 \
    --requirepass "$REDIS_PASSWORD" \
    --daemonize yes \
    --dir "$LOG_DIR/redis-data" \
    --logfile "$LOG_DIR/redis.log" \
    --pidfile "$PID_DIR/redis.pid"

  retry_until "Redis" 60 redis-cli -a "$REDIS_PASSWORD" -h 127.0.0.1 -p 6379 ping
}

start_minio() {
  require_command minio
  require_command curl
  require_command mc

  if curl -fsS "$MINIO_API_URL/minio/health/live" >/dev/null 2>&1; then
    echo "MinIO 已在运行"
  else
    echo "启动本机 MinIO..."
    minio server "$MINIO_DATA_DIR" --address ":9000" --console-address ":$MINIO_CONSOLE_PORT" \
      >"$LOG_DIR/minio.log" 2>&1 &
    echo $! > "$PID_DIR/minio.pid"
    retry_until "MinIO" 60 curl -fsS "$MINIO_API_URL/minio/health/live"
  fi

  mc alias set local "$MINIO_API_URL" "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD" >/dev/null
  mc mb --ignore-existing "local/$MINIO_BUCKET" >/dev/null
  echo "MinIO bucket 已就绪: $MINIO_BUCKET"
}

get_kafka_log_dir() {
  awk -F= '/^log\.dirs=/{print $2}' "$KAFKA_CONFIG" | tail -n 1
}

ensure_kafka_formatted() {
  local kafka_log_dir
  kafka_log_dir="$(get_kafka_log_dir)"

  if [ -z "$kafka_log_dir" ]; then
    echo "无法从 Kafka 配置中解析 log.dirs" >&2
    exit 1
  fi

  mkdir -p "$kafka_log_dir"

  if [ -f "$kafka_log_dir/meta.properties" ]; then
    return
  fi

  echo "首次初始化 Kafka KRaft 存储..."
  if [ -f "$KAFKA_CLUSTER_ID_FILE" ]; then
    local cluster_id
    cluster_id="$(cat "$KAFKA_CLUSTER_ID_FILE")"
    "$KAFKA_HOME/bin/kafka-storage.sh" format -t "$cluster_id" -c "$KAFKA_CONFIG" >/dev/null
  else
    local cluster_id
    cluster_id="$("$KAFKA_HOME/bin/kafka-storage.sh" random-uuid)"
    echo "$cluster_id" > "$KAFKA_CLUSTER_ID_FILE"
    "$KAFKA_HOME/bin/kafka-storage.sh" format -t "$cluster_id" -c "$KAFKA_CONFIG" >/dev/null
  fi
}

start_kafka() {
  require_command java

  if [ ! -x "$KAFKA_HOME/bin/kafka-server-start.sh" ]; then
    echo "Kafka 路径不存在或不可执行: $KAFKA_HOME" >&2
    exit 1
  fi

  if "$KAFKA_HOME/bin/kafka-topics.sh" --bootstrap-server localhost:9092 --list >/dev/null 2>&1; then
    echo "Kafka 已在运行"
  else
    ensure_kafka_formatted
    echo "启动本机 Kafka..."
    "$KAFKA_HOME/bin/kafka-server-start.sh" "$KAFKA_CONFIG" >"$LOG_DIR/kafka.log" 2>&1 &
    echo $! > "$PID_DIR/kafka.pid"
    retry_until "Kafka" 120 "$KAFKA_HOME/bin/kafka-topics.sh" --bootstrap-server localhost:9092 --list
  fi

  "$KAFKA_HOME/bin/kafka-topics.sh" --bootstrap-server localhost:9092 --create --if-not-exists --topic file-processing-topic1 --partitions 1 --replication-factor 1 >/dev/null
  "$KAFKA_HOME/bin/kafka-topics.sh" --bootstrap-server localhost:9092 --create --if-not-exists --topic file-processing-dlt --partitions 1 --replication-factor 1 >/dev/null
  echo "Kafka topics 已就绪"
}

start_es() {
  require_command curl

  if [ ! -x "$ES_HOME/bin/elasticsearch" ]; then
    echo "Elasticsearch 路径不存在或不可执行: $ES_HOME" >&2
    exit 1
  fi

  if curl -k -fsS -u "elastic:$ES_PASSWORD" "$ES_URL/_cluster/health" >/dev/null 2>&1; then
    echo "Elasticsearch 已在运行"
    return
  fi

  echo "启动本机 Elasticsearch..."
  "$ES_HOME/bin/elasticsearch" >"$LOG_DIR/elasticsearch.log" 2>&1 &
  echo $! > "$PID_DIR/elasticsearch.pid"
  retry_until "Elasticsearch" 180 curl -k -fsS -u "elastic:$ES_PASSWORD" "$ES_URL/_cluster/health"
}

start_mysql
mysql -h 127.0.0.1 -uroot "-p$MYSQL_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS sxh_rag DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
start_redis
start_minio
start_kafka
start_es

echo "本机依赖服务启动完成"
