package com.zinoti.jaz.drawing

import com.zinoti.jaz.geometry.Size


interface Font {
    val size: Int

    val style: Set<Style>

    val family: String

    val layout: Layout

    val isBold get() = style.contains(Style.Bold)

    val isItalic get() = style.contains(Style.Italic)

    val isRotated: Boolean

    val isVertical: Boolean

    fun width(text: String): Double

    fun height(text: String): Double

    fun wrappedSize(text: String, indent: Double, width: Double): Size

    fun size(text: String): Size = Size.create(width(text), height(text))

    enum class Style {
        Italic, Bold
    }

    enum class Layout {
        LEFT_RIGHT,
        RIGHT_LEFT,
        TOP_BOTTOM,
        BOTTOM_TOP
    }
}
