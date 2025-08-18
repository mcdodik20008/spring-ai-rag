package mcdodik.springai.config.plananalyzer.datasource

import mcdodik.springai.config.Loggable
import mcdodik.springai.config.plananalyzer.buildJson
import mcdodik.springai.config.plananalyzer.emit
import mcdodik.springai.config.plananalyzer.explainPrefix
import mcdodik.springai.config.plananalyzer.originWhitelisted
import mcdodik.springai.config.plananalyzer.shouldExplain
import java.sql.Connection
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLType
import java.sql.Time
import java.sql.Timestamp
import java.sql.Types

internal class ProxyPreparedStatement(
    private val delegate: PreparedStatement,
    private val rawConn: Connection,
    private val sql: String,
    private val ds: ExplainProxyDataSource,
) : PreparedStatement by delegate,
    Loggable {
    private val params = mutableMapOf<Int, Param>()

    // ---- перехват setNull / setObject (все перегрузки) ----
    override fun setNull(
        parameterIndex: Int,
        sqlType: Int,
    ) {
        params[parameterIndex] = Param.Null(sqlType)
        delegate.setNull(parameterIndex, sqlType)
    }

    override fun setObject(
        parameterIndex: Int,
        x: Any?,
    ) {
        params[parameterIndex] = Param.Value(x)
        delegate.setObject(parameterIndex, x)
    }

    override fun setObject(
        parameterIndex: Int,
        x: Any?,
        targetSqlType: Int,
    ) {
        params[parameterIndex] = Param.TypedValue(x, targetSqlType)
        delegate.setObject(parameterIndex, x, targetSqlType)
    }

    override fun setObject(
        parameterIndex: Int,
        x: Any?,
        targetSqlType: Int,
        scaleOrLength: Int,
    ) {
        params[parameterIndex] = Param.ScaledValue(x, targetSqlType, scaleOrLength)
        delegate.setObject(parameterIndex, x, targetSqlType, scaleOrLength)
    }

    // JDK 9+: перегрузка с SQLType (если компилируешь под 17, добавь @Suppress("OVERRIDE_DEPRECATION") при необходимости)
    override fun setObject(
        parameterIndex: Int,
        x: Any?,
        targetSqlType: SQLType,
    ) {
        val t = targetSqlType.vendorTypeNumber ?: Types.OTHER
        params[parameterIndex] = Param.TypedValue(x, t)
        delegate.setObject(parameterIndex, x, targetSqlType)
    }

    // ---- наиболее частые setXxx (подстраховка, когда драйвер/фреймворк зовёт напрямую) ----
    override fun setString(
        parameterIndex: Int,
        x: String?,
    ) {
        params[parameterIndex] = Param.Value(x)
        delegate.setString(parameterIndex, x)
    }

    override fun setInt(
        parameterIndex: Int,
        x: Int,
    ) {
        params[parameterIndex] = Param.Value(x)
        delegate.setInt(parameterIndex, x)
    }

    override fun setLong(
        parameterIndex: Int,
        x: Long,
    ) {
        params[parameterIndex] = Param.Value(x)
        delegate.setLong(parameterIndex, x)
    }

    override fun setBoolean(
        parameterIndex: Int,
        x: Boolean,
    ) {
        params[parameterIndex] = Param.Value(x)
        delegate.setBoolean(parameterIndex, x)
    }

    override fun setDouble(
        parameterIndex: Int,
        x: Double,
    ) {
        params[parameterIndex] = Param.Value(x)
        delegate.setDouble(parameterIndex, x)
    }

    override fun setFloat(
        parameterIndex: Int,
        x: Float,
    ) {
        params[parameterIndex] = Param.Value(x)
        delegate.setFloat(parameterIndex, x)
    }

    override fun setBytes(
        parameterIndex: Int,
        x: ByteArray?,
    ) {
        params[parameterIndex] = Param.Value(x)
        delegate.setBytes(parameterIndex, x)
    }

    override fun setBigDecimal(
        parameterIndex: Int,
        x: java.math.BigDecimal?,
    ) {
        params[parameterIndex] = Param.Value(x)
        delegate.setBigDecimal(parameterIndex, x)
    }

    override fun setDate(
        parameterIndex: Int,
        x: Date?,
    ) {
        params[parameterIndex] = Param.Value(x)
        delegate.setDate(parameterIndex, x)
    }

    override fun setTime(
        parameterIndex: Int,
        x: Time?,
    ) {
        params[parameterIndex] = Param.Value(x)
        delegate.setTime(parameterIndex, x)
    }

    override fun setTimestamp(
        parameterIndex: Int,
        x: Timestamp?,
    ) {
        params[parameterIndex] = Param.Value(x)
        delegate.setTimestamp(parameterIndex, x)
    }

    override fun clearParameters() {
        params.clear()
        delegate.clearParameters()
    }

    // ---- выполнение ----
    override fun executeQuery(): ResultSet {
        logger.debug("ProxyPreparedStatement.executeQuery: {}", sql)
        maybeExplain()
        return delegate.executeQuery()
    }

    override fun execute(): Boolean {
        logger.debug("ProxyPreparedStatement.execute: {}", sql)
        maybeExplain()
        return delegate.execute()
    }

    override fun executeUpdate(): Int {
        logger.debug("ProxyPreparedStatement.executeUpdate: {}", sql)
        // DML — EXPLAIN ANALYZE не делаем
        return delegate.executeUpdate()
    }

    private fun maybeExplain() {
        if (!shouldExplain(sql, ds.opts)) return
        val (ok, caller) = originWhitelisted(ds.opts)
        if (!ok) {
            ds.logger.debug("EXPLAIN skipped (not whitelisted origin) for: {}", sql.take(160))
            return
        }

        runCatching {
            rawConn.prepareStatement(explainPrefix(ds.opts) + sql).use { ps ->
                bindParams(ps, params) // <— ВАЖНО: биндим параметры для EXPLAIN
                ps.executeQuery().use { rs ->
                    val json = buildJson(rs, ds.opts.maxPlanChars)
                    ds.logger.info("EXPLAIN(JSON) for Prepared SQL (caller={}):\n{}", caller, sql)
                    emit(ds, sql, prettyParams(params), json, caller)
                }
            }
        }.onFailure { e -> ds.logger.warn("EXPLAIN failed: ${e.message}") }
    }

    // — человекочитаемый вывод параметров для отчёта
    private fun prettyParams(map: Map<Int, Param>): Map<Int, Any?> =
        map.entries.associate { (k, v) ->
            k to
                when (v) {
                    is Param.Value -> v.v
                    is Param.TypedValue -> "${v.v}::${sqlTypeName(v.sqlType)}"
                    is Param.ScaledValue -> "${v.v}::${sqlTypeName(v.sqlType)}(${v.scaleOrLength})"
                    is Param.Null -> "NULL::${sqlTypeName(v.sqlType)}"
                }
        }

    private fun sqlTypeName(t: Int): String =
        when (t) {
            Types.VARCHAR -> "VARCHAR"
            Types.INTEGER -> "INTEGER"
            Types.BIGINT -> "BIGINT"
            Types.BOOLEAN -> "BOOLEAN"
            Types.DOUBLE -> "DOUBLE"
            Types.FLOAT -> "FLOAT"
            Types.DECIMAL -> "DECIMAL"
            Types.TIMESTAMP -> "TIMESTAMP"
            Types.DATE -> "DATE"
            Types.TIME -> "TIME"
            Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> "BYTEA"
            Types.OTHER -> "OTHER"
            else ->
                "T${
                    // не все типы перечислять; важно иметь читаемость
                    t
                }"
        }
}

private sealed class Param {
    data class Value(
        val v: Any?,
    ) : Param()

    data class TypedValue(
        val v: Any?,
        val sqlType: Int,
    ) : Param()

    data class ScaledValue(
        val v: Any?,
        val sqlType: Int,
        val scaleOrLength: Int,
    ) : Param()

    data class Null(
        val sqlType: Int,
    ) : Param()
}

private fun bindParams(
    ps: PreparedStatement,
    params: Map<Int, Param>,
) {
    if (params.isEmpty()) return
    val maxIdx = params.keys.maxOrNull() ?: return
    for (i in 1..maxIdx) {
        when (val p = params[i]) {
            null -> ps.setObject(i, null) // «дырка» — ставим NULL без типа
            is Param.Value -> ps.setObject(i, p.v)
            is Param.TypedValue -> ps.setObject(i, p.v, p.sqlType)
            is Param.ScaledValue -> ps.setObject(i, p.v, p.sqlType, p.scaleOrLength)
            is Param.Null -> ps.setNull(i, p.sqlType)
        }
    }
}
