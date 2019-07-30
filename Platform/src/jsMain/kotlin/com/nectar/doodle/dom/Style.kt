@file:Suppress("NOTHING_TO_INLINE")

package com.nectar.doodle.dom

import com.nectar.doodle.drawing.AffineTransform
import com.nectar.doodle.drawing.AffineTransform.Companion.Identity
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.drawing.Font.Weight
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.image.Image
import com.nectar.doodle.system.Cursor
import org.w3c.dom.css.CSSStyleDeclaration
import kotlin.math.max

internal val defaultFontWeight = 500
internal val defaultFontFamily = "monospace"
internal val defaultFontSize   = 13

private typealias Style = CSSStyleDeclaration

val Color.rgbaString get() = "rgba($red,$green,$blue,$opacity)"

private inline fun em(value: Number, force: Boolean = false) = value.toDouble().let { if (it > 0 || force) "${it}px" else "" } //"${value.toDouble() / 16}em" // TODO: Fix

internal inline fun Style.setTextIndent(value: Double) { textIndent = em(value) }

internal inline fun Style.setTop   (value: Double) { top    = em(value          ) }
internal inline fun Style.setLeft  (value: Double) { left   = em(value          ) }
internal inline fun Style.setWidth (value: Double) { width  = em(max(0.0, value)) }
internal inline fun Style.setHeight(value: Double) { height = em(max(0.0, value)) }

internal fun Style.setSize(value: Size) { value.run { setWidth(width); setHeight(height) } }

internal fun Style.setBounds(value: Rectangle) {
    value.run {
        setTop   (y     )
        setLeft  (x     )
        setWidth (width )
        setHeight(height)
    }
}

internal inline fun Style.setTopPercent   (percent: Double        ) { top    = "$percent%" }
internal inline fun Style.setLeftPercent  (percent: Double        ) { left   = "$percent%" }
internal inline fun Style.setRightPercent (percent: Double        ) { right  = "$percent%" }
internal inline fun Style.setWidthPercent (percent: Double? = null) { width  = percent?.let { "$it%" } ?: "" }
internal inline fun Style.setHeightPercent(percent: Double? = null) { height = percent?.let { "$it%" } ?: "" }
internal inline fun Style.setBottomPercent(percent: Double        ) { bottom = "$percent%" }

internal fun rgba(color: Color) = color.run { "rgba($red,$green,$blue,$opacity)" }

internal inline fun Style.setColor  (value: Color?      ) { color   = value?.let { rgba(it) } ?: "" }
internal inline fun Style.setCursor (value: Cursor      ) { cursor  = value.toString() }
internal inline fun Style.setOpacity(value: kotlin.Float) { opacity = if (value != 1f) value.toString() else "" }

internal fun Style.setFont(value: Font?) {
    when (value) {
        null -> font = "inherit"
        else -> value.also {
            setFontSize(it.size)
            setFontFamily(it.family)
            if(it.italic) setFontStyle(FontStyle.Italic())
            setFontWeight(it.weight)
        }
    }
}

internal inline fun Style.setFontStyle (value: FontStyle) { fontStyle  = value.value     }
internal inline fun Style.setFontWeight(value: Weight   ) { fontWeight = "${value.value}" }

internal inline fun Style.setFontSize  (value: Int   ) { this.fontSize   = em(max(0, value)) }
internal inline fun Style.setFontFamily(value: String) { this.fontFamily = value            }

internal inline fun Style.setDisplay (value: Display?  = null) { display  = value?.value ?: "" }
internal inline fun Style.setPosition(value: Position? = null) { position = value?.value ?: "" }

internal inline fun Style.setOverflow (overflow: Overflow) { overflow.also { setOverflowX(it); setOverflowY(it) } }
internal inline fun Style.setOverflowX(overflow: Overflow) { overflowX = overflow.value }
internal inline fun Style.setOverflowY(overflow: Overflow) { overflowY = overflow.value }

internal inline fun Style.setVisibility(value: Visibility) { visibility = value.value }

internal inline fun Style.setBackgroundImage(image: Image? = null) { backgroundImage = image?.let { "url(${it.source})" } ?: "none" }

internal inline fun Style.setBackgroundColor(color: Color? = null) { backgroundColor = color?.let { rgba(it) } ?: ""     }

internal inline fun Style.setBackgroundRepeat(repeat: Repeat) { backgroundRepeat = repeat.value }

internal inline fun Style.setBackgroundPosition(x: Double, y: Double) { backgroundPosition = "$x $y" }

internal inline fun Style.setOutlineWidth(value: Double           ) { outlineWidth = em(value) }

internal inline fun Style.setBorderWidth (value: Double           ) { borderWidth  = em(value) }
internal inline fun Style.setBorderRadius(value: Double           ) { borderRadius = em(value) }
internal inline fun Style.setBorderRadius(x    : Double, y: Double) { borderRadius = "${em(x, true)} / ${em(y, true)}" }
internal inline fun Style.setBorderColor (color: Color? = null    ) { borderColor  = color?.let { rgba(it)} ?: "" }
internal inline fun Style.setBorderStyle (style: BorderStyle) { borderStyle  = style.value }

internal inline fun Style.setBorderTop   (value: Double) { borderTop    = value.toString() }
internal inline fun Style.setBorderLeft  (value: Double) { borderLeft   = value.toString() }
internal inline fun Style.setBorderRight (value: Double) { borderRight  = value.toString() }
internal inline fun Style.setBorderBottom(value: Double) { borderBottom = value.toString() }

internal inline fun Style.setMargin      (value: Double) { margin       = value.toString() }
internal inline fun Style.setMarginTop   (value: Double) { marginTop    = value.toString() }
internal inline fun Style.setMarginLeft  (value: Double) { marginLeft   = value.toString() }
internal inline fun Style.setMarginRight (value: Double) { marginRight  = value.toString() }
internal inline fun Style.setMarginBottom(value: Double) { marginBottom = value.toString() }


internal inline fun Style.setTextAlignment(alignment: TextAlignment) { textAlign = alignment.value }

internal inline fun Style.setVerticalAlignment(alignment: VerticalAlign) { verticalAlign = alignment.value }

internal inline fun Style.setFloat(float: Float? = null) { cssFloat = float?.value ?: "" }

internal fun Style.translate(to: Point) = translate(to.x, to.y)
internal fun Style.translate(x: Double, y: Double) {
    // FIXME: Handle case when transform is already set?
    transform = when {
        x == 0.0 && y == 0.0 -> ""
        else                 -> "translate(${em(x, true)}, ${em(y, true)})"
    }
}

internal fun Style.setTransform(transform: AffineTransform?) {
    this.transform = when (transform) {
        null, Identity -> ""
        else           -> transform.run { "matrix($scaleX,$shearY,$shearX,$scaleY,$translateX,$translateY)" }
    }
}

internal inline fun Style.setBoxSizing(boxSizing: BoxSizing) { this.boxSizing = boxSizing.value }

internal sealed class Display(val value: String)
internal class None       : com.nectar.doodle.dom.Display("none"        )
internal class Block      : com.nectar.doodle.dom.Display("block"       )
internal class Inline     : com.nectar.doodle.dom.Display("inline"      )
internal class InlineBlock: com.nectar.doodle.dom.Display("inline-block")

internal sealed class Position(val value: String)
internal class Absolute: Position("absolute")
internal class Relative: Position("relative")
internal class Static  : Position("static"  )

internal sealed class Float(val value: String)
internal class Left : Float("left" )
internal class Right: Float("right")

internal sealed class Repeat(val value: String) {
    internal class RepeatAll: Repeat("repeat"   )
    internal class RepeatX  : Repeat("repeat_x" )
    internal class RepeatY  : Repeat("repeat_y" )
    internal class NoRepeat : Repeat("no_repeat")
}

internal sealed class Overflow(val value: String) {
    internal class Auto  : Overflow("auto"  )
    internal class Hidden: Overflow("hidden")
}

internal sealed class FontStyle(val value: String) {
    internal class Normal : FontStyle("normal" )
    internal class Italic : FontStyle("italic" )
    internal class Oblique: FontStyle("oblique")
}

internal sealed class Visibility(val value: String) {
    internal class Hidden : Visibility("hidden" )
    internal class Visible: Visibility("visible")
}

internal sealed class BorderStyle(val value: String) {
    internal class None  : BorderStyle("none"  )
    internal class Dotted: BorderStyle("dotted")
    internal class Dashed: BorderStyle("dashed")
    internal class Solid : BorderStyle("solid" )
}

internal sealed class TextAlignment(val value: String) {
    internal class Right : TextAlignment("right" )
    internal class Center: TextAlignment("center")
}

internal sealed class VerticalAlign(val value: String) {
    internal class Middle : VerticalAlign("middle")
}

internal sealed class BoxSizing(val value: String) {
    internal class Content: BoxSizing("content-box")
    internal class Border : BoxSizing("border-box" )
}