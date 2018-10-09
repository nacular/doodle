package com.nectar.doodle.drawing

import com.nectar.doodle.drawing.Font.Style
import com.nectar.doodle.drawing.Font.Weight
import com.nectar.doodle.drawing.Font.Weight.Normal


class FontInfo(
        var size  : Int        = -1,
        var style : Set<Style> = setOf(),
        var weight: Weight     = Normal,
        var family: String     = "")

private class FontImpl(
        override val size  : Int,
        override val weight: Weight,
        override val style : Set<Style>,
        override val family: String): Font {
}

interface FontDetector {
    operator fun invoke(info: FontInfo.() -> Unit, result: (Font) -> Unit)

    operator fun invoke(font: Font, info: FontInfo.() -> Unit, result: (Font) -> Unit)
}
