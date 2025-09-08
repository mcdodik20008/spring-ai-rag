package mcdodik.springai.config.plananalyzer

fun interface PlanSink {
    fun accept(plan: CapturedPlan)
}
