package com.nectar.doodle.drawing


interface Font {
    val size: Int

    val weight: Int

    val style: Set<Style>

    val family: String

    val isItalic get() = style.contains(Style.Italic)

    enum class Style {
        Italic
    }
}