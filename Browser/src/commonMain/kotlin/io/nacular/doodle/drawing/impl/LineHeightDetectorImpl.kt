package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.LineHeightDetector
import io.nacular.doodle.drawing.TextMetrics
import kotlin.Double.Companion.POSITIVE_INFINITY

internal class LineHeightDetectorImpl(private val textMetrics: TextMetrics): LineHeightDetector {
    private val testText    = "Something"
    private val lineHeights = mutableMapOf<Font?, Float>()

    override fun lineHeight(font: Font?) = lineHeights.getOrPut(font) {
        textMetrics.height(testText, font).toFloat() / textMetrics.height(
            testText,
            font        = font,
            width       = POSITIVE_INFINITY,
            lineSpacing = 1f
        ).toFloat()
    }
}