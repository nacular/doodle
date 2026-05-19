package io.nacular.doodle.dom

internal actual fun Node.remove(element: Node): Node? = tryRemove(this, element)

private fun tryRemove(parent: Node, child: Node): Node? = js("""
{
    try {
        parent.removeChild(child)
    } catch (error) {
        null
    }
}
""")