package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.LineHeightDetector

internal class LineHeightDetectorImpl(private val textMetrics: TextMetricsImpl): LineHeightDetector {
    override fun lineHeight(font: Font?) = textMetrics.lineHeight(font)
}