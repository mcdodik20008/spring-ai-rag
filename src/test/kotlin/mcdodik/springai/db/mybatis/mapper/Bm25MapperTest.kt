package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.db.entity.rag.Bm25Row
import mcdodik.springai.db.entity.rag.DocumentInfo
import mcdodik.springai.db.entity.rag.RagChunkEntity
import mcdodik.springai.db.testcontainer.AbstractPgIT
import org.junit.jupiter.api.BeforeEach
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@MybatisTest
class Bm25MapperTest
    @Autowired
    constructor(
        private val mapper: Bm25Mapper,
        private val ragChunkMapper: RagChunkMapper,
        private val documentInfoMapper: DocumentInfoMapper,
        private val jdbc: JdbcTemplate,
    ) : AbstractPgIT() {
        private var embDim: Int = -1
        private val defaultDim = 768

        @BeforeEach
        fun setup() {
            if (embDim <= 0) embDim = queryEmbeddingDim() ?: defaultDim
            // порядок очистки: сначала дети
            jdbc.execute("DELETE FROM rag_chunks")
            jdbc.execute("DELETE FROM document_info")
            // На случай отсутствующего расширения в миграциях
            jdbc.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm")
        }

        // ---------- tests ----------

        @Test
        fun `search orders by rank and applies limit`() {
            // документ и 3 чанка
            insertDoc(file = "a.pdf", ext = "pdf", hash = "h1", chunkCount = 3)

            val bothTerms =
                insertAndReturn(
                    id = uuid(101),
                    content = "Kotlin coroutines and Flow basics. Learn Kotlin Flow operators.",
                    file = "a.pdf",
                    ext = "pdf",
                    hash = "h1",
                    idx = 0,
                )
            val singleTerm =
                insertAndReturn(
                    id = uuid(102),
                    content = "Deep dive into Kotlin memory model and concurrency.",
                    file = "a.pdf",
                    ext = "pdf",
                    hash = "h1",
                    idx = 1,
                )
            val noTerms =
                insertAndReturn(
                    id = uuid(103),
                    content = "PostgreSQL full text search guide.",
                    file = "a.pdf",
                    ext = "pdf",
                    hash = "h1",
                    idx = 2,
                )

            ensureTsvPopulated()

            val res = mapper.search(q = "kotlin", topK = 2)

            assertEquals(2, res.size)
            assertEquals(bothTerms.id, res[0].id) // сравниваем уже строки
            assertEquals(singleTerm.id, res[1].id)
            assertTrue(res[0].score >= res[1].score)
            assertTrue(res.none { it.id == noTerms.id })
        }

        @Test
        fun `search is case-insensitive`() {
            insertDoc(file = "b.pdf", ext = "pdf", hash = "h2", chunkCount = 2)
            val k1 =
                insertAndReturn(
                    id = uuid(201),
                    content = "Kotlin Flow tips and tricks",
                    file = "b.pdf",
                    ext = "pdf",
                    hash = "h2",
                    idx = 0,
                )
            val k2 =
                insertAndReturn(
                    id = uuid(202),
                    content = "kotlin coroutine builders overview",
                    file = "b.pdf",
                    ext = "pdf",
                    hash = "h2",
                    idx = 1,
                )

            ensureTsvPopulated()

            val res = mapper.search(q = "KOTLIN", topK = 10)
            val ids = res.map { it.id }.toSet()
            assertTrue(k1.id in ids && k2.id in ids)
        }

        @Test
        fun `search returns empty when no hits`() {
            insertDoc(file = "c.pdf", ext = "pdf", hash = "h3", chunkCount = 1)
            insertAndReturn(
                id = uuid(301),
                content = "Only Java streams content here",
                file = "c.pdf",
                ext = "pdf",
                hash = "h3",
                idx = 0,
            )

            ensureTsvPopulated()

            val res = mapper.search(q = "rust wasm", topK = 5)
            assertTrue(res.isEmpty())
        }

        // ---------- helpers: фабрики и перегрузки ----------

        private fun uuid(n: Int): UUID = UUID.fromString("00000000-0000-0000-0000-%012d".format(n))

        /** Удобная перегрузка для простых кейсов. */
        private fun insertDoc(
            file: String,
            ext: String,
            hash: String,
            chunkCount: Int,
            createdAt: LocalDateTime = LocalDateTime.now(),
        ) = insertDoc(
            DocumentInfo(
                id = UUID.randomUUID(),
                fileName = file,
                extension = ext,
                hash = hash,
                chunkCount = chunkCount,
                createdAt = createdAt,
                summary = "TODO()",
            ),
        )

        private fun insertDoc(documentInfo: DocumentInfo) {
            documentInfoMapper.insert(documentInfo)
        }

        /** Полная перегрузка — вставка чанка с конструированием сущности. */
        private fun insertChunk(
            id: UUID,
            content: String,
            file: String,
            ext: String,
            hash: String,
            idx: Int,
            createdAt: LocalDateTime = LocalDateTime.now(),
        ) = insertChunk(
            RagChunkEntity(
                id = id,
                content = content,
                embedding = basis(0),
                type = "text",
                source = "test",
                chunkIndex = idx,
                fileName = file,
                extension = ext,
                hash = hash,
                createdAt = createdAt,
            ),
        )

        private fun insertChunk(ragChunk: RagChunkEntity) {
            ragChunkMapper.insert(ragChunk)
        }

        /** Возвращает ожидаемую строку результата BM25 для удобной проверки. */
        private fun insertAndReturn(
            id: UUID,
            content: String,
            file: String,
            ext: String,
            hash: String,
            idx: Int,
            createdAt: LocalDateTime = LocalDateTime.now(),
        ): Bm25Row {
            insertChunk(id, content, file, ext, hash, idx, createdAt)
            return Bm25Row(id = id.toString(), content = content, score = .0)
        }

        /** Если tsv не автогенерится — подзаполним. */
        private fun ensureTsvPopulated() {
            runCatching {
                jdbc.execute(
                    """
                    UPDATE rag_chunks
                      SET tsv = to_tsvector('simple', coalesce(content,''))
                    WHERE tsv IS NULL

                    """.trimIndent(),
                )
            }
            // если колонка generated/immutable — апдейт просто не нужен
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
