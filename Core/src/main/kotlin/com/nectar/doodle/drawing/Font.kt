package com.nectar.doodle.drawing


interface Font {
    val size: Int

    val style: Set<Style>

    val family: String

    val layout: Layout

    val isBold get() = style.contains(Style.Bold)

    val isItalic get() = style.contains(Style.Italic)

    val isRotated: Boolean

    val isVertical: Boolean

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