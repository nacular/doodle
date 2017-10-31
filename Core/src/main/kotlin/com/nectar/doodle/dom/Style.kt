package com.nectar.doodle.dom

import com.nectar.doodle.drawing.Color
import com.nectar.doodle.image.Image
import com.nectar.doodle.system.Cursor
import org.w3c.dom.css.CSSStyleDeclaration
import kotlin.math.max

private typealias Style = CSSStyleDeclaration

inline fun Style.setTop   (value: Double) { top    = "${value}px" }
inline fun Style.setLeft  (value: Double) { left   = "${value}px" }
inline fun Style.setWidth (value: Double) { width  = "${max(0.0, value)}px" }
inline fun Style.setHeight(value: Double) { height = "${max(0.0, value)}px" }

inline fun Style.setTopPercent   (percent: Double        ) { top    = "$percent%" }
inline fun Style.setLeftPercent  (percent: Double        ) { left   = "$percent%" }
inline fun Style.setRightPercent (percent: Double        ) { right  = "$percent%" }
inline fun Style.setWidthPercent (percent: Double? = null) { width  = percent?.let { "$it%" } ?: "" }
inline fun Style.setHeightPercent(percent: Double? = null) { height = percent?.let { "$it%" } ?: "" }
inline fun Style.setBottomPercent(percent: Double        ) { bottom = "$percent%" }

inline fun Style.setColor  (value: Color ) { color  = "#${value.hexString}" }
inline fun Style.setCursor (value: Cursor) { cursor = value.toString() }
inline fun Style.setOpacity(value: kotlin.Float) { opacity = value.toString() }

inline fun Style.setFontStyle (value: FontStyle ) { fontStyle  = value.value;     }
inline fun Style.setFontWeight(value: FontWeight) { fontWeight = "${value.value}" }

inline fun Style.setFontSize  (value: Int   ) { this.fontSize   = if (value >= 0) value.toString() else "1em";  }
inline fun Style.setFontFamily(value: String) { this.fontFamily = value;                                        }


inline fun Style.setDisplay (value: Display?  = null) { display  = value?.value ?: "" }
inline fun Style.setPosition(value: Position? = null) { position = value?.value ?: "" }

//fun Style.setOverflow (overflow: Overflow) { this.overflow  = overflow;   }
inline fun Style.setOverflowX(overflow: Overflow) { overflowX = overflow.value; }
inline fun Style.setOverflowY(overflow: Overflow) { overflowY = overflow.value; }

inline fun Style.setVisibility(value: Visibility) { visibility = value.value; }

inline fun Style.setBackgroundImage(image: Image? = null) { backgroundImage = image?.let { "url(${it.source})" } ?: "none" }
inline fun Style.setBackgroundColor(color: Color? = null) { backgroundColor = color?.let { "#${it.hexString}"  } ?: ""     }

inline fun Style.setBackgroundRepeat(repeat: Repeat) { backgroundRepeat = repeat.value; }

inline fun Style.setBackgroundPosition(x: Double, y: Double) { backgroundPosition = "$x $y" }

inline fun Style.setBorderWidth(width: Double) { borderWidth = width.toString() }

inline fun Style.setBorderColor(color: Color? = null) { borderColor = color?.let { "#${it.hexString}"  } ?: "" }

inline fun Style.setBorderStyle(style: BorderStyle) {borderStyle = style.value; }

inline fun Style.setBorderTop   (value: Double) {borderTop    = value.toString() }
inline fun Style.setBorderLeft  (value: Double) {borderLeft   = value.toString() }
inline fun Style.setBorderRight (value: Double) {borderRight  = value.toString() }
inline fun Style.setBorderBottom(value: Double) {borderBottom = value.toString() }

inline fun Style.setTextAlignment(alignment: TextAlignment) { textAlign = alignment.value; }

inline fun Style.setVerticalAlignment(alignment: VerticalAlign) { verticalAlign = alignment.value; }

inline fun Style.setFloat(float: Float? = null) { setPropertyValue("float", float?.value ?: "") }

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
