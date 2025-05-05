package mcdodik.springai.extension

import org.commonmark.node.Node
import org.commonmark.node.Visitor

fun Node.visitChildren(visitor: Visitor) {
    var child = firstChild
    while (child != null) {
        val next = child.next
        child.accept(visitor)
        child = next
    }
}