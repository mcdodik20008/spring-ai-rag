package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.config.testcontainer.AbstractPgIT
import mcdodik.springai.db.entity.prompt.ChunkingPromptTemplate
import org.junit.jupiter.api.BeforeEach
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@MybatisTest
class ChunkingPromptTemplateMapperTest
    @Autowired
    constructor(
        private val mapper: ChunkingPromptTemplateMapper,
        private val jdbc: JdbcTemplate,
    ) : AbstractPgIT() {
        @BeforeEach
        fun setUp() {
            // Чистим таблицу и гарантируем, что нужные экстеншены доступны
            jdbc.execute("CREATE EXTENSION IF NOT EXISTS vector")
            jdbc.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm")
            jdbc.execute("DELETE FROM chunking_prompt_templates")
        }

        // ---------- tests ----------

        @Test
        fun `insert persists row and findAll returns it`() {
            val now = LocalDateTime.of(2025, 8, 17, 10, 0, 0)

            val t1 =
                insertTemplate(
                    id = uuid(1),
                    domain = "it",
                    userDesc = "desc1",
                    prompt = "prompt1",
                    createdAt = now.minusMinutes(1),
                    topic = "kotlin",
                    topicEmbedding = null,
                )

            val all = mapper.findAll()
            assertEquals(1, all.size)
            assertEquals(t1.id, all[0].id)
            assertEquals(t1.domainName, all[0].domainName)
            assertEquals(t1.userDescription, all[0].userDescription)
            assertEquals(t1.generatedPrompt, all[0].generatedPrompt)
            assertEquals(t1.topic, all[0].topic)
            assertNull(all[0].topicEmbedding)
        }

        @Test
        fun `findAll ordered by created_at desc`() {
            val now = LocalDateTime.of(2025, 8, 17, 10, 0, 0)

            val tOld = insertTemplate(uuid(10), "it", "old", "p-old", now.minusHours(2), "a", null)
            val tMid = insertTemplate(uuid(11), "it", "mid", "p-mid", now.minusHours(1), "b", null)
            val tNew = insertTemplate(uuid(12), "it", "new", "p-new", now, "c", null)

            val all = mapper.findAll()
            assertEquals(listOf(tNew.id, tMid.id, tOld.id), all.map { it.id })
        }

        @Test
        fun `searchByTopicEmbedding returns nearest k and ignores NULL embeddings`() {
            val dim = topicEmbeddingDims()
            val now = LocalDateTime.of(2025, 8, 17, 10, 0, 0)

            // NULL-embedding — должен быть проигнорирован
            insertTemplate(uuid(20), "it", "null-emb", "p0", now, "null", null)

            // e0 — самый близкий к запросу e0
            val e0 =
                insertTemplate(
                    uuid(21),
                    "it",
                    "e0",
                    "p1",
                    now,
                    "e0",
                    basis(dim, 0),
                )

            // около e0
            val near =
                insertTemplate(
                    uuid(22),
                    "it",
                    "near",
                    "p2",
                    now,
                    "near-e0",
                    nearE0(dim),
                )

            // e1 — далеко от e0 при cosine distance
            val e1 =
                insertTemplate(
                    uuid(23),
                    "it",
                    "e1",
                    "p3",
                    now,
                    "e1",
                    basis(dim, 1.coerceAtMost(dim - 1)),
                )

            val query = basis(dim, 0)
            val k = 2

            val result = mapper.searchByTopicEmbedding(query, k)

            // Должны вернуться только 2 ближайших: e0, near (в таком порядке)
            assertEquals(2, result.size)
            assertEquals(listOf(e0.id, near.id), result.map { it.id })
            // Проверим, что в результат не попал NULL-embedding и дальний e1
            assertTrue(result.none { it.id == uuid(20) })
            assertTrue(result.none { it.id == e1.id })
        }

        @Test
        fun `searchByTopicEmbedding applies limit k`() {
            val dim = topicEmbeddingDims()
            val now = LocalDateTime.of(2025, 8, 17, 10, 0, 0)

            insertTemplate(uuid(30), "it", "a", "p", now, "t", basis(dim, 0))
            insertTemplate(uuid(31), "it", "b", "p", now, "t", basis(dim, 0))
            insertTemplate(uuid(32), "it", "c", "p", now, "t", basis(dim, 0))

            val res = mapper.searchByTopicEmbedding(basis(dim, 0), 2)
            assertEquals(2, res.size)
        }

        @Test
        fun `searchByTopicLike matches by ILIKE and trigram similarity`() {
            val now = LocalDateTime.of(2025, 8, 17, 10, 0, 0)

            val k1 = insertTemplate(uuid(40), "it", "d1", "p", now, "Kotlin Coroutines", null)
            val k2 = insertTemplate(uuid(41), "it", "d2", "p", now, "Kotlin Flow tips", null)
            val pg = insertTemplate(uuid(42), "it", "d3", "p", now, "PostgreSQL indexing", null)

            // Подстрочный матч (ILIKE)
            val bySubstr = mapper.searchByTopicLike("kotlin", 5)
            assertTrue(bySubstr.map { it.id }.containsAll(listOf(k1.id, k2.id)))
            assertTrue(bySubstr.none { it.id == pg.id })

            // Опечатка — ловим по similarity() > 0.3
            val byTypos = mapper.searchByTopicLike("kotlib flof", 5)
            assertTrue(byTypos.map { it.id }.contains(k1.id) || byTypos.map { it.id }.contains(k2.id))
        }

        // ---------- helpers ----------

        private fun uuid(n: Int): UUID = UUID.fromString("00000000-0000-0000-0000-%012d".format(n))

        /** Вытягиваем реальную размерность vector(N) у столбца topic_embedding. */
        private fun topicEmbeddingDims(): Int {
            val t: String =
                jdbc.queryForObject(
                    """
                    SELECT format_type(a.atttypid, a.atttypmod)
                    FROM pg_attribute a
                    WHERE a.attrelid = 'chunking_prompt_templates'::regclass
                      AND a.attname = 'topic_embedding'
                    """.trimIndent(),
                    String::class.java,
                ) ?: "vector(3)"
            val m = Regex("""vector\((\d+)\)""").find(t)
            return m?.groupValues?.get(1)?.toInt() ?: 3
        }

        /** Единичный вектор e[i] указанной размерности. */
        private fun basis(
            dim: Int,
            i: Int,
        ): List<Float> = FloatArray(dim) { idx -> if (idx == i) 1f else 0f }.toList()

        /** Вектор "рядом" с e0: 1.0 по оси 0 и небольшой вклад по оси 1. */
        private fun nearE0(dim: Int): List<Float> {
            val a = FloatArray(dim) { 0f }
            a[0] = 1f
            if (dim > 1) a[1] = 0.1f
            return a.toList()
        }

        private fun insertTemplate(
            id: UUID,
            domain: String,
            userDesc: String,
            prompt: String,
            createdAt: LocalDateTime,
            topic: String? = null,
            topicEmbedding: List<Float>? = null,
        ): ChunkingPromptTemplate {
            val tpl =
                ChunkingPromptTemplate(
                    id = id,
                    domainName = domain,
                    userDescription = userDesc,
                    generatedPrompt = prompt,
                    createdAt = createdAt,
                    topic = topic,
                    topicEmbedding = topicEmbedding,
                )
            mapper.insert(tpl)
            return tpl
        }
    }
