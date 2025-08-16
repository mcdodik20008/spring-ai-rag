package mcdodik.springai.db.mybatis.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedJdbcTypes
import org.apache.ibatis.type.MappedTypes
import org.postgresql.util.PGobject
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

@MappedTypes(Map::class) // Map<String, Double>
@MappedJdbcTypes(JdbcType.OTHER) // для jsonb
class JsonbMapDoubleHandler : BaseTypeHandler<Map<String, Double>?>() {
    private val mapper = jacksonObjectMapper()

    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: Map<String, Double>?,
        jdbcType: JdbcType?,
    ) {
        val json = mapper.writeValueAsString(parameter ?: emptyMap<String, Double>())
        val pg =
            PGobject().apply {
                type = "jsonb"
                value = json
            }
        ps.setObject(i, pg)
    }

    override fun getNullableResult(
        rs: ResultSet,
        columnName: String,
    ): Map<String, Double>? = rs.getString(columnName)?.let { mapper.readValue<Map<String, Double>>(it) }

    override fun getNullableResult(
        rs: ResultSet,
        columnIndex: Int,
    ): Map<String, Double>? = rs.getString(columnIndex)?.let { mapper.readValue<Map<String, Double>>(it) }

    override fun getNullableResult(
        cs: CallableStatement,
        columnIndex: Int,
    ): Map<String, Double>? = cs.getString(columnIndex)?.let { mapper.readValue<Map<String, Double>>(it) }
}
