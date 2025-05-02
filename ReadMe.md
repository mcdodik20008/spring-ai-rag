# Сервис инжеста документов для Spring AI RAG

> **Цель проекта** — принимать Markdown‑ или обычный текст, разбивать его на логически цельные чанки и сохранять их в векторном хранилище для последующего Retrieval‑Augmented Generation (RAG).

---

## 1. Как это работает

```mermaid
graph LR
A[POST /api/docs/ingest] --> B[IngestController]
B --> C[RagService]
C --> D[MarkdownDocumentReader\\nили TextReader]
D --> E[TokenTextSplitter (300 токенов)]
E --> F[VectorStore.write]
```

1. **Клиент** отправляет сырой Markdown на `POST /api/docs/ingest` (заголовок `Content‑Type: text/plain`).
2. **IngestController** передаёт тело запроса в **RagService**.
3. **RagService** оборачивает текст в `ByteArrayResource` и передаёт в `MarkdownDocumentReader`.
4. **Reader** превращает Markdown в `List<Document>`.
5. **TokenTextSplitter** делит документы на чанки ≤ 300 токенов.
6. **VectorStore** сохраняет эмбеддинги (по умолчанию — память, но можно заменить на PGVector, Pinecone, Weaviate и др.).

---

## 2. Технологический стек

| Слой              | Библиотека / версия                 | Примечание                                 |
|-------------------|--------------------------------------|--------------------------------------------|
| Язык              | **Kotlin 2.0.20**                   | JDK 21+                                    |
| DI / Web          | **Spring Boot 3.2.x**               | Spring MVC                                 |
| Интеграция с ИИ   | **Spring AI 1.0.0‑M8**              | Markdown reader, VectorStore API           |
| Корутины          | **kotlinx‑coroutines‑core 1.9.0**   | обязательны для `suspend`‑эндпоинтов       |
| Сборка            | **Gradle Kotlin DSL**               | wrapper лежит в репозитории                |

> **Почему эти версии?** 1.9.x — последняя ветка coroutines, совместимая с Kotlin 2.0.x. При апгрейде до Spring Boot 3.3 планируется переход на Kotlin 2.1 + coroutines 1.10.

---

## 3. Начало работы

### 3.1 Предварительные требования

* **JDK 21+**
* **Gradle Wrapper** — локальная установка Gradle не нужна
* Переменные окружения:
  ```bash
  export OPENAI_API_KEY="sk‑…"      # если используете эмбеддинги OpenAI
  export VECTORSTORE_URI="…"        # например: jdbc:postgresql://… либо pinecone://…
  ```

### 3.2 Сборка и запуск

```bash
./gradlew clean build
./gradlew bootRun          # или: java -jar build/libs/ingest-0.0.1-SNAPSHOT.jar
```

Сервис будет доступен по адресу **http://localhost:8080**.

---

## 4. API

### 4.1 Инжест документа

`POST /api/docs/ingest`

| Параметр | Тип      | Обязательно | Описание                        |
|----------|----------|-------------|---------------------------------|
| Тело     | `String` | ✓           | Сырой Markdown или plain text   |

**Заголовки запроса**

```
Content-Type: text/plain; charset=utf-8
```

**Пример запроса cURL**

```bash
curl -X POST http://localhost:8080/api/docs/ingest \
     -H "Content-Type: text/plain; charset=utf-8" \
     --data-binary @docs/intro.md
```

**Ответ**

```
HTTP/1.1 202 Accepted
Location: /api/operations/{id}
```

Сейчас эндпоинт возвращает `202 Accepted`; получение статуса по пути `/api/operations/{id}` **пока не реализовано**.

---

## 5. Конфигурация

| Свойство                         | Значение по умолчанию | Назначение                                         |
|----------------------------------|-----------------------|----------------------------------------------------|
| `app.ingest.chunk-size`          | `300`                 | максимальное число токенов в чанке                 |
| `spring.ai.vectorstore.type`     | `memory`              | `pgvector`, `pinecone`, `weaviate`, `redis` и др.  |
| `spring.ai.openai.api-key`       | —                     | ключ API провайдера эмбеддингов (OpenAI)           |

Переопределять значения можно через `application.yaml` или переменные окружения.

---

## 6. Как расширить сервис

1. **Загрузка файла** — добавить эндпоинт `multipart/form-data` с `MultipartFile`, затем `MarkdownDocumentReader(file.inputStream)`.
2. **Автодетект формата** — анализировать `Content-Type` и выбирать `MarkdownDocumentReader` или `TextReader`.
3. **Асинхронный пайплайн** — помещать задачи в очередь (`TaskExecutor` / RabbitMQ) и сразу возвращать ID операции.
4. **Метаданные** — сохранять сведения о источнике, имени файла и времени инжеста в каждом `Document`.
5. **Тестирование** — примеры в `src/test` используют Testcontainers + pgvector.

---

## 7. Диагностика

| Симптом                                                  | Решение                                                   |
|----------------------------------------------------------|-----------------------------------------------------------|
| `ClassNotFoundException: kotlinx.coroutines.CoroutineScope` | Добавить зависимость `kotlinx-coroutines-core:1.9.x`      |
| `FileNotFoundException … class path resource […]`        | Передавать текст через `ByteArrayResource`, а не путь     |
| `415 Unsupported Media Type`                             | Установить `Content-Type: text/plain`                     |

---

## 8. Дорожная карта

* [ ] **API статуса операции** — polling + SSE прогресс
* [ ] **Переход на PGVector** по умолчанию (docker‑compose)
* [ ] **Бенчмарки производительности** (JMH) на чанках 256/512
* [ ] **Трейсинг OpenTelemetry**
* [ ] **Docker‑образ** — ghcr.io/your‑org/ingest‑service

---

## 9. Лицензия

MIT — подробности в файле `LICENSE`.

---

## 10. Мейнтейнеры

| Роль      | Имя / ник   | Контакты            |
|-----------|-------------|---------------------|
| Lead Dev  | @mcdodik    | you@example.com     |
| AI Infra  | @vector‑pal | vector@example.com  |

Открываем issues и PR — рады конструктивному фидбеку и автоматизации всего, что автоматизируется.

