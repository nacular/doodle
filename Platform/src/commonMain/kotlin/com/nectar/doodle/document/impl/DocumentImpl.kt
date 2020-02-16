package com.nectar.doodle.document.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.controls.document.Document
import com.nectar.doodle.dom.Block
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.Static
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.setDisplay
import com.nectar.doodle.dom.setPosition
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.TextFactory
import com.nectar.doodle.drawing.impl.CanvasImpl
import com.nectar.doodle.text.StyledText

/**
 * Created by Nicholas Eddy on 2/13/20.
 */
class DocumentImpl(private val textFactory: TextFactory, htmlFactory: HtmlFactory): Document() {
    private val root = htmlFactory.create<HTMLElement>().apply {
        style.setDisplay (Block ())
        style.setPosition(Static())
    }

//    private var firstRender  = true
    private var oldOffsetHeight = 0.0

    init {
        height = 1.0 // FIXME: Remove once a better solution exists to allow first render

//        boundsChanged += { _,_,_ ->
//            if (!ignoreBounds && root.parentNode != null) {
//                ignoreBounds = true
//
//                height = root.offsetHeight.toDouble()
//
//                ignoreBounds = false
//            }
//        }
    }

    override fun render(canvas: Canvas) {
        if (canvas is CanvasImpl) {
            canvas.addData(listOf(root))

            val newHeight = root.offsetHeight.toDouble()

            if (oldOffsetHeight != newHeight) {
                height          = newHeight
                oldOffsetHeight = newHeight
            }
        }
    }

    override fun append(text: StyledText) {
        root.add(textFactory.create(text).apply { style.setPosition(Static()) })
    }

    override fun append(text: String, font: Font?) {
        root.add(textFactory.create(text, font).apply { style.setPosition(Static()) })
    }
}