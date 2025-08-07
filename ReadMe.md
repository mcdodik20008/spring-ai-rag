# Spring AI RAG

> **R**etrieval‑**A**ugmented **G**eneration на базе Spring Boot 3.4 и Kotlin 1.9. Проект превращает локальную LLM (
> через **Ollama**) и векторное хранилище **pgvector** в готовый REST‑сервис для поиска по вашим документам и генерации
> ответов.

---

## 🚀 Ключевые возможности

|  Модуль             |  Что делает                                                                                                         |  Где смотреть                                     |
|---------------------|---------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|
| Ingest pipeline     | Разбирает PDF / Markdown / другое через Apache Tika, чистит текст, режет на чанки, отправляет в векторное хранилище | `utils.document.*`, `IngestController`            |
| Vector Store        | Custom adapter поверх **PostgreSQL + pgvector** <br/>— свой `PgVectorStoreImpl`, мапперы MyBatis, K‑NN по косинусу  | `db.*`                                            |
| Embedding           | Локальная модель `nomic‑embed‑text` из **Ollama**                                                                   | `application.yaml` → `spring.ai.ollama.embedding` |
| LLM (чат)           | По‑умолчанию `owl/t‑lite` (Ollama) + пример для **OpenRouter**                                                      | `application.yaml`, `AskController`               |
| RAG‑сервис          | 1. ищет релевантные чанки → 2. формирует промпт → 3. вызывает LLM → 4. возвращает ответ                             | `RagService`                                      |
| REST API            | `/api/ingest` и `/api/ask`                                                                                          | `controller.*`                                    |
| Докеризация         | `docker-compose.yaml` поднимает pgvector и Ollama с кешем моделей                                                   | корень проекта                                    |
| Kotlin / Gradle KTS | Все конфиги декларативно, Java 17 toolchain                                                                         | `build.gradle.kts`                                |

> **Zero‑Cloud**: для работы не нужен внешний интернет — модели и БД крутятся локально.

---

## ⚡️ Быстрый старт

```bash
# 1. Клонируем проект
$ git clone https://github.com/mcdodik20008/spring-ai-rag.git
$ cd spring-ai-rag

# 2. Поднимаем инфраструктуру (PostgreSQL + pgvector, Ollama)
$ docker compose up -d
# первый запуск докачает модели Ollama и создаст БД ragdb

# 3. (необязательно) задаём переменные окружения
$ export OPENROUTER_API_KEY=<your-key>
# или правим src/main/resources/application.yaml

# 4. Запускаем сервис
$ ./gradlew bootRun
# либо пакетируем jar и запускаем как systemd/Docker
```

После старта приложение слушает **`http://localhost:8080`**.

---

## 🛰 API Reference

### 1. POST `/api/ingest/pdf`

Загрузить PDF одним multipart‑запросом.

|  Параметр           | Тип    | Описание                                          |
|---------------------|--------|---------------------------------------------------|
| `file`              | `file` | PDF‑файл                                          |
| `skipPages`         | int    | Сколько первых страниц пропустить                 |
| `throwPagesFromEnd` | int    | Сколько последних страниц отбросить               |
| `headerFooterLines` | int    | Сколько верхних/нижних строк считать колонтитулом |
| `repeatThreshold`   | double | Порог для фильтра дубликатов строк                |

**Пример (cURL)**

```bash
curl -F "file=@spec.pdf" \
     -F skipPages=0 \
     -F throwPagesFromEnd=0 \
     -F headerFooterLines=2 \
     -F repeatThreshold=0.9 \
     http://localhost:8080/api/ingest/pdf
```

### 2. POST `/api/ask`

```json
{
  "question": "Как настроить pgvector в PostgreSQL?"
}
```

Ответ:

```json
{
  "answer": "…"
}
```

### 3. GET `/api/ask?quest=…`

Упрощённый вызов LLM без RAG через настроенный чат‑клиент (используется для отладки).

---

## 🛠 Как это работает

```mermaid
flowchart TD
    A[Документ (PDF / MD / DOCX / video)] --> B[DocumentWorker]
    B -->|Text & metadata| C[Chunker]
    C -->|Чанк + embedding| D[PgVector (pgvector)]
    E[Вопрос] --> F[Ollama Embedding]
    F -->|query‑vector| D
    D -->|K наиболее похожих| G[PromptFormatter]
    G --> H[Ollama ChatLLM]
    H --> I[Ответ]
```

* Кастомный `PgVectorStoreImpl` хранит embedding‑вектор как `vector(768)` + метаданные.
* На поиск используется оператор `<=>`pgvector (cosine distance).
* Промпт собирается в `ContextMarkdownFormatter` и `PromptTemplate` — так вы легко меняете системный текст.

---

## 🔌 Конфигурация

Все параметры лежат в `src/main/resources/application.yaml`. Главные из них:

```yaml
a spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      embedding:
        model: nomic-embed-text
      chat:
        options:
          model: owl/t-lite
    vectorstore:
      pgvector:
        initialize-schema: false  # если TRUE — создаст таблицы сама
  datasource:
    url: jdbc:postgresql://localhost:5432/ragdb
    username: postgres
    password: postgres
```

> **Совет:** храните секреты (API‑ключи) в переменных окружения или vault, а не в YAML.

---

## 🧩 Расширение проекта

|  Хотите                         |  Действия                                                                          |
|---------------------------------|------------------------------------------------------------------------------------|
| Другой источник (HTML, Git, S3) | Напишите свой `DocumentWorker` и зарегистрируйте как Spring‑bean                   |
| Облачный LLM                    | Подключите Spring‑AI starter нужного провайдера и переключите `chat.options.model` |
| Иная СУБД                       | Имплементируйте `CustomVectorStore` через интерфейс `VectorStore`                  |
| Фронтенд чат‑бот                | Любой SPA ↑ REST API или через WebSocket UIs ознакомьтесь с Spring AI Streaming    |

---

## 🗺 Roadmap / TODO

* [ ] Автоматическая генерация TL;DR summary для каждого документа
* [ ] Вынести промпт в БД
* [ ] Автоматическая генерация промпта для предметной области
* [ ] Семантический чанкинг
* [ ] Query Rewriting с LLM
* [ ] Гибридный поиск (BM25 + Embeddings)
* [ ] Reranking (переранжирование)
* [ ] LLM-based Relevance Scoring
* [ ] Динамический порог
* [ ] Удаление дубликатов / кластеризация

PR и идеи приветствуются!⭐️

---

## 📜 Лицензия

MIT — делайте, что хотите, но не забудьте про звёздочку 😊
