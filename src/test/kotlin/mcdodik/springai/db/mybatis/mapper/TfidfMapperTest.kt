package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.config.testcontainer.AbstractPgIT
import mcdodik.springai.db.entity.rag.DocumentInfo
import mcdodik.springai.db.entity.rag.RagChunkEntity
import mcdodik.springai.scheduling.mapper.TfidfMapper
import mcdodik.springai.scheduling.model.ChunkTfidfUpdate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@MybatisTest
class TfidfMapperTest
    @Autowired
    constructor(
        private val tfidfMapper: TfidfMapper,
        private val ragChunkMapper: RagChunkMapper,
        private val documentInfoMapper: DocumentInfoMapper,
        private val jdbc: JdbcTemplate,
    ) : AbstractPgIT() {
        private var embDim: Int = -1
        private val defaultDim = 768

        @BeforeEach
        fun setup() {
            if (embDim <= 0) embDim = queryEmbeddingDim() ?: defaultDim
            // порядок: сначала дети
            // используем уже имеющийся TfidfMapper для upsert дубликатов => чистка через FK каскады в миграциях
            // если каскадов нет — ожидается, что миграции содержат TRUNCATE/DELETE в базовом тестовом слое
        }

        // --- tests ---

        @Test
        fun `selectChunksNeedingTfidf returns only chunks without fresh tfidf`() {
            insertDoc("a.pdf", "pdf", "h1", 3)

            // C1, C2: tfidf == NULL => должны попасть
            val c1 = insertChunk(uuid(101), "kotlin flow basics", "a.pdf", "pdf", "h1", 0)
            val c2 = insertChunk(uuid(102), "coroutines overview", "a.pdf", "pdf", "h1", 1)

            // C3: сразу сделаем свежий tfidf => не должен попасть
            val c3 = insertChunk(uuid(103), "postgres text search", "a.pdf", "pdf", "h1", 2)
            tfidfMapper.updateTfidf(
                ChunkTfidfUpdate(
                    id = c3.id,
                    tfidf = mapOf("postgres" to 1.0),
                    tfidfNorm = 1.0,
                ),
            )

            val rows = tfidfMapper.selectChunksNeedingTfidf(limit = 10, offset = 0)

            val gotIds = rows.map { it.id }.toSet()
            assertTrue(c1.id in gotIds && c2.id in gotIds)
            assertTrue(c3.id !in gotIds)
            // порядок по updated_at может различаться — не проверяем строгий order
        }

        @Test
        fun `updateTfidf updates once and becomes no-op if payload is identical`() {
            insertDoc("b.pdf", "pdf", "h2", 1)
            val c = insertChunk(uuid(201), "kotlin channels", "b.pdf", "pdf", "h2", 0)

            // до апдейта — попадает в needing
            val before = tfidfMapper.selectChunksNeedingTfidf(limit = 10, offset = 0).map { it.id }.toSet()
            assertTrue(c.id in before)

            // первый апдейт — 1 строка
            val upd =
                ChunkTfidfUpdate(
                    id = c.id,
                    tfidf = mapOf("kotlin" to 0.7, "channels" to 0.3),
                    tfidfNorm = 1.0,
                )
            val r1 = tfidfMapper.updateTfidf(upd)
            assertEquals(1, r1)

            // второй апдейт с тем же payload — 0 строк
            val r2 = tfidfMapper.updateTfidf(upd)
            assertEquals(0, r2)

            // после апдейта — больше не нужен пересчёт
            val after = tfidfMapper.selectChunksNeedingTfidf(limit = 10, offset = 0).map { it.id }.toSet()
            assertTrue(c.id !in after)
        }

        @Test
        fun `findCandidatesByAnyTerms returns chunks sharing keys, excludes self, respects limit`() {
            insertDoc("c.pdf", "pdf", "h3", 4)

            val q = insertChunk(uuid(301), "Q", "c.pdf", "pdf", "h3", 0)
            val a = insertChunk(uuid(302), "A", "c.pdf", "pdf", "h3", 1)
            val b = insertChunk(uuid(303), "B", "c.pdf", "pdf", "h3", 2)
            val c = insertChunk(uuid(304), "C", "c.pdf", "pdf", "h3", 3)

            // Заполним tfidf через ТУ ЖЕ updateTfidf
            tfidfMapper.updateTfidf(ChunkTfidfUpdate(q.id, mapOf("kotlin" to 0.8, "flow" to 0.2), tfidfNorm = 1.0))
            tfidfMapper.updateTfidf(ChunkTfidfUpdate(a.id, mapOf("kotlin" to 0.3), tfidfNorm = 1.0))
            tfidfMapper.updateTfidf(ChunkTfidfUpdate(b.id, mapOf("flow" to 0.9), tfidfNorm = 1.0))
            tfidfMapper.updateTfidf(ChunkTfidfUpdate(c.id, mapOf("postgres" to 1.0), tfidfNorm = 1.0))

            // terms = ["kotlin","flow"] — ждём A и B; self (Q) исключён, C не подходит
            val res = tfidfMapper.findCandidatesByAnyTerms(id = q.id, terms = listOf("kotlin", "flow"), limit = 10)
            val ids = res.map { it.id }.toSet()
            assertTrue(a.id in ids && b.id in ids)
            assertTrue(q.id !in ids && c.id !in ids)

            // Пустой список — пустой ответ
            val empty = tfidfMapper.findCandidatesByAnyTerms(id = q.id, terms = emptyList(), limit = 10)
            assertTrue(empty.isEmpty())

            // Лимит
            val limited = tfidfMapper.findCandidatesByAnyTerms(id = q.id, terms = listOf("kotlin", "flow"), limit = 1)
            assertEquals(1, limited.size)
        }

        @Test
        fun `upsertDuplicate inserts then updates`() {
            insertDoc("d.pdf", "pdf", "h4", 2)
            val keep1 = insertChunk(uuid(401), "KEEP1", "d.pdf", "pdf", "h4", 0)
            val dup = insertChunk(uuid(402), "DUP", "d.pdf", "pdf", "h4", 1)

            // insert
            val r1 = tfidfMapper.upsertDuplicate(dupId = dup.id, keepId = keep1.id, simScore = 0.88)
            assertEquals(1, r1)

            // update (сменим keep и sim) — тоже 1 строка
            val keep2 = insertChunk(uuid(403), "KEEP2", "d.pdf", "pdf", "h4", 2)
            val r2 = tfidfMapper.upsertDuplicate(dupId = dup.id, keepId = keep2.id, simScore = 0.91)
            assertEquals(1, r2)
        }

        // --- helpers (только готовые мапперы) ---

        private fun uuid(n: Int): UUID = UUID.fromString("00000000-0000-0000-0000-%012d".format(n))

        private fun insertDoc(
            file: String,
            ext: String,
            hash: String,
            chunkCount: Int,
        ) {
            documentInfoMapper.insert(
                DocumentInfo(
                    id = UUID.randomUUID(),
                    fileName = file,
                    extension = ext,
                    hash = hash,
                    chunkCount = chunkCount,
                    createdAt = LocalDateTime.now(),
                    summary = "TODO()",
                ),
            )
        }

        private fun insertChunk(
            id: UUID,
            text: String,
            file: String,
            ext: String,
            hash: String,
            idx: Int,
            createdAt: LocalDateTime = LocalDateTime.now(),
        ): RagChunkEntity {
            val entity =
                RagChunkEntity(
                    id = id,
                    content = text,
                    embedding = basis(0),
                    type = "text",
                    source = "test",
                    chunkIndex = idx,
                    fileName = file,
                    extension = ext,
                    hash = hash,
                    createdAt = createdAt,
                )
            ragChunkMapper.insert(entity)
            return entity
        }

        private fun basis(i: Int): List<Float> {
            val n = requireNotNull(embDim.takeIf { it > 0 }) { "embDim not detected" }
            val arr = FloatArray(n) { 0f }
            arr[i % n] = 1f
            return arr.toList()
        }

        /** определяем размерность vector(N) как atttypmod (без -4!) */
        private fun queryEmbeddingDim(): Int? =
            jdbc
                .query(
                    """
                    SELECT a.atttypmod AS dim
                    FROM pg_attribute a
                    JOIN pg_class c ON a.attrelid = c.oid
                    WHERE c.relname = 'rag_chunks'
                      AND a.attname = 'embedding'
                      AND a.attnum > 0
                      AND NOT a.attisdropped
                    """.trimIndent(),
                ) { rs, _ -> rs.getInt("dim") }
                .firstOrNull()
                ?.takeIf { it > 0 }
    }
