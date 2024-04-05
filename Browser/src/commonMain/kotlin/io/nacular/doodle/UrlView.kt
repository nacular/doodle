package io.nacular.doodle

import io.nacular.doodle.core.View
import io.nacular.doodle.dom.HTMLIFrameElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.NativeCanvas
import io.nacular.doodle.utils.observable

/**
 * Created by Nicholas Eddy on 3/4/20.
 */
public class UrlView internal constructor(htmlFactory: HtmlFactory): View() {
    /**
     * The URL to show in the View
     */
    public var url: String by observable("") { _,new ->
        root.src = new
    }

    private val root = htmlFactory.create<HTMLIFrameElement>("iframe").apply {
        style.setWidthPercent (100.0)
        style.setHeightPercent(100.0)
        style.border = "none"

        src = url
    }

    override fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            canvas.addData(listOf(root))
        }
    }
}