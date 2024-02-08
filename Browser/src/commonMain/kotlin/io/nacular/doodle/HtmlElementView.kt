package io.nacular.doodle

import io.nacular.doodle.core.View
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HTMLIFrameElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.SystemStyler
import io.nacular.doodle.dom.SystemStyler.Style
import io.nacular.doodle.dom.add
import io.nacular.doodle.dom.cssStyle
import io.nacular.doodle.dom.setHeightPercent
import io.nacular.doodle.dom.setWidthPercent
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.impl.NativeCanvas
import io.nacular.doodle.utils.IdGenerator

internal class HtmlElementView(
                htmlFactory : HtmlFactory,
    private val idGenerator : IdGenerator,
    private val systemStyler: SystemStyler,
    private val element     : HTMLElement,
    private val autoScale   : Boolean
): View() {
    private val root = htmlFactory.create<HTMLIFrameElement>().apply {
        className = className(idGenerator)

        if (autoScale) {
            style.setWidthPercent (100.0)
            style.setHeightPercent(100.0)
        }

        if (autoScale) {
            element.style.setWidthPercent(100.0)
            element.style.setHeightPercent(100.0)
        }

        add(element)
    }

    override fun addedToDisplay() {
        added(systemStyler)
    }

    override fun removedFromDisplay() {
        removed()
    }

    override fun render(canvas: Canvas) {
        if (canvas is NativeCanvas) {
            canvas.addData(listOf(root))
        }
    }

    private companion object {
        private var className: String? = null
        private var numView   = 0
        private var rule: Style? by cssStyle()

        private fun className(idGenerator: IdGenerator): String {
            return className ?: idGenerator.nextId().also { className = it }
        }

        private fun added(systemStyler: SystemStyler) {
            if (++numView == 1) {
                // Undo all the rules created by SystemStyler so hosted components
                // styles work.
                rule = systemStyler.insertRule(".$className * { all:revert }")
            }
        }
        private fun removed() {
            if (--numView <= 0) {
                rule = null
            }
        }
    }
}