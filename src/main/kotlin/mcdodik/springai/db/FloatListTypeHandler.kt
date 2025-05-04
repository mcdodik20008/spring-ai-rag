package mcdodik.springai.db

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.*
import org.postgresql.util.PGobject

class FloatListTypeHandler : BaseTypeHandler<List<Float>>() {

    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: List<Float>, jdbcType: JdbcType?) {
        val vectorStr = parameter.joinToString(prefix = "[", postfix = "]")  // формат pgvector
        val pgObject = PGobject()
        pgObject.type = "vector"
        pgObject.value = vectorStr
        ps.setObject(i, pgObject)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): List<Float>? {
        return parseVector(rs.getString(columnName))
    }

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): List<Float>? {
        return parseVector(rs.getString(columnIndex))
    }

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): List<Float>? {
        return parseVector(cs.getString(columnIndex))
    }

    private fun parseVector(value: String?): List<Float>? {
        return value?.removePrefix("[")?.removeSuffix("]")?.split(",")?.map { it.trim().toFloat() }
    }
}
