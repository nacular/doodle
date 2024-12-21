package io.nacular.doodle.drawing.impl

import io.nacular.doodle.FontSerializer
import io.nacular.doodle.dom.CanvasRenderingContext2D
import io.nacular.doodle.dom.HTMLCanvasElement
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.impl.ElementRulerImpl
import io.nacular.doodle.dom.setLineHeight
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.text.StyledText
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.utils.LeastRecentlyUsedCache

/**
 * Created by Nicholas Eddy on 4/19/24.
 */
internal interface TextVerticalAligner {
    fun verticalOffset(text: String, font: Font?, lineSpacing: Float = -1f): Double
}

internal class TextVerticalAlignerImpl(
    private val defaultFontSize: Int,
    private val elementRuler   : ElementRulerImpl,
    private val textFactoryImpl: TextFactoryImpl,
    private val fontSerializer : FontSerializer,
                htmlFactory    : HtmlFactory,
                cacheLength    : Int
): TextVerticalAligner {
    private val results          = LeastRecentlyUsedCache<Font?, Double>(maxSize = cacheLength)
    private val renderingContext = htmlFactory.create<HTMLCanvasElement>("canvas").getContext("2d") as CanvasRenderingContext2D

    override fun verticalOffset(text: String, font: Font?, lineSpacing: Float) = results.getOrPut(font) {

        var offsetY = (font?.size ?: defaultFontSize).toDouble()

        textFactoryImpl.createBoxed(StyledText(text, font), TextSpacing(), null).also {
            it.style.setLineHeight(lineSpacing)

            val (top, height) = elementRuler.measure(it) {
                (it.firstChild as? HTMLElement)?.let { child ->
                    child.offsetTop to child.offsetHeight
                } ?: (0 to 0)
            }

            renderingContext.font = fontSerializer(font)
            val decent = renderingContext.measureText(text).fontBoundingBoxDescent

            offsetY += top + height - offsetY - decent
        }

        offsetY
    }
}