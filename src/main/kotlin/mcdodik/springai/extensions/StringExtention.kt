package mcdodik.springai.extensions

fun String?.sanitize(): String =
    this
        ?.replace("\u0000", "")
        ?.replace("\r\n", "\n")
        ?.trim()
        ?: ""

fun String?.extractBetween(
    start: String,
    end: String,
): String {
    if (this == null) return ""
    val i = this.indexOf(start)
    val j = this.indexOf(end)
    return if (i >= 0 && j > i) this.substring(i + start.length, j).trim() else ""
}
