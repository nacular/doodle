package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.SvgFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.numChildren
import com.nectar.doodle.drawing.CanvasBrush
import com.nectar.doodle.drawing.TextFactory
import org.w3c.dom.HTMLElement
import org.w3c.dom.svg.SVGElement

/**
 * Created by Nicholas Eddy on 1/21/19.
 */

interface VectorBackgroundFactory {
    operator fun invoke(brush: CanvasBrush): String
}

class VectorBackgroundFactoryImpl(private val htmlFactory: HtmlFactory, private val textFactory: TextFactory, private val svgFactory : SvgFactory): VectorBackgroundFactory {
    override fun invoke(brush: CanvasBrush): String {
        val region = htmlFactory.create<HTMLElement>()

        // FIXME: Need to ensure this canvas only uses svg
        val canvas = CanvasImpl(region, htmlFactory, textFactory, this) {
            VectorRendererSvg(it, svgFactory, this)
        }

        canvas.apply(brush.fill)

        if (region.numChildren == 0) {
            return ""
        }

        var svg = region.firstChild

        if (region.numChildren > 1 || svg !is SVGElement) {
            svg = svgFactory("svg")

            svg.add(region.firstChild!!)
        }

        svg.setAttribute("xmlns",  "http://www.w3.org/2000/svg")
        svg.setAttribute("width",  "${brush.size.width }px"    )
        svg.setAttribute("height", "${brush.size.height}px"    )

        // FIXME: Better encoding
        return "url(\"data:image/svg+xml;utf8,${region.innerHTML.replace("\"", "'").replace("#", "%23")}\")"
    }
}