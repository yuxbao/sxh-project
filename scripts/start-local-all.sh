#!/usr/bin/env bash
# ！本地运行时候使用
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_PID=""
FRONTEND_PID=""

run_with_prefix() {
  local prefix="$1"
  shift

  (
    "$@" \
      > >(
        while IFS= read -r line; do
          printf '[%s] %s\n' "$prefix" "$line"
        done
      ) \
      2> >(
        while IFS= read -r line; do
          printf '[%s] %s\n' "$prefix" "$line" >&2
        done
      )
  )
}

cleanup() {
  local exit_code=$?
  trap - EXIT INT TERM

  if [ -n "$BACKEND_PID" ] && kill -0 "$BACKEND_PID" 2>/dev/null; then
    kill "$BACKEND_PID" 2>/dev/null || true
  fi

  if [ -n "$FRONTEND_PID" ] && kill -0 "$FRONTEND_PID" 2>/dev/null; then
    kill "$FRONTEND_PID" 2>/dev/null || true
  fi

  wait "$BACKEND_PID" 2>/dev/null || true
  wait "$FRONTEND_PID" 2>/dev/null || true

  exit "$exit_code"
}

trap cleanup EXIT INT TERM

"$ROOT_DIR/scripts/start-local-deps.sh"

echo "启动后端和前端..."
echo "前端地址: http://localhost:9527"
echo "后端地址: http://localhost:8081/api/v1"
echo "按 Ctrl-C 可停止当前终端中的前后端进程"

run_with_prefix backend env SPRING_PROFILE= "$ROOT_DIR/scripts/start-backend.sh" &
BACKEND_PID=$!

run_with_prefix frontend "$ROOT_DIR/scripts/start-frontend.sh" &
FRONTEND_PID=$!

while true; do
  if ! kill -0 "$BACKEND_PID" 2>/dev/null; then
    wait "$BACKEND_PID"
    exit $?
  fi

  if ! kill -0 "$FRONTEND_PID" 2>/dev/null; then
    wait "$FRONTEND_PID"
    exit $?
  fi

  sleep 1
done
