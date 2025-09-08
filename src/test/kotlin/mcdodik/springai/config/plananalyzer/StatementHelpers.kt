package mcdodik.springai.config.plananalyzer

import mcdodik.springai.config.plananalyzer.datasource.ExplainOptions
import mcdodik.springai.config.plananalyzer.datasource.ExplainProxyDataSource
import java.sql.ResultSet

/** Вспомогалки, общие для Statement/PreparedStatement */
internal fun shouldExplain(
    sql: String,
    opts: ExplainOptions,
): Boolean {
    val trimmed = sql.trimStart()
    val isQuery = trimmed.uppercase().startsWith("SELECT") || trimmed.uppercase().startsWith("WITH")
    if (!isQuery) return false
    if (opts.excludeRegex?.containsMatchIn(trimmed) == true) return false
    if (opts.includeRegex != null && !opts.includeRegex.containsMatchIn(trimmed)) return false
    return true
}

internal fun explainPrefix(opts: ExplainOptions): String =
    buildString {
        append("EXPLAIN (")
        append("ANALYZE ").append(if (opts.analyze) "true" else "false").append(", ")
        append("BUFFERS ").append(if (opts.buffers) "true" else "false").append(", ")
        append("VERBOSE ").append(if (opts.verbose) "true" else "false").append(", ")
        append("TIMING ").append(if (opts.timing) "true" else "false").append(", ")
        append("SUMMARY ").append(if (opts.summary) "true" else "false").append(", ")
        append("FORMAT JSON) ")
    }

internal fun buildJson(
    rs: ResultSet,
    max: Int,
): String {
    val sb = StringBuilder()
    while (rs.next() && sb.length < max) {
        sb.append(rs.getString(1))
        if (sb.length >= max) break
    }
    return sb.toString()
}

internal fun emit(
    ds: ExplainProxyDataSource,
    sql: String,
    params: Map<Int, Any?>,
    planJson: String,
    caller: String?,
) {
    val sinks = runCatching { ds.sinksProvider() }.getOrElse { emptyList() }
    if (sinks.isEmpty()) {
        ds.logger.debug("No PlanSink registered — report not written for: {}", sql.take(120))
        return
    }
    val p = CapturedPlan(sql = sql, params = params.toSortedMap(), planJson = planJson, caller = caller)
    sinks.forEach {
        runCatching { it.accept(p) }
            .onFailure { e -> ds.logger.warn("PlanSink failed: ${e.message}") }
    }
}

internal fun originWhitelisted(opts: ExplainOptions): Pair<Boolean, String?> {
    val el = firstExternalCaller(opts) ?: return false to null
    val cn = el.className
    val ok = opts.whitelistPackages.isEmpty() || opts.whitelistPackages.any { cn.startsWith(it) }
    val where = "${el.className}#${el.methodName}:${el.lineNumber}"
    return ok to where
}

internal fun firstExternalCaller(opts: ExplainOptions): StackTraceElement? {
    val skip = opts.blacklistPackages + opts.internalPackages
    return Thread.currentThread().stackTrace.firstOrNull { el ->
        val cn = el.className ?: return@firstOrNull false
        skip.none { cn.startsWith(it) }
    }
}
