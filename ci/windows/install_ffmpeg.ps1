$ErrorActionPreference = "Stop"

Write-Host "🔍 Проверка ffmpeg..." -ForegroundColor Cyan

if (-not (Get-Command ffmpeg -ErrorAction SilentlyContinue)) {
    Write-Host "📦 Устанавливаем ffmpeg через Chocolatey..."
    if (-not (Get-Command choco -ErrorAction SilentlyContinue)) {
        Write-Host "❌ Chocolatey не найден. Установи его: https://chocolatey.org/install" -ForegroundColor Red
        exit 1
    }
    choco install ffmpeg -y
} else {
    Write-Host "✅ ffmpeg уже установлен: $(ffmpeg -version | Select-String -Pattern '^ffmpeg')"
}
