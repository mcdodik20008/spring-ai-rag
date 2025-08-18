package mcdodik.springai.config.plananalyzer

import java.nio.file.Path
import java.security.MessageDigest
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class FilesystemPlanSink(
    reportsDir: Path = Path.of("build/reports/explain"),
) : PlanSink {
    private val base = reportsDir.also { it.createDirectories() }
    private val summary = Summary(base.resolve("index.md"))

    override fun accept(plan: CapturedPlan) {
        val id = sha1(plan.sql + "@" + plan.capturedAt).take(12)
        val dir = base.resolve(id).also { it.createDirectories() }

        // JSON план
        dir.resolve("plan.json").writeText(plan.planJson)

        // Офлайн HTML-визуализация
        val html =
            PlanHtmlRenderer.render(
                plan,
            )
        dir.resolve("viewer.html").writeText(html)

        // Markdown-отчёт
        dir.resolve("report.md").writeText(renderReportMd(plan, id))

        // Сводка
        summary.add(id, plan.sql, plan.capturedAt)
        summary.flush()
    }

    // === Markdown per-query ===
    private fun renderReportMd(
        p: CapturedPlan,
        id: String,
    ): String =
        buildString {
            appendLine("# Plan $id")
            appendLine()
            appendLine("**SQL**")
            appendLine("```sql")
            appendLine(p.sql.trim())
            appendLine("```")
            if (p.params.isNotEmpty()) {
                appendLine()
                appendLine("**Params**")
                appendLine("```")
                appendLine(
                    p.params
                        .toSortedMap()
                        .entries
                        .joinToString(", ") { (k, v) -> "$k=${formatParam(v)}" },
                )
                appendLine("```")
            }
            appendLine()
            appendLine("- Captured: ${DateTimeFormatter.ISO_INSTANT.format(p.capturedAt)}")
            p.caller?.let { appendLine("- Caller: `$it`") }
            appendLine("- JSON: `plan.json`")
            appendLine("- HTML Viewer: `viewer.html`")
        }

    private fun formatParam(v: Any?): String =
        when (v) {
            null -> "NULL"
            is ByteArray -> "bytea[${v.size}]"
            else -> v.toString()
        }

    private fun sha1(s: String) =
        MessageDigest
            .getInstance("SHA-1")
            .digest(s.toByteArray())
            .joinToString("") { "%02x".format(it) }

    // === сводная index.md ===
    private class Summary(
        private val file: Path,
    ) {
        private val items = ConcurrentLinkedQueue<Triple<String, String, Instant>>()

        fun add(
            id: String,
            sql: String,
            at: Instant,
        ) {
            items += Triple(id, sql, at)
        }

        fun flush() {
            val body =
                buildString {
                    appendLine("# EXPLAIN summary")
                    items.forEach { (id, sql, at) ->
                        appendLine("- [$id](./$id/report.md) — `${sql.take(180).replace('\n', ' ')}` · ${DateTimeFormatter.ISO_INSTANT.format(at)}")
                    }
                }
            file.writeText(body)
        }
    }
}
