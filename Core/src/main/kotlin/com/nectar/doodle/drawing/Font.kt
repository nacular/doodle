package com.nectar.doodle.drawing


interface Font {
    val size: Int

    val weight: Weight

    val style: Set<Style>

    val family: String

    val isItalic get() = style.contains(Style.Italic)

    enum class Style {
        Italic
    }

    enum class Weight(val value: Int) {
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
}