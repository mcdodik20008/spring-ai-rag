package mcdodik.springai.extensions

import org.springframework.ai.vectorstore.filter.Filter

fun Filter.Expression?.toFilterClause(): String {
    return ""
}