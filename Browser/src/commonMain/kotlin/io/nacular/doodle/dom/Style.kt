@file:Suppress("NOTHING_TO_INLINE")

package io.nacular.doodle.dom

import io.nacular.doodle.dom.CSSStyleDeclaration
import io.nacular.doodle.dom.clipPath
import io.nacular.doodle.dom.textDecorationThickness
import io.nacular.doodle.drawing.AffineTransform
import io.nacular.doodle.drawing.AffineTransform.Companion.Identity
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Font.Style.Italic
import io.nacular.doodle.drawing.Font.Style.Normal
import io.nacular.doodle.drawing.Font.Style.Oblique
import io.nacular.doodle.geometry.Path
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Rectangle
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.image.Image
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.text.TextDecoration
import io.nacular.doodle.text.TextDecoration.Line
import io.nacular.doodle.text.TextDecoration.Line.Over
import io.nacular.doodle.text.TextDecoration.Line.Through
import io.nacular.doodle.text.TextDecoration.Line.Under
import io.nacular.doodle.text.TextDecoration.Thickness
import io.nacular.doodle.text.TextDecoration.Thickness.FromFont
import io.nacular.doodle.text.TextDecoration.Thickness.Percent
import io.nacular.doodle.text.TextSpacing
import io.nacular.doodle.utils.SquareMatrix
import io.nacular.doodle.utils.TextAlignment
import io.nacular.doodle.utils.TextAlignment.Center
import io.nacular.doodle.utils.TextAlignment.End
import io.nacular.doodle.utils.TextAlignment.Justify
import io.nacular.doodle.utils.TextAlignment.Start
import io.nacular.measured.units.Angle.Companion.degrees
import kotlin.math.max

internal const val defaultFontWeight = 500
internal const val defaultFontFamily = "monospace"
internal const val defaultFontSize   = 13

private typealias Style = CSSStyleDeclaration

internal val Color.rgbaString get() = "rgba($red,$green,$blue,$opacity)"

internal fun em(value: Number, force: Boolean = false) = value.toDouble().let { if (it != 0.0 || force) "${it}px" else "" } //"${value.toDouble() / 16}em" // TODO: Fix

internal inline fun Style.setTextIndent(value: Double) { textIndent = em(value, true) }

internal fun Style.setTextAlignment(alignment: TextAlignment?) {
    textAlign = when (alignment) {
        Start   -> "left"
        Center  -> "center"
        End     -> "right"
        Justify -> "justify"
        else    -> ""
    }
}

internal fun Style.setLineHeight(value: Float) {
    lineHeight = "$value"
}

internal fun Style.setTextSpacing(textSpacing: TextSpacing) {
    letterSpacing = when {
        textSpacing.letterSpacing != 0.0 -> "${textSpacing.letterSpacing}px"
        else                             -> ""
    }

    wordSpacing = when {
        textSpacing.wordSpacing != 0.0 -> "${textSpacing.wordSpacing}px"
        else                           -> ""
    }
}

internal inline fun Style.setTop   (value: Double) { top    = em(value) }
internal inline fun Style.setLeft  (value: Double) { left   = em(value) }
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

internal fun Style.setClipPath(value: Path?) {
    clipPath = when (value) {
        null -> ""
        else -> "path('${value.data}')"
    }
}

internal fun Style.setPosition(point: Point) {
    point.run {
        setTop (y)
        setLeft(x)
    }
}

internal inline fun Style.setWidthPercent (percent: Double? = null) { width  = percent?.let { "$it%" } ?: "" }
internal inline fun Style.setHeightPercent(percent: Double? = null) { height = percent?.let { "$it%" } ?: "" }

internal fun rgba(color: Color) = color.run { "rgba($red,$green,$blue,$opacity)" }

internal inline fun Style.setColor  (value: Color? ) { color   = value?.let { rgba(it) } ?: "" }
internal inline fun Style.setCursor (value: Cursor?) { cursor  = value?.let { "$it" } ?: "" }
internal inline fun Style.setOpacity(value: Float  ) { opacity = if (value != 1f) "$value" else "" }

internal fun Style.setTextDecoration(value: TextDecoration?) {
    when (value) {
        null -> {
            textDecorationLine      = ""
            textDecorationColor     = ""
            textDecorationStyle     = ""
            textDecorationThickness = ""
        }
        else -> {
            textDecorationLine      = value.lines.joinToString(" ") { it.styleText }.takeUnless { it == "" } ?: ""
            textDecorationColor     = value.color?.let { rgba(it) } ?: ""
            textDecorationStyle     = "${value.style}".lowercase()
            textDecorationThickness = when (val t = value.thickness) {
                FromFont              -> "from-font"
                is Percent            -> "${t.value}%"
                is Thickness.Absolute -> em(t.value)
                null                  -> ""
            }
        }
    }
}

private val Line.styleText: String get() = when (this) {
    Under   -> "underline"
    Over    -> "overline"
    Through -> "line-through"
}

internal fun Style.setFont(value: Font?) {
    when (value) {
        null -> font = "inherit"
        else -> value.also {
            setFontSize  (it.size  )
            setFontFamily(it.family)
            setFontStyle (it.style )
            setFontWeight(it.weight)
        }
    }
}

internal inline fun Style.setFontStyle(value: Font.Style) { fontStyle = value.styleText }

internal val Font.Style.styleText: String get() = when (this) {
    Normal     -> "normal"
    Italic     -> "italic"
    is Oblique -> "oblique${angle?.let { " ${it `in` degrees}deg" } ?: ""}"
}

internal inline fun Style.setFontWeight(value: Int       ) { fontWeight = "$value"    }

internal inline fun Style.setFontSize  (value: Int   ) { fontSize   = em(max(0, value)) }
internal inline fun Style.setFontFamily(value: String) { fontFamily = value            }

internal inline fun Style.setDisplay    (value: Display?  = null) { display  = value?.value ?: "" }
internal inline fun Style.setDomPosition(value: Position? = null) { position = value?.value ?: "" }

internal inline fun Style.setOverflow (overflow: Overflow? = null) { overflow.also { setOverflowX(it); setOverflowY(it) } }
internal inline fun Style.setOverflowX(overflow: Overflow? = null) { overflowX = overflow?.value ?: "" }
internal inline fun Style.setOverflowY(overflow: Overflow? = null) { overflowY = overflow?.value ?: "" }

internal inline fun Style.setBackgroundImage(image: Image? = null) { backgroundImage = image?.let { "url(${it.source})" } ?: "none" }
internal inline fun Style.setBackgroundSize(size: Size? = null) { backgroundSize = size?.let { "${em(it.width)} ${em(it.height)}" } ?: "" }

internal inline fun Style.setBackgroundColor(color: Color? = null) { backgroundColor = color?.let { rgba(it) } ?: ""     }

internal inline fun Style.setOutlineWidth(value: Double?          ) { outlineWidth = value?.let { em(value, true) } ?: "" }
internal inline fun Style.setBorderWidth (value: Double?          ) { borderWidth  = value?.let { em(it, true) } ?: "" }
internal inline fun Style.setBorderRadius(value: Double?          ) { borderRadius = value?.let { em(it) } ?: "" }
internal inline fun Style.setBorderRadius(x    : Double, y: Double) { borderRadius = "${em(x, true)} / ${em(y, true)}" }
internal inline fun Style.setBorderStyle (style: BorderStyle      ) { borderStyle  = style.value }

internal inline fun Style.setMargin      (value: Double           ) { margin       = "$value" }

internal inline fun Style.translate(to: Point) = translate(to.x, to.y)
internal fun Style.translate(x: Double, y: Double) {
    // FIXME: Handle case when transform is already set?
    transform = when {
        x == 0.0 && y == 0.0 -> ""
        else                 -> "translate(${em(x, true)}, ${em(y, true)})"
    }
}

internal fun Style.setTransform(transform: AffineTransform? = null) {
    this.transform = when {
        transform == null || transform == Identity -> ""
        transform.is3d                             -> transform.run {
            """matrix3d(
            |$m00,$m10,$m20,0,
            |$m01,$m11,$m21,0,
            |$m02,$m12,$m22,0,
            |$m03,$m13,$m23,1
            |)""".trimMargin()
        }
        else                                       -> transform.run { "matrix($scaleX,$shearY,$shearX,$scaleY,$translateX,$translateY)" }
    }
}

internal fun Style.setPerspectiveTransform(transform: SquareMatrix<Double>? = null) {
    this.transform = when {
        transform == null || transform.isIdentity -> ""
        else                                      -> transform.run {
            """matrix3d(
            |${this[0, 0]},${this[1, 0]},${this[2, 0]},${this[3, 0]},
            |${this[0, 1]},${this[1, 1]},${this[2, 1]},${this[3, 1]},
            |${this[0, 2]},${this[1, 2]},${this[2, 2]},${this[3, 2]},
            |${this[0, 3]},${this[1, 3]},${this[2, 3]},${this[3, 3]}
            |)""".trimMargin()
        }
    }
}

internal sealed class Display(val value: String)
internal class None       : Display("none"        )
internal class Block      : Display("block"       )
internal class Inline     : Display("inline"      )
internal class InlineBlock: Display("inline-block")

internal sealed class Position(val value: String)
internal class Static  : Position("static"  )

internal sealed class Overflow(val value: String) {
    internal class Scroll : Overflow("scroll" )
    internal class Auto   : Overflow("auto"   )
    internal class Hidden : Overflow("hidden" )
    internal class Visible: Overflow("visible")
}

internal sealed class BorderStyle(val value: String) {
    internal class None: BorderStyle("none")
}