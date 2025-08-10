package mcdodik.springai.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Loggable {
    val logger: Logger
        get() {
            val clazz = this::class.java
            val target = clazz.enclosingClass ?: clazz
            return LoggerFactory.getLogger(target)
        }
}
