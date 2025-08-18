package mcdodik.springai.config.plananalyzer

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.math.max

object PlanHtmlRenderer {
    private val mapper = jacksonObjectMapper()

    fun render(capturedPlan: CapturedPlan): String {
        val root = mapper.readTree(capturedPlan.planJson)
        val top = root.get(0)
        val plan = top.get("Plan")
        val planning = top.path("Planning Time").asDouble(-1.0)
        val execution = top.path("Execution Time").asDouble(-1.0)

        val sb = StringBuilder()
        sb.appendLine("<!doctype html><html lang='en'><head>")
        sb.appendLine("<meta charset='utf-8'/>")
        sb.appendLine("<meta name='viewport' content='width=device-width, initial-scale=1'/>")
        sb.appendLine("<title>EXPLAIN Viewer</title>")
        sb.appendLine("<style>")
        sb.appendLine(BASE_CSS)
        sb.appendLine("</style>")
        sb.appendLine("</head><body>")
        sb.appendLine("<header><h1>PostgreSQL EXPLAIN</h1>")
        sb.appendLine("<div class='meta'>Captured: ${esc(capturedPlan.capturedAt.toString())}${capturedPlan.caller?.let { " • Caller: ${esc(it)}" } ?: ""}</div>")
        sb.appendLine("<details open><summary><strong>SQL</strong></summary>")
        sb.appendLine("<pre class='sql'>${esc(capturedPlan.sql.trim())}</pre>")
        if (capturedPlan.params.isNotEmpty()) {
            sb.appendLine("<div class='params'><b>Params:</b> ${esc(capturedPlan.params.toSortedMap().toString())}</div>")
        }
        sb.appendLine("</details>")
        if (planning >= 0 || execution >= 0) {
            sb.append("<div class='times'>")
            if (planning >= 0) sb.append("<span>Planning: ${fmtMs(planning)}</span>")
            if (execution >= 0) sb.append("<span>Execution: ${fmtMs(execution)}</span>")
            sb.appendLine("</div>")
        }

        sb.appendLine("<div class='actions'>")
        sb.appendLine("<button onclick='expandAll()'>Expand all</button>")
        sb.appendLine("<button onclick='collapseAll()'>Collapse all</button>")
        sb.appendLine("</div>")

        // Рендер дерева
        val issues = mutableListOf<Issue>()
        sb.appendLine("<section class='tree'>")
        renderNode(plan, "Plan", issues, sb, 0)
        sb.appendLine("</section>")

        // Сводка по «красным флагам»
        sb.appendLine("<section class='issues'><h2>Findings</h2>")
        if (issues.isEmpty()) {
            sb.appendLine("<div class='ok'>No obvious issues detected.</div>")
        } else {
            sb.appendLine("<ul>")
            issues.forEach { sb.appendLine("<li class='sev-${it.sev}'>[${it.sev}] ${esc(it.where)} — ${esc(it.hint)}</li>") }
            sb.appendLine("</ul>")
        }
        sb.appendLine("</section>")

        // Мелкий JS только для раскрытия/сворачивания <details>
        sb.appendLine("<script>")
        sb.appendLine(
            """
            function expandAll(){ document.querySelectorAll('details').forEach(d=>d.open=true); }
            function collapseAll(){ document.querySelectorAll('details').forEach(d=>d.open=false); }
            """.trimIndent(),
        )
        sb.appendLine("</script>")

        sb.appendLine("</body></html>")
        return sb.toString()
    }

    // --- рекурсивный рендер одного узла + сбор проблем ---
    private fun renderNode(
        n: JsonNode,
        path: String,
        issues: MutableList<Issue>,
        sb: StringBuilder,
        depth: Int,
    ) {
        val nodeType = n.path("Node Type").asText("")
        val rel = n.path("Relation Name").asText("")
        val alias = n.path("Alias").asText("")
        val indexName = n.path("Index Name").asText("")
        val planRows = n.path("Plan Rows").asDouble(-1.0)
        val actualRows = n.path("Actual Rows").asDouble(-1.0)
        val loops = n.path("Actual Loops").asDouble(1.0)
        val totalTime = n.path("Actual Total Time").asDouble(n.path("Total Cost").asDouble(-1.0))
        val filter = n.path("Filter").asText("")
        val indexCond = n.path("Index Cond").asText("")
        val sortMethod = n.path("Sort Method").asText("")
        val sortSpaceType = n.path("Sort Space Type").asText("")
        val batches = n.path("Batches").asInt(1)
        val sharedHit = n.path("Shared Hit Blocks").asLong(0)
        val sharedRead = n.path("Shared Read Blocks").asLong(0)
        val tempRead = n.path("Temp Read Blocks").asLong(0)
        val tempWritten = n.path("Temp Written Blocks").asLong(0)

        // эвристики (подсветка)
        addIssues(nodeType, rel, filter, indexCond, sortMethod, sortSpaceType, batches, sharedHit, sharedRead, tempRead, tempWritten, planRows, actualRows, loops, totalTime, path, issues)

        val header =
            buildString {
                append(nodeType)
                if (rel.isNotBlank()) append(" on ").append(rel)
                if (alias.isNotBlank()) append(" as ").append(alias)
                if (indexName.isNotBlank()) append(" using ").append(indexName)
                val meta = mutableListOf<String>()
                if (actualRows >= 0) meta += "rows=${trimD(actualRows)}"
                if (planRows >= 0) meta += "est=${trimD(planRows)}"
                if (loops >= 0) meta += "loops=${trimD(loops)}"
                if (totalTime >= 0) meta += "time=${fmtMs(totalTime)}"
                if (meta.isNotEmpty()) append(" • ").append(meta.joinToString(" | "))
            }

        sb.appendLine("<details class='node' ${if (depth <= 1) "open" else ""}><summary>${esc(header)}</summary>")
        // Детали узла
        sb.appendLine("<div class='kv'>")

        fun kv(
            k: String,
            v: String,
        ) {
            if (v.isNotBlank()) sb.appendLine("<div><span>$k</span><code>${esc(v)}</code></div>")
        }
        kv("Filter", filter)
        kv("Index Cond", indexCond)
        kv("Sort", listOfNotNull(sortMethod.takeIf { it.isNotBlank() }, sortSpaceType.takeIf { it.isNotBlank() }).joinToString(" "))
        if (sharedHit + sharedRead + tempRead + tempWritten > 0) {
            kv("Buffers", "hit=$sharedHit read=$sharedRead tempRead=$tempRead tempWritten=$tempWritten")
        }
        sb.appendLine("</div>")

        // Дети
        n.path("Plans")?.forEachIndexed { i, child ->
            renderNode(child, "$path → [$i] ${child.path("Node Type").asText("")}", issues, sb, depth + 1)
        }
        sb.appendLine("</details>")
    }

    private fun addIssues(
        nodeType: String,
        rel: String,
        filter: String,
        indexCond: String,
        sortMethod: String,
        sortSpaceType: String,
        batches: Int,
        sharedHit: Long,
        sharedRead: Long,
        tempRead: Long,
        tempWritten: Long,
        planRows: Double,
        actualRows: Double,
        loops: Double,
        totalTime: Double,
        path: String,
        issues: MutableList<Issue>,
    ) {
        // 1) ошибка кардинальности
        if (planRows > 0 && actualRows > 0) {
            val ratio = max((actualRows + 1.0) / (planRows + 1.0), (planRows + 1.0) / (actualRows + 1.0))
            if (ratio >= 10) issues += Issue("HIGH", path, "Большая ошибка кардинальности (×${trimD(ratio)}). Проверьте статистику/селективность.")
        }
        // 2) Seq Scan большой таблицы с фильтром
        if (nodeType == "Seq Scan" && rel.isNotBlank() && filter.isNotBlank() && indexCond.isBlank() && actualRows >= 100_000) {
            issues += Issue("HIGH", "$path on $rel", "Полный скан большой таблицы. Рассмотрите индекс по предикату.")
        }
        // 3) «дорогой» nested loop
        if (nodeType == "Nested Loop" && actualRows >= 10_000 && loops >= 100) {
            issues += Issue("HIGH", path, "Nested Loop выглядит дорогим. Нужен индекс на ключи соединения внутреннего узла.")
        }
        // 4) сортировки/хэши на диск
        if (nodeType.contains("Sort") && (sortSpaceType.equals("Disk", true) || tempRead > 0 || tempWritten > 0)) {
            issues += Issue("MEDIUM", path, "Сортировка на диск. Индекс под ORDER BY или точечный work_mem.")
        }
        if (nodeType == "Hash" && batches > 1) {
            issues += Issue("MEDIUM", path, "Hash разбит на батчи — не влезает в память. Увеличьте work_mem или уменьшите вход.")
        }
        // 5) высокий физический I/O
        if (sharedRead > sharedHit && sharedRead > 0) {
            issues += Issue("LOW", path, "Высокий физический I/O (read > hit). Проверьте кэш/индексы.")
        }
        // 6) узлы с большим временем
        if (totalTime >= 50.0) { // эвристика
            issues += Issue("LOW", path, "Долгий узел (${fmtMs(totalTime)}).")
        }
    }

    private data class Issue(
        val sev: String,
        val where: String,
        val hint: String,
    )

    private fun esc(s: String): String =
        s
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    private fun fmtMs(ms: Double) = "${"%.3f".format(ms)} ms"

    private fun trimD(d: Double) = if (d >= 0) "%.0f".format(d) else "n/a"

    private val BASE_CSS =
        """
        :root { --bg:#0f172a; --card:#111827; --fg:#e5e7eb; --muted:#9ca3af; --ok:#10b981; --warn:#f59e0b; --bad:#ef4444; }
        html,body{background:var(--bg);color:var(--fg);font:14px/1.5 ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;margin:0;padding:0}
        header{padding:16px 24px;border-bottom:1px solid #1f2937}
        h1{margin:0 0 8px 0;font-size:18px}
        .meta,.times,.params{color:var(--muted);margin:8px 0}
        .times span{margin-right:12px}
        .sql{white-space:pre-wrap;background:#0b1220;padding:12px;border-radius:10px;border:1px solid #1f2937}
        .actions{padding:8px 24px;border-bottom:1px solid #1f2937}
        .actions button{background:#1f2937;border:1px solid #374151;border-radius:8px;color:var(--fg);padding:6px 10px;margin-right:8px;cursor:pointer}
        .tree{padding:12px 24px}
        details.node{margin:8px 0;border:1px solid #1f2937;border-radius:10px;background:#0b1220}
        details.node > summary{padding:8px 12px;cursor:pointer}
        details.node > .kv{padding:8px 12px;border-top:1px dashed #1f2937}
        .kv div{display:flex;gap:10px}
        .kv span{min-width:100px;color:var(--muted)}
        .issues{padding:12px 24px}
        .ok{color:var(--ok)}
        .sev-LOW{color:#93c5fd}
        .sev-MEDIUM{color:var(--warn)}
        .sev-HIGH{color:var(--bad);font-weight:600}
        """.trimIndent()
}
