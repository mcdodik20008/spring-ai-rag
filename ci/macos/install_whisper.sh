#!/usr/bin/env bash

set -euo pipefail

WHISPER_REPO="https://github.com/ggerganov/whisper.cpp.git"
WHISPER_DIR="whisper.cpp"
MODEL_NAME="medium"
MODEL_FILE="ggml-${MODEL_NAME}.bin"
MODELS_DIR="$WHISPER_DIR/models"

echo "🧠 Whisper.cpp setup script (macOS)"
echo "===================================="

install_ffmpeg() {
  echo "🔍 Проверка ffmpeg..."
  if ! command -v ffmpeg &> /dev/null; then
    echo "📦 Устанавливаем ffmpeg через Homebrew..."
    if ! command -v brew &> /dev/null; then
      echo "❌ Homebrew не найден. Установи его вручную: https://brew.sh/"
      exit 1
    fi
    brew install ffmpeg
  else
    echo "✅ ffmpeg уже установлен"
  fi
}

clone_whisper() {
  if [[ ! -d "$WHISPER_DIR" ]]; then
    echo "📥 Клонируем whisper.cpp..."
    git clone "$WHISPER_REPO"
  else
    echo "✅ whisper.cpp уже клонирован"
  fi
}

build_whisper() {
  echo "🛠 Сборка whisper.cpp..."
  make -C "$WHISPER_DIR"
}

download_model() {
  if [[ ! -f "$MODELS_DIR/$MODEL_FILE" ]]; then
    echo "⬇️ Скачиваем модель: $MODEL_NAME"
    "$WHISPER_DIR/models/download-ggml-model.sh" "$MODEL_NAME"
  else
    echo "✅ Модель уже загружена: $MODEL_FILE"
  fi
}

main() {
  install_ffmpeg
  clone_whisper
  build_whisper
  download_model
  echo "🎉 Whisper.cpp установлен успешно!"
}

main
