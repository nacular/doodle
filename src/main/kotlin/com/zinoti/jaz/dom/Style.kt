package com.zinoti.jaz.dom

import com.zinoti.jaz.drawing.Color
import com.zinoti.jaz.image.Image
import com.zinoti.jaz.system.Cursor
import org.w3c.dom.css.CSSStyleDeclaration

private typealias Style = CSSStyleDeclaration

fun Style.setTopPercent   (percent: Double        ) { this.top    = "$percent%" }
fun Style.setLeftPercent  (percent: Double        ) { this.left   = "$percent%" }
fun Style.setRightPercent (percent: Double        ) { this.right  = "$percent%" }
fun Style.setWidthPercent (percent: Double? = null) { this.width  = percent?.let { "$it%" } ?: "" }
fun Style.setHeightPercent(percent: Double? = null) { this.height = percent?.let { "$it%" } ?: "" }
fun Style.setBottomPercent(percent: Double        ) { this.bottom = "$percent%" }

fun Style.setColor (color : Color ) { this.color  = "#${color.hexString}" }
fun Style.setCursor(cursor: Cursor) { this.cursor = cursor.toString() }

fun Style.setFontStyle (fontStyle : FontStyle ) { this.fontStyle  = fontStyle.value;      }
fun Style.setFontWeight(fontWeight: FontWeight) { this.fontWeight = "${fontWeight.value}" }

fun Style.setDisplay (display : Display?  = null) { this.display  = display?.value  ?: "" }
fun Style.setPosition(position: Position? = null) { this.position = position?.value ?: "" }

//fun Style.setOverflow (overflow: Overflow) { this.overflow  = overflow;   }
fun Style.setOverflowX(overflow: Overflow) { this.overflowX = overflow.value; }
fun Style.setOverflowY(overflow: Overflow) { this.overflowY = overflow.value; }

fun Style.setVisibility(visibility: Visibility) { this.visibility = visibility.value; }

fun Style.setBackgroundImage(image: Image? = null) { this.backgroundImage = image?.let { "url(${it.source})" } ?: "none" }
fun Style.setBackgroundColor(color: Color? = null) { this.backgroundColor = color?.let { "#${it.hexString}"  } ?: ""     }

fun Style.setBackgroundRepeat(repeat: Repeat) { this.backgroundRepeat = repeat.value; }

fun Style.setBackgroundPosition(x: Double, y: Double) { this.backgroundPosition = "$x $y" }

fun Style.setBorderWidth(width: Double) { this.borderWidth = width.toString() }

fun Style.setBorderColor(color: Color? = null) { this.borderColor = color?.let { "#${it.hexString}"  } ?: ""     }

fun Style.setBorderStyle(style: BorderStyle) {this.borderStyle = style.value; }

fun Style.setBorderTop   (value: Double) {this.borderTop    = value.toString() }
fun Style.setBorderLeft  (value: Double) {this.borderLeft   = value.toString() }
fun Style.setBorderRight (value: Double) {this.borderRight  = value.toString() }
fun Style.setBorderBottom(value: Double) {this.borderBottom = value.toString() }

fun Style.setTextAlignment(alignment: TextAlignment) { this.textAlign = alignment.value; }

fun Style.setVerticalAlignment(alignment: VerticalAlign) { this.verticalAlign = alignment.value; }

fun Style.setFloat(float: Float? = null) { this.setPropertyValue("float", float?.value ?: "") }

//    fun setOpacity( float aOpacity )
//    {
//        // FIXME: This is here b/c GWT SuperDevMode refuses to call this class' static
//        // initializer.s
//        if( sStyleImpl == null ) { sStyleImpl = GWT.create( StyleImpl.class ); }
//
//        sStyleImpl.setOpacity( this, aOpacity );
//    }
//

enum class Float        (val value: String) { Left     ("left"    ), Right   ("right"   )                                                   }
enum class Repeat       (val value: String) { RepeatAll("repeat"  ), RepeatX ("repeat_x"), RepeatY("repeat_y"), NoRepeat   ("no_repeat")    }
enum class Display      (val value: String) { None     ("none"    ), Block   ("block"   ), Inline ("inline"  ), InlineBlock("inline-block") }
enum class Position     (val value: String) { Absolute ("absolute"), Relative("relative"), Static ("static"  )                              }
enum class Overflow     (val value: String) { Hidden   ("hidden"  ), Scroll  ("scroll"  )                                                   }
enum class FontStyle    (val value: String) { Normal   ("normal"  ), Italic  ("italic"  ), Oblique("oblique" )                              }
enum class Visibility   (val value: String) { Hidden   ("hidden"  ), Visible ("visible" )                                                   }
enum class BorderStyle  (val value: String) { Dotted   ("dotted"  ), Dashed  ("dashed"  ), Solid  ("solid"   )                              }
enum class TextAlignment(val value: String) { Right    ("right"   ), Center  ("center"  )                                                   }
enum class VerticalAlign(val value: String) { Middle   ("middle"  )                                                                         }
enum class FontWeight   (val value: Int   ) {
    Thinnest(100),
    Thinner (200),
    Thin    (300),
    Normal  (400),
    Thick   (500),
    Thicker (600),
    Bold    (700),
    Bolder  (800),
    Boldest (900),
}
