package mcdodik.springai.extensions

import org.apache.pdfbox.pdmodel.font.PDFont

fun PDFont.hasGlyph(ch: Char): Boolean {
    return try {
        this.encode(ch.toString())
        true
    } catch (_: Exception) {
        false
    }
}
