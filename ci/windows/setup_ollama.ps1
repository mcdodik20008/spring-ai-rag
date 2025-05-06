# Проверь, установлен ли Ollama
if (-not (Get-Command ollama -ErrorAction SilentlyContinue))
{
    Write-Warning "Ollama не найден. Установи вручную: https://ollama.com/download"
    exit 1
}

# Загрузка моделей
$models = @("mistral", "nomic-embed-text")

foreach ($model in $models)
{
    Write-Host "⬇️ Загружаем модель: $model"
    ollama pull $model
}

Write-Host "`n✅ Все готово. Модели доступны и Ollama работает."
