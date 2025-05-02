#!/usr/bin/env bash
set -euo pipefail

MODELS=("mistral" "nomic-embed-text")

echo "🧠 Установка Ollama..."

if ! command -v ollama &>/dev/null; then
  echo "📦 Устанавливаем Ollama..."
  curl -fsSL https://ollama.com/install.sh | sh
else
  echo "✅ Ollama уже установлен"
fi

echo "⬇️ Загружаем модели: ${MODELS[*]}"
for model in "${MODELS[@]}"; do
  ollama pull "$model"
done

echo "✅ Все готово. Ollama запущен и модели загружены."
