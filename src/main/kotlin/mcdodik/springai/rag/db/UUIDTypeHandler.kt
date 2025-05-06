package mcdodik.springai.rag.db

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*

class UUIDTypeHandler : BaseTypeHandler<UUID>() {
    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: UUID, jdbcType: JdbcType?) {
        ps.setObject(i, parameter)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): UUID? {
        return rs.getObject(columnName, UUID::class.java)
    }

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): UUID? {
        return rs.getObject(columnIndex, UUID::class.java)
    }

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): UUID? {
        return cs.getObject(columnIndex, UUID::class.java)
    }
}
