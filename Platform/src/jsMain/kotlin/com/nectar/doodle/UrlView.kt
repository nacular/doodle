package com.nectar.doodle

import com.nectar.doodle.core.View
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.setHeightPercent
import com.nectar.doodle.dom.setWidthPercent
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.impl.CanvasImpl
import org.w3c.dom.HTMLIFrameElement

/**
 * Created by Nicholas Eddy on 3/4/20.
 */
internal class UrlView(htmlFactory: HtmlFactory, url: String): View() {
    private val root = htmlFactory.create<HTMLIFrameElement>("iframe").apply {
        src = url
        style.setWidthPercent (100.0)
        style.setHeightPercent(100.0)
    }

    override fun render(canvas: Canvas) {
        if (canvas is CanvasImpl) {
            canvas.addData(listOf(root))
        }
    }
}