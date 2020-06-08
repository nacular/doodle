package io.nacular.doodle

import io.nacular.doodle.core.View
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.NativeCanvas
import org.w3c.dom.HTMLIFrameElement

/**
 * Created by Nicholas Eddy on 3/4/20.
 */
class UrlView internal constructor(htmlFactory: HtmlFactory): View() {
    var url = ""

    private val root = htmlFactory.create<HTMLIFrameElement>("iframe").apply {
        style.setWidthPercent (100.0)
        style.setHeightPercent(100.0)

        src = url
    }

    override fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            canvas.addData(listOf(root))
        }
    }
}