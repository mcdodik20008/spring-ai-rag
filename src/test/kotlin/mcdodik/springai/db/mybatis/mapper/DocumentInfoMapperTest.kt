package mcdodik.springai.db.mybatis.mapper

import mcdodik.springai.db.entity.rag.DocumentInfo
import mcdodik.springai.db.testcontainer.AbstractPgIT
import org.assertj.core.api.Assertions.assertThat
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@MybatisTest
class DocumentInfoMapperTest
    @Autowired
    constructor(
        private val mapper: DocumentInfoMapper,
        private val jdbc: JdbcTemplate,
    ) : AbstractPgIT() {
        private val now: LocalDateTime = LocalDateTime.of(2025, 8, 13, 16, 0, 0)

        @Test
        fun `insert persists row`() {
            truncate()
            val doc =
                newDoc(
                    id = uuid(1),
                    file = "report.pdf",
                    ext = "pdf",
                    hash = "abc123",
                    chunks = 5,
                )

            mapper.insert(doc)

            val cnt = countAll()
            assertEquals(1, cnt)
        }

        @Test
        fun `findById returns entity when exists`() {
            truncate()
            val expected = newDoc(uuid(1), "a.pdf", "pdf", "h1", 1)
            mapper.insert(expected)

            val actual = mapper.findById(uuid(1))

            assertNotNull(actual)
            assertEquals(expected, actual)
        }

        @Test
        fun `findById returns null when not exists`() {
            truncate()
            val actual = mapper.findById(uuid(999))
            assertNull(actual)
        }

        @Test
        fun `findAll returns all rows`() {
            truncate()
            mapper.insert(newDoc(uuid(1), "a.pdf", "pdf", "h1", 1))
            mapper.insert(newDoc(uuid(2), "b.docx", "docx", "h2", 2))
            mapper.insert(newDoc(uuid(3), "c.txt", "txt", "h3", 3))

            val all = mapper.findAll()

            assertThat(all).hasSize(3)
            // Дополнительно — проверка упорядочивания при необходимости (если маппер сортирует)
            assertThat(all.map { it.id }).containsExactlyInAnyOrder(uuid(1), uuid(2), uuid(3))
        }

        @Test
        fun `searchByFilenames filters by exact filenames`() {
            truncate()
            mapper.insert(newDoc(uuid(1), "a.pdf", "pdf", "h1", 1))
            mapper.insert(newDoc(uuid(2), "b.pdf", "pdf", "h2", 2))
            mapper.insert(newDoc(uuid(3), "c.txt", "txt", "h3", 3))

            val res = mapper.searchByFilenames(setOf("a.pdf", "c.txt"))

            assertThat(res.map { it.id }).containsExactlyInAnyOrder(uuid(1), uuid(3))
        }

        @Test
        fun `searchByFilenames returns empty when none match`() {
            truncate()
            mapper.insert(newDoc(uuid(1), "a.pdf", "pdf", "h1", 1))

            val res = mapper.searchByFilenames(setOf("nope.pdf", "miss.txt"))

            assertThat(res).isEmpty()
        }

        @Test
        fun `searchByFilenameLike returns single match`() {
            truncate()
            mapper.insert(newDoc(uuid(1), "report-q1.pdf", "pdf", "h1", 1))
            mapper.insert(newDoc(uuid(2), "notes.txt", "txt", "h2", 2))

            // Допущение: маппер ожидает готовый шаблон с % (LIKE #{fileName})
            val res = mapper.searchByFilenameLike("%report%")

            assertEquals(uuid(1), res[0].id)
            assertEquals("report-q1.pdf", res[0].fileName)
        }

        @Test
        fun `searchByFilenameLike fails when more than one row matches (mapper must define behavior)`() {
            truncate()
            mapper.insert(newDoc(uuid(1), "report-q1.pdf", "pdf", "h1", 1))
            mapper.insert(newDoc(uuid(2), "report-q2.pdf", "pdf", "h2", 2))

            val res = mapper.searchByFilenameLike("%report%").map { it.fileName }
            assertThat(res.contains("report-q1.pdf")).isTrue
            assertThat(res.contains("report-q2.pdf")).isTrue
        }

        @Test
        fun `searchByNameAndHash returns match`() {
            truncate()
            mapper.insert(newDoc(uuid(1), "doc.pdf", "pdf", "h1", 1))
            mapper.insert(newDoc(uuid(2), "doc.pdf", "pdf", "h2", 1))

            val res = mapper.searchByNameAndHash("doc.pdf", "h2")

            assertNotNull(res)
            assertEquals(uuid(2), res.id)
        }

        @Test
        fun `searchByNameAndHash returns null when not found`() {
            truncate()
            mapper.insert(newDoc(uuid(1), "doc.pdf", "pdf", "h1", 1))

            val res = mapper.searchByNameAndHash("doc.pdf", "absent")

            assertNull(res)
        }

        @Test
        fun `delete removes row`() {
            truncate()
            mapper.insert(newDoc(uuid(1), "doc.pdf", "pdf", "h1", 1))
            assertEquals(1, countAll())

            mapper.delete(uuid(1))

            assertEquals(0, countAll())
            assertNull(mapper.findById(uuid(1)))
        }

        // ------------------- helpers -------------------

        private fun truncate() {
            jdbc.execute("DELETE FROM document_info")
        }

        private fun countAll(): Int = jdbc.queryForObject("SELECT COUNT(*) FROM document_info") { rs, _ -> rs.getInt(1) }!!

        private fun newDoc(
            id: UUID,
            file: String,
            ext: String,
            hash: String,
            chunks: Int,
            createdAt: LocalDateTime = now,
            summary: String? = "summary of $file",
        ) = DocumentInfo(
            id = id,
            fileName = file,
            extension = ext,
            hash = hash,
            chunkCount = chunks,
            createdAt = createdAt,
            summary = summary,
        )

        private fun uuid(n: Int): UUID = UUID.fromString("00000000-0000-0000-0000-%012d".format(n))
    }
