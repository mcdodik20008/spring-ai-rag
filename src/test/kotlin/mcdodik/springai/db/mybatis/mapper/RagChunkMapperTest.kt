package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.config.testcontainer.AbstractPgIT
import mcdodik.springai.db.entity.rag.DocumentInfo
import mcdodik.springai.db.entity.rag.RagChunkEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import java.util.UUID

@MybatisTest
class RagChunkMapperTest
    @Autowired
    constructor(
        private val mapper: RagChunkMapper,
        private val docMapper: DocumentInfoMapper,
        private val jdbc: JdbcTemplate,
    ) : AbstractPgIT() {
        private val now = LocalDateTime.of(2025, 8, 14, 12, 0, 0)

        private var embDim: Int = -1
        private val defaultDim = 768

        @BeforeEach
        fun setup() {
            if (embDim <= 0) embDim = queryEmbeddingDim() ?: defaultDim
            // порядок очистки важен: сначала дочерняя, затем родительская
            jdbc.execute("DELETE FROM rag_chunks")
            jdbc.execute("DELETE FROM document_info")
        }

        @Test
        fun `insert persists row`() {
            // подготовим родительский документ под (a.pdf, pdf, h1)
            insertDoc(uuid(100), "a.pdf", "pdf", "h1", chunkCount = 1)

            val e =
                chunk(
                    id = uuid(1),
                    content = "hello world",
                    emb = basis(0),
                    type = "text",
                    source = "test",
                    idx = 0,
                    file = "a.pdf",
                    ext = "pdf",
                    hash = "h1",
                )

            mapper.insert(e)

            val cnt = jdbc.queryForObject("SELECT COUNT(*) FROM rag_chunks") { rs, _ -> rs.getInt(1) }!!
            assertThat(cnt).isEqualTo(1)
        }

        @Test
        fun `search returns most similar first`() {
            insertDoc(uuid(101), "a.pdf", "pdf", "h1", 1)
            insertDoc(uuid(102), "b.pdf", "pdf", "h2", 1)

            val a = chunk(uuid(1), "A", basis(0), "text", "s", 0, "a.pdf", "pdf", "h1")
            val b = chunk(uuid(2), "B", nearBasis(0), "text", "s", 1, "b.pdf", "pdf", "h2")
            listOf(a, b).forEach(mapper::insert)

            val q = basis(0)

            val res =
                mapper.searchByEmbeddingFiltered(
                    embedding = q,
                    similarityThreshold = 1.0,
                    topK = 10,
                    filterClause = null,
                )

            assertThat(res.map { it.id }).containsExactly(a.id, b.id)
        }

        @Test
        fun `similarityThreshold filters out low-similarity`() {
            insertDoc(uuid(101), "a.pdf", "pdf", "h1", 1)
            insertDoc(uuid(102), "b.pdf", "pdf", "h2", 1)
            insertDoc(uuid(103), "c.pdf", "pdf", "h3", 1)

            val a = chunk(uuid(1), "A", basis(0), "text", "s", 0, "a.pdf", "pdf", "h1")
            val b = chunk(uuid(2), "B", nearBasis(0), "text", "s", 1, "b.pdf", "pdf", "h2")
            val c = chunk(uuid(3), "C", basis(1), "text", "s", 2, "c.pdf", "pdf", "h3")
            listOf(a, b, c).forEach(mapper::insert)

            val q = basis(0)

            val res =
                mapper.searchByEmbeddingFiltered(
                    embedding = q,
                    similarityThreshold = 0.0,
                    topK = 10,
                    filterClause = null,
                )

            assertThat(res.map { it.id }).containsExactly(a.id)
        }

        @Test
        fun `topK limits results`() {
            insertDoc(uuid(101), "a.pdf", "pdf", "h1", 1)
            insertDoc(uuid(102), "b.pdf", "pdf", "h2", 1)
            insertDoc(uuid(103), "c.pdf", "pdf", "h3", 1)

            val a = chunk(uuid(1), "A", basis(0), "text", "s", 0, "a.pdf", "pdf", "h1")
            val b = chunk(uuid(2), "B", nearBasis(0), "text", "s", 1, "b.pdf", "pdf", "h2")
            val c = chunk(uuid(3), "C", basis(1), "text", "s", 2, "c.pdf", "pdf", "h3")
            listOf(a, b, c).forEach(mapper::insert)

            val q = basis(0)

            val res =
                mapper.searchByEmbeddingFiltered(
                    embedding = q,
                    similarityThreshold = 1.0,
                    topK = 1,
                    filterClause = null,
                )

            assertThat(res).hasSize(1)
            assertThat(res.map { it.id }).containsExactly(a.id)
        }

        @Test
        fun `filterClause narrows results`() {
            insertDoc(uuid(101), "a.pdf", "pdf", "h1", 1)
            insertDoc(uuid(102), "b.pdf", "pdf", "h2", 1)

            val a = chunk(uuid(1), "A", basis(0), "text", "SRC1", 0, "a.pdf", "pdf", "h1")
            val b = chunk(uuid(2), "B", basis(0), "text", "SRC2", 1, "b.pdf", "pdf", "h2")
            listOf(a, b).forEach(mapper::insert)

            val q = basis(0)

            val res =
                mapper.searchByEmbeddingFiltered(
                    embedding = q,
                    similarityThreshold = 1.0,
                    topK = 10,
                    filterClause = "source = 'SRC1'",
                )
            assertThat(res.map { it.id }).containsExactly(a.id)
        }

        @Test
        fun `empty result when nothing fits`() {
            insertDoc(uuid(101), "a.pdf", "pdf", "h1", 1)

            val a = chunk(uuid(1), "A", basis(1), "text", "SRC", 0, "a.pdf", "pdf", "h1")
            mapper.insert(a)

            val q = basis(0)

            val res =
                mapper.searchByEmbeddingFiltered(
                    embedding = q,
                    similarityThreshold = 0.9999,
                    topK = 10,
                    filterClause = null,
                )

            assertThat(res).isEmpty()
        }

        // ---------- helpers ----------

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

        private fun basis(i: Int): List<Float> {
            val n = requireNotNull(embDim.takeIf { it > 0 }) { "embDim not detected" }
            val arr = FloatArray(n) { 0f }
            arr[i % n] = 1f
            return arr.toList()
        }

        private fun nearBasis(i: Int): List<Float> {
            val n = requireNotNull(embDim.takeIf { it > 0 }) { "embDim not detected" }
            val arr = FloatArray(n) { 0f }
            val main = i % n
            val side = (i + 1) % n
            arr[main] = 0.9998f
            arr[side] = 0.01f
            return arr.toList()
        }

        private fun chunk(
            id: UUID,
            content: String,
            emb: List<Float>,
            type: String,
            source: String,
            idx: Int,
            file: String,
            ext: String,
            hash: String,
            created: LocalDateTime = now,
        ) = RagChunkEntity(
            id = id,
            content = content,
            embedding = emb,
            type = type,
            source = source,
            chunkIndex = idx,
            fileName = file,
            extension = ext,
            hash = hash,
            createdAt = created,
        )

        private fun insertDoc(
            id: UUID,
            file: String,
            ext: String,
            hash: String,
            chunkCount: Int,
            created: LocalDateTime = now,
            summary: String? = "test doc $file",
        ) {
            docMapper.insert(
                DocumentInfo(
                    id = id,
                    fileName = file,
                    extension = ext,
                    hash = hash,
                    chunkCount = chunkCount,
                    createdAt = created,
                    summary = summary,
                ),
            )
        }

        private fun uuid(n: Int): UUID = UUID.fromString("00000000-0000-0000-0000-%012d".format(n))
    }
