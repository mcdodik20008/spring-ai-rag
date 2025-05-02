#!/usr/bin/env bash
set -euo pipefail

echo "🔍 Проверка ffmpeg..."

if ! command -v ffmpeg &> /dev/null; then
  echo "📦 Устанавливаем ffmpeg через Homebrew..."
  if ! command -v brew &> /dev/null; then
    echo "❌ Homebrew не найден. Установи его с https://brew.sh/"
    exit 1
  fi
  brew install ffmpeg
else
  echo "✅ ffmpeg уже установлен: $(ffmpeg -version | head -n 1)"
fi
