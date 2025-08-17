package mcdodik.springai.config.datasource

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import kotlin.math.min

class ProxyPreparedStatement(
    private val delegate: PreparedStatement,
    private val rawConn: Connection, // исходный, без прокси
    private val sql: String,
    private val logger: Logger,
) : PreparedStatement by delegate {
    val explainPrefix =
        "EXPLAIN (ANALYZE, BUFFERS, VERBOSE, FORMAT TEXT) "

    // храним параметры по индексу
    private val params = mutableMapOf<Int, Any?>()

    // универсальная точка: большинство setX в итоге вызывает setObject
    override fun setObject(
        parameterIndex: Int,
        x: Any?,
    ) {
        params[parameterIndex] = x
        delegate.setObject(parameterIndex, x)
    }

    // подстрахуемся для примитивов (JDBC может не звать setObject)
    override fun setString(
        parameterIndex: Int,
        x: String?,
    ) {
        params[parameterIndex] = x
        delegate.setString(parameterIndex, x)
    }

    override fun setInt(
        parameterIndex: Int,
        x: Int,
    ) {
        params[parameterIndex] = x
        delegate.setInt(parameterIndex, x)
    }

    override fun setLong(
        parameterIndex: Int,
        x: Long,
    ) {
        params[parameterIndex] = x
        delegate.setLong(parameterIndex, x)
    }

    override fun setBoolean(
        parameterIndex: Int,
        x: Boolean,
    ) {
        params[parameterIndex] = x
        delegate.setBoolean(parameterIndex, x)
    }

    override fun setDouble(
        parameterIndex: Int,
        x: Double,
    ) {
        params[parameterIndex] = x
        delegate.setDouble(parameterIndex, x)
    }

    override fun setFloat(
        parameterIndex: Int,
        x: Float,
    ) {
        params[parameterIndex] = x
        delegate.setFloat(parameterIndex, x)
    }

    override fun setBytes(
        parameterIndex: Int,
        x: ByteArray?,
    ) {
        params[parameterIndex] = x
        delegate.setBytes(parameterIndex, x)
    }

    override fun setTimestamp(
        parameterIndex: Int,
        x: Timestamp?,
    ) {
        params[parameterIndex] = x
        delegate.setTimestamp(parameterIndex, x)
    }

    override fun clearParameters() {
        params.clear()
        delegate.clearParameters()
    }

    override fun executeQuery(): ResultSet {
        if (isSelect(sql)) explain()
        return delegate.executeQuery()
    }

    override fun execute(): Boolean {
        if (isSelect(sql)) explain()
        return delegate.execute()
    }

    override fun executeUpdate(): Int {
        // не делаем ANALYZE для DML
        return delegate.executeUpdate()
    }

    private fun explain() {
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
        // пропустим WITH; берём первое слово
        val firstWord = trimmed.substring(0, min(10, trimmed.length)).uppercase()
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
