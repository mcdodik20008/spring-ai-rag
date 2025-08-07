package mcdodik.springai.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Interface that provides access to a logger instance for classes implementing it.
 * Simplifies logging by allowing direct access to the logger without manual initialization.
 */
interface Loggable {
    /**
     * Logger instance associated with the class that implements this interface.
     * The logger is automatically initialized using SLF4J LoggerFactory.
     */
    val logger: Logger
        get() = LoggerFactory.getLogger(this::class.java.enclosingClass)
}
