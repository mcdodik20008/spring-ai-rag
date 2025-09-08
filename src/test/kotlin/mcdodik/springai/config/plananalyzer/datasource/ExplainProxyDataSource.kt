package mcdodik.springai.config.plananalyzer.datasource

import mcdodik.springai.config.Loggable
import mcdodik.springai.config.plananalyzer.PlanSink
import java.io.PrintWriter
import java.sql.Connection
import java.util.logging.Logger
import javax.sql.DataSource

class ExplainProxyDataSource(
    private val target: DataSource,
    internal val sinksProvider: () -> List<PlanSink> = { emptyList() },
    internal val opts: ExplainOptions = ExplainOptions(),
) : DataSource,
    Loggable {
    override fun getConnection(): Connection = ProxyConnection(target.connection, this)

    override fun getConnection(
        username: String?,
        password: String?,
    ): Connection = ProxyConnection(target.getConnection(username, password), this)

    override fun <T : Any?> unwrap(iface: Class<T?>?): T? = (if (iface?.isInstance(this) == true) this else target.unwrap(iface)) as T?

    override fun isWrapperFor(iface: Class<*>?): Boolean = iface?.isInstance(this) ?: false || target.isWrapperFor(iface)

    override fun getLogWriter(): PrintWriter? = target.logWriter

    override fun setLogWriter(out: PrintWriter?) {
        target.logWriter = out
    }

    override fun setLoginTimeout(seconds: Int) {
        target.loginTimeout = seconds
    }

    override fun getLoginTimeout(): Int = target.loginTimeout

    override fun getParentLogger(): Logger? = Logger.getLogger("mcdodik.springai.plananalyzer")
}
