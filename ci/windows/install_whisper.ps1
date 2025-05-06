# PowerShell-скрипт установки whisper.cpp под Windows
# Требует: git, cmake, MSVC или clang, ffmpeg (в PATH)

$ErrorActionPreference = "Stop"

$WhisperDir = "whisper.cpp"
$ModelName = "base"
$ModelFile = "ggml-$ModelName.bin"
$ModelPath = "$WhisperDir\models\$ModelFile"

Write-Host "`n🧠 Whisper.cpp Setup Script for Windows`n==============================" -ForegroundColor Cyan

function Ensure-Tool($name, $checkCmd, $installHint)
{
    Write-Host "🔍 Checking for $name..."
    if (-not (Get-Command $checkCmd -ErrorAction SilentlyContinue))
    {
        Write-Warning "$name not found. Please install it manually: $installHint"
        exit 1
    }
    else
    {
        Write-Host "✅ $name is available."
    }
}

# Проверки зависимостей
Ensure-Tool "Git" "git" "https://git-scm.com/downloads"
Ensure-Tool "CMake" "cmake" "https://cmake.org/download/"
Ensure-Tool "FFmpeg" "ffmpeg" "https://ffmpeg.org/download.html"

# Клонирование репозитория
if (-not (Test-Path $WhisperDir))
{
    Write-Host "📥 Cloning whisper.cpp..."
    git clone https://github.com/ggerganov/whisper.cpp
}
else
{
    Write-Host "✅ Repository already exists."
}

# Сборка whisper.cpp
Write-Host "🛠 Building whisper.cpp..."
$BuildDir = "$WhisperDir\build"
New-Item -ItemType Directory -Force -Path $BuildDir | Out-Null
Push-Location $BuildDir

cmake .. -DCMAKE_BUILD_TYPE=Release
cmake --build . --config Release

Pop-Location

# Скачивание модели
if (-not (Test-Path $ModelPath))
{
    Write-Host "⬇️ Downloading model $ModelName..."
    Push-Location "$WhisperDir\models"
    curl.exe -O "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/$ModelFile"
    Pop-Location
}
else
{
    Write-Host "✅ Model already exists: $ModelFile"
}

Write-Host "`n🎉 Setup complete! You can now run whisper.exe on your audio files." -ForegroundColor Green
