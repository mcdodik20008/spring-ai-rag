package mcdodik.springai.config.plananalyzer.datasource

data class ExplainOptions(
    val analyze: Boolean = true,
    val buffers: Boolean = true,
    val verbose: Boolean = true,
    val timing: Boolean = false,
    val summary: Boolean = true,
    val includeRegex: Regex? = null,
    val excludeRegex: Regex? = null,
    val maxPlanChars: Int = 1_000_000,
    val whitelistPackages: List<String> = emptyList(),
    val blacklistPackages: List<String> =
        listOf(
            "org.postgresql.",
            "org.springframework.",
            "org.flywaydb.",
            "org.apache.ibatis.",
            "org.mybatis.",
            "java.",
            "kotlin.",
        ),
    val internalPackages: List<String> =
        listOf(
            // весь наш модуль план-аналитики
            ExplainProxyDataSource::class.java.packageName.removeSuffix(".datasource"),
            ExplainProxyDataSource::class.java.packageName,
        ),
)
