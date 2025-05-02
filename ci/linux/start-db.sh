#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
INIT_DIR="$PROJECT_ROOT/zuni/init"

echo "🧠 Запуск PostgreSQL (pgvector) из $INIT_DIR"

# Проверка Docker
if ! command -v docker &>/dev/null; then
  echo "❌ Docker не установлен. Установи Docker и повтори попытку."
  exit 1
fi

# Проверка, что Docker работает
if ! docker info &>/dev/null; then
  echo "❌ Docker демон не запущен. Запусти Docker Desktop или systemd службу."
  exit 1
fi

cd "$INIT_DIR"

echo "📦 Запускаем docker-compose..."
docker-compose up -d

echo "✅ PostgreSQL (pgvector) запущен!"
