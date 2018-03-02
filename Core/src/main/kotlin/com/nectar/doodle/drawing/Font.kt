package com.nectar.doodle.drawing

import com.nectar.doodle.drawing.Font.Style.Italic


interface Font {
    val size  : Int
    val style : Set<Style>
    val weight: Weight
    val family: String

    val italic get() = Italic in style

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