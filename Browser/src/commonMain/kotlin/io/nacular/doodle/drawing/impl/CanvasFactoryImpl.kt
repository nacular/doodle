package io.nacular.doodle.drawing.impl

import io.nacular.doodle.dom.HTMLElement
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.SvgFactory
import io.nacular.doodle.drawing.CanvasFactory
import io.nacular.doodle.drawing.LineHeightDetector
import io.nacular.doodle.drawing.TextFactory
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.utils.IdGenerator


internal class CanvasFactoryImpl(
    private val htmlFactory       : HtmlFactory,
    private val textFactory       : TextFactory,
    private val svgFactory        : SvgFactory,
    private val textMetrics       : TextMetrics,
    private val aligner           : TextVerticalAligner,
    private val idGenerator       : IdGenerator,
    private val useShadowHack     : Boolean,
    private val lineHeightDetector: LineHeightDetector,
): CanvasFactory {
    override operator fun invoke(region: HTMLElement) = CanvasImpl(region, htmlFactory, textFactory, textMetrics, useShadowHack, lineHeightDetector) {
        VectorRendererSvg(it, svgFactory, htmlFactory, aligner, textMetrics, idGenerator, lineHeightDetector)
    }
}