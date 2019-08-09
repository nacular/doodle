package com.nectar.doodle.drawing.impl

import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.Left
import com.nectar.doodle.dom.SvgFactory
import com.nectar.doodle.dom.add
import com.nectar.doodle.dom.childAt
import com.nectar.doodle.dom.numChildren
import com.nectar.doodle.dom.setFloat
import com.nectar.doodle.drawing.Brush
import com.nectar.doodle.drawing.PatternBrush
import com.nectar.doodle.drawing.TextFactory
import org.w3c.dom.HTMLBodyElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLStyleElement
import org.w3c.dom.asList
import org.w3c.dom.css.CSSStyleSheet
import org.w3c.dom.css.get
import org.w3c.dom.parsing.XMLSerializer
import org.w3c.dom.svg.SVGElement
import kotlin.browser.document

/**
 * Created by Nicholas Eddy on 1/21/19.
 */
interface VectorBackgroundFactory {
    operator fun invoke(brush: PatternBrush): String
}

class VectorBackgroundFactoryImage(private val htmlFactory: HtmlFactory): VectorBackgroundFactory {
    override fun invoke(brush: PatternBrush): String = "url(\"${
        ImageCanvas(htmlFactory.create(), htmlFactory, false).apply{ size = brush.size; brush.fill(this) }.image.source
    }\")"
}

class VectorBackgroundFactorySvg(private val htmlFactory: HtmlFactory, private val textFactory: TextFactory, private val svgFactory: SvgFactory): VectorBackgroundFactory {
    override fun invoke(brush: PatternBrush): String {
        val region = htmlFactory.create<HTMLElement>()

        // FIXME: Need to ensure this canvas only uses svg
        // FIXME: Need to find a way to get foreignObject to work
        val canvas = object: CanvasImpl(region, htmlFactory, textFactory, {
            VectorRendererSvg(it, svgFactory)
        }) {
            override fun isSimple(brush: Brush) = false
        }

        canvas.apply {
            canvas.size = brush.size

            brush.fill(this)
        }

        if (region.numChildren == 0) {
            return ""
        }

        var svg = region.firstChild

        if (region.numChildren > 1 || svg !is SVGElement) {
            svg = svgFactory("svg")

            val body: HTMLBodyElement

            val foreignObject = svgFactory<SVGElement>("foreignObject").apply {
                setAttribute("width",  "100%")
                setAttribute("height", "100%")
                setAttribute("externalResourcesRequired", "true")
                style.setFloat(Left())

                // TODO: Can this be optimized?
                val style = htmlFactory.create<HTMLStyleElement>("style").apply {
                    type = "text/css"

                    (document.styleSheets[0] as? CSSStyleSheet)?.cssRules?.asList()?.forEach {
                        appendChild(htmlFactory.createText(it.cssText))
                    }
                }

                body = htmlFactory.create<HTMLBodyElement>("body").apply {
                    setAttribute("xmlns", "http://www.w3.org/1999/xhtml")
                }

                add(style)
                add(body )
            }

            svg.add(foreignObject)

            (0 .. region.numChildren).mapNotNull { region.childAt(it) }.forEach {
                body.add(it)
            }
        }

        svg.setAttribute("xmlns",  "http://www.w3.org/2000/svg")
        svg.setAttribute("width",  "${brush.size.width }"    )
        svg.setAttribute("height", "${brush.size.height}"    )

        // FIXME: Better encoding
        return "url(\"data:image/svg+xml;utf8,${XMLSerializer().serializeToString(svg).replace("\"", "'").replace("#", "%23")}\")".also { println(it) }
    }
}