package mcdodik.springai.config

import mcdodik.springai.config.datasource.ExplainProxyDataSource
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.EnvironmentAware
import org.springframework.core.Ordered
import org.springframework.core.PriorityOrdered
import org.springframework.core.env.Environment
import javax.sql.DataSource

@TestConfiguration
class ExplainProxyDsPostProcessorConfig :
    BeanPostProcessor,
    EnvironmentAware,
    PriorityOrdered,
    Loggable {
    // настройки читаем из application-*.properties / системных свойств
    private var enabled: Boolean = true
    private var targetBeanName: String = "dataSource"
    private var wrapAll: Boolean = false
    private var skipBeanNames: Set<String> = emptySet()

    override fun setEnvironment(env: Environment) {
        enabled = env.getProperty("sql.explain.enabled", Boolean::class.java, true)
        targetBeanName = env.getProperty("sql.explain.bean-name", "dataSource")
        wrapAll = env.getProperty("sql.explain.wrap-all", Boolean::class.java, false)
        val skip = env.getProperty("sql.explain.skip-beans", "")
        skipBeanNames =
            skip
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
    }

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE

    override fun postProcessBeforeInitialization(
        bean: Any,
        beanName: String,
    ): Any = bean

    override fun postProcessAfterInitialization(
        bean: Any,
        beanName: String,
    ): Any {
        if (!enabled) return bean
        if (bean !is DataSource) return bean
        if (bean is ExplainProxyDataSource) return bean
        if (beanName in skipBeanNames) return bean
        if (!shouldWrap(beanName)) return bean

        val wrapped =
            ExplainProxyDataSource(
                target = bean,
            )
        logger.info("ExplainProxyDataSource applied to bean '{}'", beanName)
        return wrapped
    }

    private fun shouldWrap(beanName: String): Boolean =
        if (wrapAll) {
            true
        } else {
            // по умолчанию только основной бин
            beanName == targetBeanName || beanName.contains("dataSource", ignoreCase = true)
        }
}
