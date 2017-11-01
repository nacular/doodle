package com.nectar.doodle.drawing


interface Font {
    val size: Int

    val style: Set<Style>

    val family: String

    val isBold   get() = style.contains(Style.Bold)
    val isItalic get() = style.contains(Style.Italic)

    enum class Style {
        Italic, Bold
    }
}