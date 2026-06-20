package io.nacular.doodle.drawing.impl

import io.nacular.doodle.FontSerializer
import io.nacular.doodle.dom.CanvasRenderingContext2D
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.HTMLCanvasElement
import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.defaultFontSize
import io.nacular.doodle.dom.insert
import io.nacular.doodle.dom.letterSpacing
import io.nacular.doodle.dom.parent
import io.nacular.doodle.dom.setPosition
import io.nacular.doodle.dom.setWidth
import io.nacular.doodle.dom.wordSpacing
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.maxFont
import io.nacular.doodle.scheduler.Scheduler
import io.nacular.doodle.scheduler.Task
import io.nacular.doodle.text.AnnotatedString
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.text.TextSpacing.Companion.default
import io.nacular.doodle.utils.LeastRecentlyUsedCache
import io.nacular.doodle.utils.TextAlignment.Start
import io.nacular.doodle.utils.autoCanceling
import io.nacular.doodle.utils.splitMatches
import io.nacular.measured.units.Measure
import io.nacular.measured.units.Time
import io.nacular.measured.units.Time.Companion.seconds
import io.nacular.measured.units.times
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.max

internal class TextMetricsImpl(
    private val scheduler     : Scheduler,
    private val textFactory   : TextFactory,
    private val htmlFactory   : HtmlFactory,
    private val elementRuler  : ElementRuler,
    private val fontSerializer: FontSerializer,
                cacheLength   : Int,
    private val noFontSupport : Boolean = false,
    private val cleanupDelay  : Measure<Time> = 5 * seconds,
): TextMetrics {
    private data class TextInfo        (val text: String,     val font: Font?,   val textSpacing: TextSpacing = default)
    private data class StyledTextInfo  (val text: AnnotatedString, val textSpacing: TextSpacing = default)
    private data class WrappedInfo     (val text: String,     val width: Double, val indent: Double, val font: Font?, val lineSpacing: Float?, val textSpacing: TextSpacing)
    private data class WrappedStyleInfo(val text: AnnotatedString, val width: Double, val indent: Double, val lineSpacing: Float?, val textSpacing: TextSpacing)

    private class LayoutCursor(
        val currentX           : Double = 0.0,
        val currentY           : Double = 0.0,
        val totalLines         : Int    = 1,
        val maxLineWidthReached: Double = 0.0
    )

    private class ChainedTextDimensions(
        val nextCursor : LayoutCursor,
        val totalWidth : Double,
        val totalHeight: Double,
        val linesCount : Int
    )

    private val widths              = LeastRecentlyUsedCache<TextInfo,         Double> (maxSize = cacheLength)
    private val styledWidths        = LeastRecentlyUsedCache<StyledTextInfo,   Double> (maxSize = cacheLength)
    private val wrappedWidths       = LeastRecentlyUsedCache<WrappedInfo,      Double> (maxSize = cacheLength)
    private val wrappedStyledWidths = LeastRecentlyUsedCache<WrappedStyleInfo, Double> (maxSize = cacheLength)

    private val heights              = LeastRecentlyUsedCache<TextInfo,         Double> (maxSize = cacheLength)
    private val styledHeights        = LeastRecentlyUsedCache<StyledTextInfo,   Double> (maxSize = cacheLength)
    private val wrappedHeights       = LeastRecentlyUsedCache<WrappedInfo,      Double> (maxSize = cacheLength)
    private val wrappedStyledHeights = LeastRecentlyUsedCache<WrappedStyleInfo, Double> (maxSize = cacheLength)

    private val renderingContext = htmlFactory.create<HTMLCanvasElement>("canvas").getContext("2d") as CanvasRenderingContext2D

    private val noWordSpacingSupport   = (renderingContext.wordSpacing   == null)
    private val noLetterSpacingSupport = (renderingContext.letterSpacing == null)

    private fun addElementToRoot(element: HTMLElement) {
        if (element.parent == null) {
            element.style.setPosition(Point(0, 10_000))
            htmlFactory.root.insert(element, 0)
        }
    }

    private var elementTask: Task? by autoCanceling()

    private var element: HTMLElement? = null; set(value) {
        if (field != value) {
            field = value?.also { addElementToRoot(it) }
        }

        elementTask = scheduler.after(cleanupDelay) {
            field?.remove()
            field = null
        }
    }

    private var wrappedElement: HTMLElement? = null; set(value) {
        if (field != value) {
            field = value?.also { addElementToRoot(it) }
        }

        elementTask = scheduler.after(cleanupDelay) {
            field?.remove()
            field = null
        }
    }

    override fun width(text: String, font: Font?, textSpacing: TextSpacing) = widths.getOrPut(TextInfo(text, font, textSpacing)) {
        textWidth(text, font, textSpacing)
    }

    override fun width(text: AnnotatedString, textSpacing: TextSpacing) = styledWidths.getOrPut(StyledTextInfo(text, textSpacing)) {
        textWidth(text, textSpacing)
    }

    override fun width(text: String, width: Double, indent: Double, font: Font?, textSpacing: TextSpacing) = wrappedWidths.getOrPut(
        WrappedInfo(text, width, indent, font, null, textSpacing)
    ) {
        when {
            canvasNotSupported(textSpacing) -> {
                val box = htmlFactory.create<HTMLElement>()

                box.appendChild(textFactory.wrapped(
                    text        = text,
                    font        = font,
                    url         = null,
                    width       = width,
                    indent      = indent,
                    alignment   = Start,
                    lineSpacing = null,
                    textSpacing = textSpacing,
//                    possible    = wrappedElement,
                )/*.also { wrappedElement = it }*/)

                box.style.setWidth(width)

                elementRuler.width(box)
            }
            else -> textSize(text, width, font, null, textSpacing).run { totalWidth }
        }
    }

    override fun width(text: AnnotatedString, width: Double, indent: Double, textSpacing: TextSpacing) = wrappedStyledWidths.getOrPut(
        WrappedStyleInfo(text, width, indent, null, textSpacing)
    ) {
        size(text, width, indent, textSpacing).width
    }

    override fun height(text: String, font: Font?) = heights.getOrPut(TextInfo(text, font)) {
        when {
            canvasNotSupported(default) -> {
                // Special check for empty added to avoid font black-holing if first text checked is empty string
                elementRuler.height(textFactory.create(
                    text.takeUnless { it.isBlank() } ?: " ",
                    font,
                    textSpacing = default,
                    possible    = element
                ).also { element = it })
            }
            else -> textSize(text, POSITIVE_INFINITY, font, null, default).totalHeight
        }
    }

    override fun height(text: AnnotatedString, textSpacing: TextSpacing) = styledHeights.getOrPut(StyledTextInfo(text, textSpacing)) {
        var maxHeight = 0.0

        text.forEach { (string, style) ->
            maxHeight = max(maxHeight, height(string, style.font))
        }

        maxHeight
    }

    override fun height(
        text       : String,
        width      : Double,
        indent     : Double,
        font       : Font?,
        textSpacing: TextSpacing
    ): Double = height(text, width, indent, font, lineHeight(font), textSpacing)

    override fun height(
        text       : String,
        width      : Double,
        indent     : Double,
        font       : Font?,
        lineSpacing: Float,
        textSpacing: TextSpacing
    ) = wrappedHeights.getOrPut(WrappedInfo(text, width, indent, font, lineSpacing, textSpacing)) {
        size(text, width, indent, font, lineSpacing, textSpacing).height
    }

    override fun height(
        text       : AnnotatedString,
        width      : Double,
        indent     : Double,
        textSpacing: TextSpacing
    ): Double = height(text, width, indent, lineHeight(text.maxFont), textSpacing)

    override fun height(
        text       : AnnotatedString,
        width      : Double,
        indent     : Double,
        lineSpacing: Float,
        textSpacing: TextSpacing
    ) = wrappedStyledHeights.getOrPut(WrappedStyleInfo(text, width, indent, lineSpacing, textSpacing)) {
        size(text, width, indent, lineSpacing, textSpacing).width
    }

    override fun size(
        text       : String,
        width      : Double,
        indent     : Double,
        font       : Font?,
        lineSpacing: Float,
        textSpacing: TextSpacing
    ): Size = when {
        canvasNotSupported(textSpacing) -> elementRuler.size(
            textFactory.wrapped(
                text        = text,
                font        = font,
                width       = width,
                indent      = indent,
                alignment   = Start,
                lineSpacing = lineSpacing,
                textSpacing = textSpacing,
                possible    = wrappedElement,
            ).also { wrappedElement = it }
        )
        else -> textSize(text, width, font, lineSpacing, textSpacing).run {
            Size(totalWidth, totalHeight)
        }
    }

    override fun size(
        text       : AnnotatedString,
        width      : Double,
        indent     : Double,
        lineSpacing: Float,
        textSpacing: TextSpacing
    ): Size = when {
        canvasNotSupported(textSpacing) -> elementRuler.size(
            textFactory.wrapped(
                text        = text,
                width       = width,
                indent      = indent,
                alignment   = Start,
                lineSpacing = lineSpacing,
                textSpacing = textSpacing,
                possible    = wrappedElement,
            ).also { wrappedElement = it })
        else -> {
            var cursor = LayoutCursor(currentX = indent)
            lateinit var dimension: ChainedTextDimensions

            text.forEach { (string, style) ->
                dimension = textSize(string, width, style.font, lineSpacing, textSpacing, cursor)
                cursor    = dimension.nextCursor
            }

            Size(dimension.totalWidth, dimension.totalHeight)
        }
    }

    private fun textWidth(text: String, font: Font?, textSpacing: TextSpacing) = when {
        canvasNotSupported(textSpacing) -> elementRuler.size(
            textFactory.create(
                text        = text,
                font        = font,
                url         = null,
                textSpacing = textSpacing,
                possible    = element,
            ).also { element = it }
        ).width
        else         -> {
            val lines = text.splitMatches(lineBreakRegex).run { matches.map { it.match } + remaining }

            renderingContext.font          = fontSerializer(font)
            renderingContext.wordSpacing   = "${max(0.0, textSpacing.wordSpacing  )}px"
            renderingContext.letterSpacing = "${max(0.0, textSpacing.letterSpacing)}px"

            lines.maxOf {
                renderingContext.measureText(it).width
            }
        }
    }

    private fun textWidth(text: AnnotatedString, textSpacing: TextSpacing): Double {
        var width       = 0.0
        var font        = null as Font?
        var runningText = ""

        text.filter { it.first.isNotEmpty() }.forEach { (string, style) ->
            if (style.font != font) {
                if (runningText.isNotEmpty()) {
                    width += textWidth(runningText, font, textSpacing)
                }

                font        = style.font
                runningText = string
            } else {
                runningText += string
            }
        }

        if (runningText.isNotEmpty()) {
            width += textWidth(runningText, font, textSpacing)
        }

        return width
    }

    private var currentFontSet = false
    private var currentFont = null as Font?

    private fun textSize(
        text       : String,
        width      : Double,
        font       : Font?,
        lineSpacing: Float?,
        textSpacing: TextSpacing,
        cursor     : LayoutCursor = LayoutCursor(),
    ): ChainedTextDimensions {

        if (!currentFontSet || currentFont != font) {
            renderingContext.font = fontSerializer(font)
            currentFont = font
            currentFontSet = true
        }

        renderingContext.wordSpacing   = "${max(0.0, textSpacing.wordSpacing  )}px"
        renderingContext.letterSpacing = "${max(0.0, textSpacing.letterSpacing)}px"

        val paragraphs = text.splitMatches(lineBreakRegex).run { matches.map { it.match } + remaining }

        // Mutable trackers initialized from the incoming cursor state
        var runningX     = cursor.currentX
        var runningY     = cursor.currentY
        var lineCount    = cursor.totalLines
        var maxLineWidth = cursor.maxLineWidthReached

        val lineHeight   = (lineSpacing?.takeIf { it >= 0 } ?: lineHeight(font)) * (font?.size ?: defaultFontSize)

        paragraphs.forEachIndexed { index, paragraph ->
            val words = paragraph.split(" ")
            var currentLine = ""

            if (index > 0) {
                runningX = 0.0
                runningY += lineHeight
                lineCount++
            }

            for (i in words.indices) {
                val word = words[i]
                val testSegment = if (currentLine.isEmpty()) word else "$currentLine $word"

                // Measure how long this specific segment variant is
                val testSegmentWidth = renderingContext.measureText(testSegment).width

                // Total boundary check: where does this line segment end from our starting point?
                val absoluteEndPosition = runningX + testSegmentWidth

                if (absoluteEndPosition > width && (currentLine.isNotEmpty() || runningX > 0.0)) {
                    // Current segment exceeds layout bounds. Commit the *previous* segment width.
                    val finalSegmentWidth = renderingContext.measureText(currentLine).width
                    val lineEndCoordinate = runningX + finalSegmentWidth
                    if (lineEndCoordinate > maxLineWidth) maxLineWidth = lineEndCoordinate

                    // Wrap down to the beginning of the next line
                    currentLine = word
                    runningX = 0.0
                    runningY += lineHeight
                    lineCount++
                } else {
                    currentLine = testSegment
                }
            }

            if (currentLine.isNotEmpty()) {
                val finalSegmentWidth = renderingContext.measureText(currentLine).width
                runningX += finalSegmentWidth
                if (runningX > maxLineWidth) maxLineWidth = runningX
            }
        }

        val finalCursor = LayoutCursor(
            currentX            = runningX,
            currentY            = runningY,
            totalLines          = lineCount,
            maxLineWidthReached = maxLineWidth
        )

        return ChainedTextDimensions(
            nextCursor  = finalCursor,
            totalWidth  = maxLineWidth,
            totalHeight = runningY + lineHeight,
            linesCount  = lineCount
        )
    }

    private val testText    = "Something"
    private var skipCanvas  = false
    private val lineHeights = LeastRecentlyUsedCache<Font?, Float>(10)

    internal fun lineHeight(font: Font?) = lineHeights.getOrPut(font) {
        skipCanvas = true
        val heightDefaultLineSpacing = height(testText, font).toFloat()
        skipCanvas = false

        heightDefaultLineSpacing / height(
            testText,
            font        = font,
            width       = POSITIVE_INFINITY,
            lineSpacing = 1f
        ).toFloat()
    }

    private fun canvasNotSupported(textSpacing: TextSpacing) = skipCanvas
            || noFontSupport
            || (textSpacing.wordSpacing   >= 0.0 && noWordSpacingSupport  )
            || (textSpacing.letterSpacing >= 0.0 && noLetterSpacingSupport)
}