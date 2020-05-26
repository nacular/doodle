package com.nectar.doodle

import com.nectar.doodle.core.View
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.impl.NativeCanvas
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