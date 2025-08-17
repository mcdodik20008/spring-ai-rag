package mcdodik.springai.config.datasource

import mcdodik.springai.config.Loggable
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

class ProxyStatement(
    private val delegate: Statement,
    private val rawConn: Connection,
) : Statement by delegate,
    Loggable {
    val explainPrefix =
        "EXPLAIN (ANALYZE, BUFFERS, VERBOSE, FORMAT TEXT) "

    override fun executeQuery(sql: String): ResultSet {
        if (isSelect(sql)) explain(sql, emptyMap())
        return delegate.executeQuery(sql)
    }

    override fun execute(sql: String): Boolean {
        if (isSelect(sql)) explain(sql, emptyMap())
        return delegate.execute(sql)
    }

    override fun executeUpdate(sql: String): Int = delegate.executeUpdate(sql)

    private fun explain(
        sql: String,
        params: Map<Int, Any?>,
    ) {
        runCatching {
            rawConn.prepareStatement(explainPrefix + sql).use { ps ->
                bindParams(ps, params)
                ps.executeQuery().use { rs ->
                    val plan = buildPlan(rs)
                    logPlan(sql, plan, params)
                }
            }
        }.onFailure { e -> logger.warn("EXPLAIN failed: ${e.message}") }
    }

    private fun isSelect(sql: String): Boolean {
        val trimmed = sql.trimStart()
        return trimmed.uppercase().startsWith("SELECT") || trimmed.uppercase().startsWith("WITH")
    }

    private fun bindParams(
        ps: PreparedStatement,
        params: Map<Int, Any?>,
    ) {
        if (params.isEmpty()) return
        val max = params.keys.maxOrNull() ?: 0
        for (i in 1..max) {
            if (params.containsKey(i)) {
                ps.setObject(i, params[i])
            } else {
                // параметр не был установлен — ставим NULL без типа
                ps.setObject(i, null)
            }
        }
    }

    private fun buildPlan(rs: ResultSet): String {
        val sb = StringBuilder()
        while (rs.next()) {
            // FORMAT TEXT → одна колонка 'QUERY PLAN'
            sb.append(rs.getString(1)).append('\n')
        }
        return sb.toString()
    }

    private fun logPlan(
        sql: String,
        plan: String,
        params: Map<Int, Any?>,
    ) {
        val prefix = "/* params=${params.toSortedMap()} */\n"
        LoggerFactory
            .getLogger("SQL_EXPLAIN")
            .info("EXPLAIN for SQL:\n{}{}\n--- PLAN ---\n{}\n-------------", prefix, sql.trim(), plan)
    }
}
