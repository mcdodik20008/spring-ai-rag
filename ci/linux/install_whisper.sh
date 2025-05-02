#!/usr/bin/env bash

set -euo pipefail

WHISPER_REPO="https://github.com/ggerganov/whisper.cpp.git"
WHISPER_DIR="whisper.cpp"
MODEL_NAME="medium"
MODEL_FILE="ggml-${MODEL_NAME}.bin"
MODELS_DIR="$WHISPER_DIR/models"

echo "🧠 Whisper.cpp setup script (Linux)"
echo "==================================="

install_ffmpeg() {
  echo "🔍 Проверка наличия ffmpeg..."
  if ! command -v ffmpeg &> /dev/null; then
    echo "📦 ffmpeg не найден, устанавливаю через apt..."
    sudo apt update
    sudo apt install -y ffmpeg
  else
    echo "✅ ffmpeg уже установлен"
  fi
}

clone_whisper() {
  if [[ ! -d "$WHISPER_DIR" ]]; then
    echo "📥 Клонируем whisper.cpp из $WHISPER_REPO..."
    git clone "$WHISPER_REPO"
  else
    echo "✅ whisper.cpp уже клонирован"
  fi
}

build_whisper() {
  echo "🛠 Собираем whisper.cpp..."
  make -C "$WHISPER_DIR"
}

download_model() {
  if [[ ! -f "$MODELS_DIR/$MODEL_FILE" ]]; then
    echo "⬇️ Скачиваем модель $MODEL_NAME..."
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
  echo "🎉 Установка завершена успешно!"
}

main
