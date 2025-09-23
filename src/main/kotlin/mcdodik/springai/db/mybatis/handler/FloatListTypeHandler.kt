package mcdodik.springai.db.mybatis.handler

import mcdodik.springai.config.Loggable
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.postgresql.util.PGobject
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

class FloatListTypeHandler : BaseTypeHandler<List<Float>>() {
    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: List<Float>,
        jdbcType: JdbcType?,
    ) {
        val vectorStr = parameter.joinToString(prefix = "[", postfix = "]") // pgvector формат
        val pgObject =
            PGobject().apply {
                type = "vector"
                value = vectorStr
            }
        ps.setObject(i, pgObject)
    }

    override fun getNullableResult(
        rs: ResultSet,
        columnName: String,
    ): List<Float>? {
        val raw = rs.getString(columnName)
        return parseVector(raw, columnName)
    }

    override fun getNullableResult(
        rs: ResultSet,
        columnIndex: Int,
    ): List<Float>? {
        val raw = rs.getString(columnIndex)
        return parseVector(raw, "col[$columnIndex]")
    }

    override fun getNullableResult(
        cs: CallableStatement,
        columnIndex: Int,
    ): List<Float>? {
        val raw = cs.getString(columnIndex)
        return parseVector(raw, "cs[$columnIndex]")
    }

    private fun parseVector(
        value: String?,
        origin: String,
    ): List<Float>? {
        if (value.isNullOrBlank()) return null
        if (!value.contains(",")) {
            logger.warn("Received suspicious value in $origin: '{}'", value)
            return null
        }

        return try {
            value
                .removePrefix("[")
                .removeSuffix("]")
                .split(",")
                .map { it.trim().toFloat() }
        } catch (e: Exception) {
            logger.warn("Failed to parse pgvector value in $origin: '{}'", value, e)
            null
        }
    }

    companion object : Loggable
}
