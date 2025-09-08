package mcdodik.springai.config.plananalyzer.datasource

import mcdodik.springai.config.Loggable
import mcdodik.springai.config.plananalyzer.buildJson
import mcdodik.springai.config.plananalyzer.emit
import mcdodik.springai.config.plananalyzer.explainPrefix
import mcdodik.springai.config.plananalyzer.originWhitelisted
import mcdodik.springai.config.plananalyzer.shouldExplain
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

internal class ProxyStatement(
    private val delegate: Statement,
    private val rawConn: Connection,
    private val ds: ExplainProxyDataSource,
) : Statement by delegate,
    Loggable {
    override fun executeQuery(sql: String): ResultSet {
        logger.debug("ProxyStatement.executeQuery: {}", sql)
        maybeExplain(sql, emptyMap())
        return delegate.executeQuery(sql)
    }

    override fun execute(sql: String): Boolean {
        logger.debug("ProxyStatement.execute: {}", sql)
        maybeExplain(sql, emptyMap())
        return delegate.execute(sql)
    }

    override fun executeUpdate(sql: String): Int {
        logger.debug("ProxyStatement.executeUpdate: {}", sql)
        return delegate.executeUpdate(sql)
    }

    private fun maybeExplain(
        sql: String,
        params: Map<Int, Any?>,
    ) {
        if (!shouldExplain(sql, ds.opts)) return
        val (ok, caller) = originWhitelisted(ds.opts)
        if (!ok) {
            ds.logger.debug("EXPLAIN skipped (not whitelisted origin) for: {}", sql)
            return
        }

        runCatching {
            rawConn.prepareStatement(explainPrefix(ds.opts) + sql).use { ps ->
                ps.executeQuery().use { rs ->
                    val json = buildJson(rs, ds.opts.maxPlanChars)
                    ds.logger.info("EXPLAIN(JSON) for SQL (caller={}):\n{}", caller, sql)
                    emit(ds, sql, params, json, caller)
                }
            }
        }.onFailure { e -> ds.logger.warn("EXPLAIN failed: ${e.message}") }
    }
}
