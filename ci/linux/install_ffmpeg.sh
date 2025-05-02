#!/usr/bin/env bash
set -euo pipefail

echo "🔍 Проверка ffmpeg..."

if ! command -v ffmpeg &> /dev/null; then
  echo "📦 Устанавливаем ffmpeg через apt..."
  sudo apt update && sudo apt install -y ffmpeg
else
  echo "✅ ffmpeg уже установлен: $(ffmpeg -version | head -n 1)"
fi
