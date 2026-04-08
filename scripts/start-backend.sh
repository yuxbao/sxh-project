#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SPRING_PROFILE="${SPRING_PROFILE-docker}"

if ! command -v mvn >/dev/null 2>&1; then
  echo "未找到 mvn，请先安装 Maven" >&2
  exit 1
fi

cd "$ROOT_DIR"

if [ -n "$SPRING_PROFILE" ]; then
  exec mvn spring-boot:run "-Dspring-boot.run.profiles=$SPRING_PROFILE"
else
  exec mvn spring-boot:run
fi
