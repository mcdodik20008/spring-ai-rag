package mcdodik.springai.config.datasource

import java.io.PrintWriter
import java.sql.Connection
import java.util.logging.Logger
import javax.sql.DataSource

class ExplainProxyDataSource(
    private val target: DataSource,
) : DataSource {
    override fun getConnection(): Connection = ProxyConnection(target.connection)

    override fun getConnection(
        username: String?,
        password: String?,
    ): Connection = ProxyConnection(target.getConnection(username, password))

    override fun <T : Any?> unwrap(iface: Class<T?>?): T? = (if (iface?.isInstance(this) == true) this else target.unwrap(iface)) as T?

    override fun isWrapperFor(iface: Class<*>?): Boolean = iface?.isInstance(this) ?: false || target.isWrapperFor(iface)

    override fun getLogWriter(): PrintWriter? = target.logWriter

    override fun setLogWriter(out: PrintWriter?) = target.run { logWriter = out }

    override fun setLoginTimeout(seconds: Int) = target.run { loginTimeout = seconds }

    override fun getLoginTimeout(): Int = target.loginTimeout

    override fun getParentLogger(): Logger = Logger.getLogger("SQL_EXPLAIN")
}
