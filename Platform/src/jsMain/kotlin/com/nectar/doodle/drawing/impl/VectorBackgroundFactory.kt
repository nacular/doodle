package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.SvgFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.childAt
import com.nectar.doodle.dom.numChildren
import com.nectar.doodle.drawing.Brush
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
        // FIXME: Need to find a way to use foreignObject to work
        val canvas = /*CanvasImpl(region, htmlFactory, textFactory, this) {
            VectorRendererSvg(it, svgFactory, this)
        }*/

        object: CanvasImpl(region, htmlFactory, textFactory, this, {
            VectorRendererSvg(it, svgFactory, this)
        }) {
            override fun isSimple(brush: Brush) = false
        }

        canvas.apply(brush.fill)

        if (region.numChildren == 0) {
            return ""
        }

        var svg = region.firstChild

        if (region.numChildren > 1 || svg !is SVGElement) {
            svg = svgFactory("svg")

//            val foreignObject = svgFactory<SVGElement>("foreignObject").apply {
//                setAttribute("width",  "100%")
//                setAttribute("height", "100%")
//                style.transform = "translate(0, 0)"
//            }
//
//            svg.add(foreignObject)

            (0 .. region.numChildren).mapNotNull {region.childAt(it) }.forEach {
                svg.add(it)
//                (it as? HTMLElement)?.style?.setPosition(Absolute)
//
//                foreignObject.add(it)
            }
        }

        svg.setAttribute("xmlns",  "http://www.w3.org/2000/svg")
        svg.setAttribute("width",  "${brush.size.width }px"    )
        svg.setAttribute("height", "${brush.size.height}px"    )

        // FIXME: Better encoding
        return "url(\"data:image/svg+xml;utf8,${svg.outerHTML.replace("\"", "'").replace("#", "%23")}\")" //.also { println(it) }
    }
}