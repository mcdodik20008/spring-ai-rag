package mcdodik.springai.extension

import org.springframework.ai.vectorstore.filter.Filter

fun Filter.Expression?.toFilterClause(): String {
    return ""
}