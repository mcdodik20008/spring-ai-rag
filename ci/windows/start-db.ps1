# запуск базы PostgreSQL (pgvector) из подкаталога zuni/init

$ErrorActionPreference = "Stop"

Write-Host "🧠 Запуск PostgreSQL (pgvector) из zuni/init..." -ForegroundColor Cyan

# Проверка наличия Docker
if (-not (Get-Command docker -ErrorAction SilentlyContinue))
{
    Write-Host "❌ Docker не найден. Установи Docker Desktop." -ForegroundColor Red
    exit 1
}

# Проверка, что Docker работает
try
{
    docker info | Out-Null
}
catch
{
    Write-Host "❌ Docker демон не запущен. Запусти Docker Desktop." -ForegroundColor Red
    exit 1
}

# Переход в нужную директорию
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path "$scriptDir\.."
$initPath = Join-Path $projectRoot "zuni\init"

Set-Location $initPath

Write-Host "📦 Запускаем docker-compose..."
docker-compose up -d

Write-Host "✅ PostgreSQL (pgvector) запущен!" -ForegroundColor Green
