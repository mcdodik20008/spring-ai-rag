package mcdodik.springai.config.plananalyzer

import mcdodik.springai.config.plananalyzer.datasource.ExplainOptions
import mcdodik.springai.config.plananalyzer.datasource.ExplainProxyDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.env.Environment
import org.springframework.test.context.TestPropertySource
import javax.sql.DataSource

@TestConfiguration
@TestPropertySource(
    properties = [
        "sql.explain.enabled=true",
        "sql.explain.whitelist.packages=mcdodik.springai",
    ],
)
class ExplainProxyDsPostProcessorConfig :
    BeanPostProcessor,
    ApplicationContextAware {
    private lateinit var ctx: ApplicationContext
    private val log = LoggerFactory.getLogger(javaClass)

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.ctx = applicationContext
    }

    override fun postProcessAfterInitialization(
        bean: Any,
        beanName: String,
    ): Any {
        if (bean is DataSource && bean !is ExplainProxyDataSource) {
            val env: Environment = ctx.environment
            val excludeDefault =
                "(?is)\\b(select\\s+version\\(\\)|select\\s+current_user|select\\s+current_schema|pg_namespace|pg_catalog\\.|pg_roles\\b|pg_class\\b|pg_try_advisory_xact_lock|set_config\\(|search_path\\b)"
            val opts =
                ExplainOptions(
                    analyze = env.getProperty("sql.explain.analyze", Boolean::class.java, true),
                    buffers = env.getProperty("sql.explain.buffers", Boolean::class.java, true),
                    verbose = env.getProperty("sql.explain.verbose", Boolean::class.java, true),
                    timing = env.getProperty("sql.explain.timing", Boolean::class.java, false),
                    summary = env.getProperty("sql.explain.summary", Boolean::class.java, true),
                    includeRegex =
                        env
                            .getProperty("sql.explain.include", "")
                            .takeIf { it.isNotBlank() }
                            ?.toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
                    excludeRegex =
                        (env.getProperty("sql.explain.exclude", "").ifBlank { excludeDefault })
                            .toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
                    maxPlanChars = env.getProperty("sql.explain.max-plan-chars", Int::class.java, 1_000_000),
                    whitelistPackages =
                        env
                            .getProperty("sql.explain.whitelist.packages", "mcdodik.springai")
                            .split(',')
                            .map { it.trim() }
                            .filter { it.isNotEmpty() },
                )

            val enabled = env.getProperty("sql.explain.enabled", Boolean::class.java, true)
            if (!enabled) return bean

            val provider = { ctx.getBeansOfType(PlanSink::class.java).values.toList() }
            return ExplainProxyDataSource(target = bean, sinksProvider = provider, opts = opts)
        }
        return bean
    }
}
