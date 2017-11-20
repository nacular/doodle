package com.nectar.doodle.dom

import com.nectar.doodle.drawing.AffineTransform
import com.nectar.doodle.drawing.Color
import com.nectar.doodle.drawing.Font.Weight
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.image.Image
import com.nectar.doodle.system.Cursor
import org.w3c.dom.css.CSSStyleDeclaration
import kotlin.math.max

private typealias Style = CSSStyleDeclaration

internal inline fun Style.setTop   (value: Double) { top    = "${value}px" }
internal inline fun Style.setLeft  (value: Double) { left   = "${value}px" }
internal inline fun Style.setWidth (value: Double) { width  = "${max(0.0, value)}px" }
internal inline fun Style.setHeight(value: Double) { height = "${max(0.0, value)}px" }

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

internal fun rgba(color: Color) = color.run { "rgba($red, $green, $blue, $opacity)" }

internal inline fun Style.setColor  (value: Color ) { color  = rgba(value) /*"#${value.hexString}"*/ }
internal inline fun Style.setCursor (value: Cursor) { cursor = value.toString() }
internal inline fun Style.setOpacity(value: kotlin.Float) { opacity = value.toString() }

internal inline fun Style.setFontStyle (value: FontStyle) { fontStyle  = value.value;     }
internal inline fun Style.setFontWeight(value: Weight   ) { fontWeight = "${value.value}" }

internal inline fun Style.setFontSize  (value: Int   ) { this.fontSize   = if (value >= 0) "${value}px" else "1em"  }
internal inline fun Style.setFontFamily(value: String) { this.fontFamily = value;                                   }


internal inline fun Style.setDisplay (value: Display?  = null) { display  = value?.value ?: "" }
internal inline fun Style.setPosition(value: Position? = null) { position = value?.value ?: "" }

//fun Style.setOverflow (overflow: Overflow) { this.overflow  = overflow;   }
internal inline fun Style.setOverflowX(overflow: Overflow) { overflowX = overflow.value; }
internal inline fun Style.setOverflowY(overflow: Overflow) { overflowY = overflow.value; }

internal inline fun Style.setVisibility(value: Visibility) { visibility = value.value; }

internal inline fun Style.setBackgroundImage(image: Image? = null) { backgroundImage = image?.let { "url(${it.source})" } ?: "none" }
internal inline fun Style.setBackgroundColor(color: Color? = null) { backgroundColor = color?.let { rgba(it) /*"#${it.hexString}"*/  } ?: ""     }

internal inline fun Style.setBackgroundRepeat(repeat: Repeat) { backgroundRepeat = repeat.value; }

internal inline fun Style.setBackgroundPosition(x: Double, y: Double) { backgroundPosition = "$x $y" }

internal inline fun Style.setBorderWidth(width: Double) { borderWidth = width.toString() }

internal inline fun Style.setBorderColor(color: Color? = null) { borderColor = color?.let { rgba(it) /*"#${it.hexString}"*/ } ?: "" }

internal inline fun Style.setBorderStyle(style: BorderStyle) {borderStyle = style.value; }

internal inline fun Style.setBorderTop   (value: Double) {borderTop    = value.toString() }
internal inline fun Style.setBorderLeft  (value: Double) {borderLeft   = value.toString() }
internal inline fun Style.setBorderRight (value: Double) {borderRight  = value.toString() }
internal inline fun Style.setBorderBottom(value: Double) {borderBottom = value.toString() }

internal inline fun Style.setTextAlignment(alignment: TextAlignment) { textAlign = alignment.value; }

internal inline fun Style.setVerticalAlignment(alignment: VerticalAlign) { verticalAlign = alignment.value; }

internal inline fun Style.setFloat(float: Float? = null) { setPropertyValue("float", float?.value ?: "") }

//    fun setOpacity( float aOpacity )
//    {
//        // FIXME: This is here b/c GWT SuperDevMode refuses to call this class' static
//        // initializer.s
//        if( sStyleImpl == null ) { sStyleImpl = GWT.create( StyleImpl.class ); }
//
//        sStyleImpl.setOpacity( this, aOpacity );
//    }
//

fun Style.setTransform(transform: AffineTransform?) = when(transform) {
    null, AffineTransform.Identity -> this.transform = ""
    else                           -> transform.run { this@setTransform.transform = "matrix($scaleX,$shearY,$shearX,$scaleY,$translateX,$translateY)" }
}


internal enum class Float        (val value: String) { Left     ("left"    ), Right   ("right"   )                                                   }
internal enum class Repeat       (val value: String) { RepeatAll("repeat"  ), RepeatX ("repeat_x"), RepeatY("repeat_y"), NoRepeat   ("no_repeat")    }
internal enum class Display      (val value: String) { None     ("none"    ), Block   ("block"   ), Inline ("inline"  ), InlineBlock("inline-block") }
internal enum class Position     (val value: String) { Absolute ("absolute"), Relative("relative"), Static ("static"  )                              }
internal enum class Overflow     (val value: String) { Hidden   ("hidden"  ), Scroll  ("scroll"  )                                                   }
internal enum class FontStyle    (val value: String) { Normal   ("normal"  ), Italic  ("italic"  ), Oblique("oblique" )                              }
internal enum class Visibility   (val value: String) { Hidden   ("hidden"  ), Visible ("visible" )                                                   }
internal enum class BorderStyle  (val value: String) { Dotted   ("dotted"  ), Dashed  ("dashed"  ), Solid  ("solid"   )                              }
internal enum class TextAlignment(val value: String) { Right    ("right"   ), Center  ("center"  )                                                   }
internal enum class VerticalAlign(val value: String) { Middle   ("middle"  )                                                                         }